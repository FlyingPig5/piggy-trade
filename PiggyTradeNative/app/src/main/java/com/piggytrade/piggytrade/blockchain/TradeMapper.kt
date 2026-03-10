package com.piggytrade.piggytrade.blockchain

import android.util.Log

data class TradeRoute(
    val tokenKey: String,
    val orderType: String,
    val poolType: String
)

class TradeMapper(private val tokens: Map<String, Map<String, Any>>) {

    companion object {
        const val ERG = "ERG"
        private const val TAG = "TradeMapper"
    }

    // Normalized asset names reachable directly from ERG
    private val ergTokens = mutableSetOf<String>()
    // pairSides: normalized pair key -> (normalizedSide1, normalizedSide2)
    private val pairSides = mutableMapOf<String, Pair<String, String>>()
    // pairKeyOriginal: normalized pair key -> original key in `tokens` map
    private val pairKeyOriginal = mutableMapOf<String, String>()

    init {
        // 1. Identify all pairs first
        for ((key, data) in tokens) {
            val hasT2T = data.containsKey("id_in") || data.containsKey("id_out")
            val hasDash = key.contains("-")
            
            if (hasT2T || hasDash) {
                val nameIn = (data["name_in"] as? String)
                val nameOut = (data["name_out"] as? String)
                
                val parts = key.split("-", limit = 2)
                val rawN1 = nameIn ?: parts[0]
                val rawN2 = nameOut ?: if (parts.size > 1) parts[1] else ""
                
                val n1 = com.piggytrade.piggytrade.data.TokenRepository.normalizeTokenName(rawN1)
                val n2 = com.piggytrade.piggytrade.data.TokenRepository.normalizeTokenName(rawN2)
                
                val normalizedKey = "$n1-$n2"
                pairSides[normalizedKey] = Pair(n1, n2)
                pairKeyOriginal[normalizedKey] = key
            }
        }
        
        // 2. Identify ERG-to-Token assets (everything that isn't a pair)
        for ((key, data) in tokens) {
            val isT2T = data.containsKey("id_in") || data.containsKey("id_out")
            val hasDash = key.contains("-")
            if (!isT2T && !hasDash) {
                ergTokens.add(com.piggytrade.piggytrade.data.TokenRepository.normalizeTokenName(key))
            }
        }
    }

    fun allAssets(): List<String> {
        val seen = mutableSetOf<String>()
        seen.add(ERG)
        seen.addAll(ergTokens)
        for ((_, sides) in pairSides) {
            seen.add(sides.first)
            seen.add(sides.second)
        }
        val others = seen.filter { it != ERG }.sorted()
        return listOf(ERG) + others
    }

    fun normalizeAsset(assetName: String?): String? {
        if (assetName.isNullOrBlank()) return assetName
        return com.piggytrade.piggytrade.data.TokenRepository.normalizeTokenName(assetName)
    }

    fun toAssetsFor(fromAsset: String): List<String> {
        val reachable = mutableSetOf<String>()
        val fa = normalizeAsset(fromAsset) ?: return emptyList()

        if (fa == ERG) {
            reachable.addAll(ergTokens)
        } else if (fa in ergTokens) {
            reachable.add(ERG)
        }

        for ((_, sides) in pairSides) {
            if (fa == sides.second) {
                reachable.add(sides.first)
            } else if (fa == sides.first) {
                reachable.add(sides.second)
            }
        }

        reachable.remove(fa)
        return reachable.sorted()
    }

    fun resolve(fromAsset: String, toAsset: String): TradeRoute? {
        val fa = normalizeAsset(fromAsset) ?: return null
        val ta = normalizeAsset(toAsset) ?: return null

        Log.d(TAG, "resolve('$fromAsset','$toAsset') -> fa='$fa' ta='$ta' ergTokens=${ergTokens.size}")

        if (fa == ERG && ta in ergTokens) {
            val originalKey = findOriginalErgKey(ta)
            Log.d(TAG, "BUY ERG->Token route: key='$originalKey'")
            return TradeRoute(tokenKey = originalKey, orderType = "BUY", poolType = "erg")
        }
        if (ta == ERG && fa in ergTokens) {
            val originalKey = findOriginalErgKey(fa)
            Log.d(TAG, "SELL Token->ERG route: key='$originalKey'")
            return TradeRoute(tokenKey = originalKey, orderType = "SELL", poolType = "erg")
        }

        for ((normalizedKey, sides) in pairSides) {
            val originalKey = pairKeyOriginal[normalizedKey] ?: normalizedKey
            if (fa == sides.second && ta == sides.first) {
                return TradeRoute(tokenKey = originalKey, orderType = "BUY", poolType = "token")
            }
            if (fa == sides.first && ta == sides.second) {
                return TradeRoute(tokenKey = originalKey, orderType = "SELL", poolType = "token")
            }
        }

        Log.w(TAG, "No route for '$fa' -> '$ta'. ergTokens sample: ${ergTokens.take(5)}")
        return null
    }

    // Find the original key in the `tokens` map that normalizes to the given name
    private fun findOriginalErgKey(normalizedName: String): String {
        val trimmed = normalizedName.trim()
        if (tokens.containsKey(trimmed)) return trimmed
        return tokens.keys.firstOrNull { it.trim().equals(trimmed, ignoreCase = true) } ?: trimmed
    }

    fun describeRoute(route: TradeRoute, amount: String, expected: String): String {
        if (route.poolType == "erg") {
            return if (route.orderType == "BUY") {
                "$expected ${route.tokenKey} for $amount ERG"
            } else {
                "$expected ERG for $amount ${route.tokenKey}"
            }
        } else {
            val sides = pairSides[route.tokenKey] ?: Pair(route.tokenKey, "???")
            return if (route.orderType == "BUY") {
                "$expected ${sides.first} for $amount ${sides.second}"
            } else {
                "$expected ${sides.second} for $amount ${sides.first}"
            }
        }
    }
}
