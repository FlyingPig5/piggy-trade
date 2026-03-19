package com.piggytrade.piggytrade.blockchain

import com.piggytrade.piggytrade.network.NodeClient
import java.math.BigInteger

/**
 * Builds simple send transactions (ERG + tokens) to one or more recipients.
 * Produces the same txDict format that ErgoSigner.signTransaction() and
 * ErgoSigner.reduceTxForErgopay() already consume.
 */
class SendTxBuilder(
    private val client: NodeClient
) {
    companion object {
        /** Minimum nanoERG per output box (dust threshold) */
        const val MIN_BOX_VALUE = 1_000_000L
    }

    data class SendRecipient(
        val address: String,
        val nanoErg: Long,
        val tokens: List<TokenAmount> = emptyList()
    )

    data class TokenAmount(
        val tokenId: String,
        val amount: Long
    )

    /**
     * Build a send transaction with multiple recipients.
     *
     * @param recipients list of recipients with addresses, ERG amounts, and tokens
     * @param addressBoxes pre-fetched UTXOs per address from the wallet
     * @param changeAddress address for the change output
     * @param feeNano miner fee in nanoERGs
     * @param currentHeight current blockchain height
     * @return txDict compatible with ErgoSigner
     */
    suspend fun buildSendTx(
        recipients: List<SendRecipient>,
        addressBoxes: Map<String, List<Map<String, Any>>>,
        changeAddress: String,
        feeNano: Long,
        currentHeight: Int
    ): Map<String, Any> {
        require(recipients.isNotEmpty()) { "At least one recipient is required" }

        // Validate recipients
        for ((i, r) in recipients.withIndex()) {
            require(r.address.isNotEmpty()) { "Recipient ${i + 1}: address is empty" }
            require(r.nanoErg >= MIN_BOX_VALUE) {
                "Recipient ${i + 1}: minimum send is ${MIN_BOX_VALUE / 1_000_000_000.0} ERG"
            }
            for ((j, t) in r.tokens.withIndex()) {
                require(t.tokenId.isNotEmpty()) { "Recipient ${i + 1}, token ${j + 1}: token ID is empty" }
                require(t.amount > 0) { "Recipient ${i + 1}, token ${j + 1}: amount must be > 0" }
            }
        }

        // Calculate total required
        var totalErgRequired = feeNano
        val totalTokensRequired = mutableMapOf<String, Long>()

        for (r in recipients) {
            totalErgRequired += r.nanoErg
            for (t in r.tokens) {
                totalTokensRequired[t.tokenId] =
                    (totalTokensRequired[t.tokenId] ?: 0L) + t.amount
            }
        }

        // Flatten all boxes from all addresses
        val allBoxes = mutableListOf<Map<String, Any>>()
        for ((_, boxes) in addressBoxes) {
            allBoxes.addAll(boxes)
        }
        require(allBoxes.isNotEmpty()) { "No UTXOs available in wallet" }

        // Select minimum boxes needed
        val selectedBoxes = selectBoxes(
            allBoxes, totalErgRequired, totalTokensRequired
        )

        // Calculate what we selected
        var selectedErg = 0L
        val selectedTokens = mutableMapOf<String, Long>()
        for (box in selectedBoxes) {
            selectedErg += (box["value"] as? Number)?.toLong() ?: 0L
            val assets = box["assets"] as? List<Map<String, Any>> ?: emptyList()
            for (asset in assets) {
                val tid = asset["tokenId"] as String
                val amt = (asset["amount"] as? Number)?.toLong() ?: 0L
                selectedTokens[tid] = (selectedTokens[tid] ?: 0L) + amt
            }
        }

        // Validate sufficient funds
        require(selectedErg >= totalErgRequired) {
            "Insufficient ERG. Have ${selectedErg / 1_000_000_000.0}, need ${totalErgRequired / 1_000_000_000.0}"
        }
        for ((tid, required) in totalTokensRequired) {
            val have = selectedTokens[tid] ?: 0L
            require(have >= required) {
                "Insufficient token $tid. Have $have, need $required"
            }
        }

        // Build output requests
        val requests = mutableListOf<MutableMap<String, Any>>()

        // Recipient outputs
        for (r in recipients) {
            val assets = r.tokens.map { t ->
                mapOf("tokenId" to t.tokenId, "amount" to t.amount)
            }
            requests.add(mutableMapOf(
                "address" to r.address,
                "value" to r.nanoErg,
                "assets" to assets,
                "registers" to emptyMap<String, String>(),
                "creationHeight" to currentHeight
            ))
        }

        // Change output
        val changeErg = selectedErg - totalErgRequired
        val changeTokens = mutableMapOf<String, Long>()
        for ((tid, amt) in selectedTokens) {
            val sent = totalTokensRequired[tid] ?: 0L
            val remaining = amt - sent
            if (remaining > 0) {
                changeTokens[tid] = remaining
            }
        }

        // Only add change output if there's ERG or tokens remaining
        if (changeErg >= MIN_BOX_VALUE || changeTokens.isNotEmpty()) {
            val changeValue = if (changeErg >= MIN_BOX_VALUE) changeErg else MIN_BOX_VALUE
            val changeAssets = changeTokens.map { (tid, amt) ->
                mapOf("tokenId" to tid, "amount" to amt)
            }
            requests.add(mutableMapOf(
                "address" to changeAddress,
                "value" to changeValue,
                "assets" to changeAssets,
                "registers" to emptyMap<String, String>(),
                "creationHeight" to currentHeight
            ))
        }

        // Fetch box bytes for signing
        val inputIds = selectedBoxes.map { it["boxId"] as String }
        val inputsRaw = client.getBoxBytes(inputIds)

        return mapOf(
            "requests" to requests,
            "fee" to feeNano,
            "inputsRaw" to inputsRaw,
            "dataInputsRaw" to emptyList<String>(),
            "current_height" to currentHeight,
            "input_boxes" to selectedBoxes,
            "data_input_boxes" to emptyList<Map<String, Any>>(),
            "context_extensions" to emptyMap<String, Any>(),
            "inputIds" to inputIds
        )
    }

    /**
     * Selects the minimum number of UTXOs to cover the required ERG and tokens.
     * Sorts by token relevance first, then by ERG value descending.
     */
    private fun selectBoxes(
        allBoxes: List<Map<String, Any>>,
        requiredErg: Long,
        requiredTokens: Map<String, Long>
    ): List<Map<String, Any>> {
        // Score each box by how many required tokens it contains
        val sortedBoxes = allBoxes.sortedWith(compareByDescending<Map<String, Any>> { box ->
            val assets = box["assets"] as? List<Map<String, Any>> ?: emptyList()
            var tokenScore = 0L
            for ((tid, _) in requiredTokens) {
                assets.find { it["tokenId"] == tid }?.let {
                    tokenScore += (it["amount"] as? Number)?.toLong() ?: 0L
                }
            }
            tokenScore
        }.thenByDescending { box ->
            (box["value"] as? Number)?.toLong() ?: 0L
        })

        val selected = mutableListOf<Map<String, Any>>()
        var accErg = 0L
        val accTokens = mutableMapOf<String, Long>()

        for (box in sortedBoxes) {
            selected.add(box)
            accErg += (box["value"] as? Number)?.toLong() ?: 0L

            val assets = box["assets"] as? List<Map<String, Any>> ?: emptyList()
            for (asset in assets) {
                val tid = asset["tokenId"] as String
                val amt = (asset["amount"] as? Number)?.toLong() ?: 0L
                accTokens[tid] = (accTokens[tid] ?: 0L) + amt
            }

            val ergMet = accErg >= requiredErg
            val tokensMet = requiredTokens.all { (tid, req) ->
                (accTokens[tid] ?: 0L) >= req
            }
            if (ergMet && tokensMet) break
        }

        return selected
    }
}
