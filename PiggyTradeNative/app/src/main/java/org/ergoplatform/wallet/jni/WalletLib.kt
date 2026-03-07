package org.ergoplatform.wallet.jni

object WalletLib {
    init {
        System.loadLibrary("ergowalletlibjni")
    }

    // Original method (kept for compatibility)
    @JvmStatic external fun addressFromTestNet(addressStr: String): Long
    @JvmStatic external fun addressDelete(address: Long)

    /**
     * Derive an Ergo P2PK address from a BIP39 mnemonic using EIP-3 path m/44'/429'/0'/0/[index].
     * @param mnemonic BIP39 mnemonic phrase
     * @param mnemonicPass optional mnemonic passphrase (use empty string for none)
     * @param index child index (usually 0)
     * @param isMainnet true for mainnet, false for testnet
     * @return base58-encoded Ergo address string, or throws RuntimeException on error
     */
    @JvmStatic external fun mnemonicToAddress(
        mnemonic: String,
        mnemonicPass: String,
        index: Int,
        isMainnet: Boolean
    ): String

    /**
     * Build a reduced (ErgoPay-ready) transaction from raw input box bytes (hex) and output requests.
     *
     * @param inputBoxesHex JSON array of hex-encoded box bytes, e.g. ["aabbcc...", "ddeeff..."]
     * @param outputCandidatesJson JSON array of output candidates in ergo-lib JSON format
     * @param feeNano miner fee in nanoERGs
     * @param changeAddress change address (base58)
     * @param currentHeight current blockchain height
     * @param lastBlockHeadersJson JSON array of last 10 block headers from /blocks/lastHeaders/10
     * @return base64url-encoded ReducedTransaction bytes (to be used in ergopay: URL)
     */
    @JvmStatic external fun buildReducedTxBytes(
        inputBoxesHex: String,
        outputCandidatesJson: String,
        feeNano: Long,
        changeAddress: String,
        currentHeight: Int,
        lastBlockHeadersJson: String
    ): String

    @JvmStatic external fun signTransactionJson(
        mnemonic: String,
        mnemonicPass: String,
        inputBoxesHex: String,
        outputCandidatesJson: String,
        feeNano: Long,
        changeAddress: String,
        currentHeight: Int,
        lastBlockHeadersJson: String
    ): String
}
