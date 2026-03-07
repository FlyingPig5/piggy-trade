package com.piggytrade.piggytrade.crypto

import android.util.Base64
import org.bouncycastle.crypto.generators.SCrypt
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class WalletManager {

    /**
     * Decrypts a Fernet token using BouncyCastle SCrypt to match the Python backend.
     */
    fun decryptMnemonic(walletData: Map<String, Any>, password: String): String {
        val saltB64 = walletData["salt"] as String
        val tokenB64 = walletData["token"] as String
        val kdfType = walletData.getOrDefault("kdf", "pbkdf2") as String

        val salt = Base64.decode(saltB64, Base64.DEFAULT)
        val token = Base64.decode(tokenB64, Base64.DEFAULT)

        val derivedKey: ByteArray = if (kdfType == "scrypt") {
            // Match Python: n=32768 (2^15), r=8, p=1, length=32
            SCrypt.generate(
                password.toByteArray(Charsets.UTF_8),
                salt,
                32768,
                8,
                1,
                32
            )
        } else {
            // PBKDF2 fallback if needed (not implementing here unless required, assuming scrypt)
            throw UnsupportedOperationException("Only scrypt is supported for native migration")
        }

        // Fernet key is urlsafe base64 encoded 32-bytes
        val fernetKeyBase64 = Base64.encode(derivedKey, Base64.URL_SAFE or Base64.NO_WRAP)
        
        return decryptFernet(token, fernetKeyBase64)
    }

    /**
     * Standard Fernet decryption: 
     * Version(1) | Timestamp(8) | IV(16) | Ciphertext(...) | HMAC(32)
     */
    private fun decryptFernet(token: ByteArray, urlSafeBase64Key: ByteArray): String {
        val decodedKey = Base64.decode(urlSafeBase64Key, Base64.URL_SAFE or Base64.NO_WRAP)
        require(decodedKey.size == 32) { "Fernet key must be 32 bytes" }

        val signingKey = decodedKey.copyOfRange(0, 16)
        val encryptionKey = decodedKey.copyOfRange(16, 32)

        val version = token[0]
        require(version == 0x80.toByte()) { "Invalid Fernet version" }

        val iv = token.copyOfRange(9, 25)
        val ciphertext = token.copyOfRange(25, token.size - 32)
        val hmac = token.copyOfRange(token.size - 32, token.size)

        // Verify HMAC
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(signingKey, "HmacSHA256"))
        val dataToSign = token.copyOfRange(0, token.size - 32)
        val computedHmac = mac.doFinal(dataToSign)
        
        require(MessageDigest.isEqual(hmac, computedHmac)) { "HMAC verification failed" }

        // Decrypt AES/CBC
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), IvParameterSpec(iv))
        val decrypted = cipher.doFinal(ciphertext)

        return String(decrypted, Charsets.UTF_8)
    }
}
