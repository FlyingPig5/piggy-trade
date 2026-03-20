package com.piggytrade.piggytrade.blockchain

import java.math.BigDecimal
import java.math.RoundingMode

object Amm {
    private const val PRECISION = 100

    fun buyToken(amount: Long, ergPool: Long, tokenAmountFull: Long): Triple<BigDecimal, BigDecimal, BigDecimal> {
        val initialErg = BigDecimal.valueOf(ergPool)
        val initialToken = BigDecimal.valueOf(tokenAmountFull)
        val k = initialErg.multiply(initialToken)
        val ergNew = initialErg.add(BigDecimal.valueOf(amount))
        val tokenNew = k.divide(ergNew, PRECISION, RoundingMode.HALF_UP)
        val tokenDelta = initialToken.subtract(tokenNew)
        return Triple(tokenDelta, tokenNew, ergNew)
    }

    fun sellToken(tokensToSell: Long, poolNanoerg: Long, tokenBalance: Long): Quadruple<Long, Long, Long, Long> {
        val sellDec = BigDecimal.valueOf(tokensToSell)
        val poolErgDec = BigDecimal.valueOf(poolNanoerg)
        val tokenBalDec = BigDecimal.valueOf(tokenBalance)
        val k = poolErgDec.multiply(tokenBalDec)
        val tokenNew = tokenBalDec.add(sellDec)
        val ergNew = k.divide(tokenNew, PRECISION, RoundingMode.HALF_UP)
        val ergDelta = poolErgDec.subtract(ergNew)
        return Quadruple(ergDelta.toLong(), tokensToSell, tokenNew.toLong(), ergNew.toLong())
    }

    fun tokenForToken(tokensToSell: BigDecimal, txInBalance: BigDecimal, txOutBalance: BigDecimal, feePercentage: BigDecimal): BigDecimal {
        val effective = tokensToSell.multiply(BigDecimal.ONE.subtract(feePercentage))
        val newTxInBalance = txInBalance.add(effective)
        // tx_out_amount = tx_out_balance - (tx_out_balance * tx_in_balance) / new_tx_in_balance
        val k = txOutBalance.multiply(txInBalance)
        val sub = k.divide(newTxInBalance, PRECISION, RoundingMode.HALF_UP)
        return txOutBalance.subtract(sub)
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
