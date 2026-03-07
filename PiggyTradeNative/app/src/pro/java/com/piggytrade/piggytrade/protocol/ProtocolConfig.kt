package com.piggytrade.piggytrade.protocol

import java.math.BigInteger

object ProtocolConfig {
    fun getNodeConfigP2(n: BigInteger): BigInteger {
        val v1 = BigInteger.valueOf(0x2540BE400L)
        val v2 = BigInteger.valueOf(0x186A0L)
        val v = n.abs()
        
        if (v < v1) {
            return v2
        }
        
        val multiplier = BigInteger.valueOf(0x10L - 0x6L)
        val divisor = BigInteger.valueOf(0x4E20L)
        
        return v.multiply(multiplier).divide(divisor)
    }
}
