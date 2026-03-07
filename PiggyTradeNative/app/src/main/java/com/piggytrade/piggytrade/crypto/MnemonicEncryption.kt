package com.piggytrade.piggytrade.crypto

import android.util.Base64
import org.bouncycastle.crypto.generators.SCrypt
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encrypts and decrypts mnemonics using Fernet-compatible format (matching Python backend).
 * Scheme: SCrypt KDF → 32-byte key → Fernet (AES-CBC + HMAC-SHA256)
 *
 * Token format: version(1) | timestamp(8) | iv(16) | ciphertext | hmac(32)
 */
object MnemonicEncryption {

    data class EncryptedMnemonic(val salt: String, val token: String)

    private const val SCRYPT_N = 32768
    private const val SCRYPT_R = 8
    private const val SCRYPT_P = 1
    private const val KEY_LEN = 32

    fun encrypt(mnemonic: String, password: String): EncryptedMnemonic {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)

        val signingKey = key.copyOfRange(0, 16)
        val encryptionKey = key.copyOfRange(16, 32)

        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), IvParameterSpec(iv))
        val ciphertext = cipher.doFinal(mnemonic.toByteArray(Charsets.UTF_8))

        // Build Fernet token: 0x80 | timestamp(8 BE) | iv(16) | ciphertext | hmac(32)
        val timestamp = (System.currentTimeMillis() / 1000)
        val payload = ByteArray(1 + 8 + 16 + ciphertext.size)
        payload[0] = 0x80.toByte()
        for (i in 7 downTo 0) { payload[1 + (7 - i)] = ((timestamp shr (i * 8)) and 0xFF).toByte() }
        System.arraycopy(iv, 0, payload, 9, 16)
        System.arraycopy(ciphertext, 0, payload, 25, ciphertext.size)

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(signingKey, "HmacSHA256"))
        val hmac = mac.doFinal(payload)

        val token = ByteArray(payload.size + 32)
        System.arraycopy(payload, 0, token, 0, payload.size)
        System.arraycopy(hmac, 0, token, payload.size, 32)

        return EncryptedMnemonic(
            salt = Base64.encodeToString(salt, Base64.DEFAULT),
            token = Base64.encodeToString(token, Base64.DEFAULT)
        )
    }

    fun decrypt(encrypted: EncryptedMnemonic, password: String): String {
        val salt = Base64.decode(encrypted.salt, Base64.DEFAULT)
        val token = Base64.decode(encrypted.token, Base64.DEFAULT)
        val key = deriveKey(password, salt)

        val signingKey = key.copyOfRange(0, 16)
        val encryptionKey = key.copyOfRange(16, 32)

        require(token[0] == 0x80.toByte()) { "Invalid Fernet version" }

        val iv = token.copyOfRange(9, 25)
        val ciphertext = token.copyOfRange(25, token.size - 32)
        val hmac = token.copyOfRange(token.size - 32, token.size)

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(signingKey, "HmacSHA256"))
        val computedHmac = mac.doFinal(token.copyOfRange(0, token.size - 32))
        require(java.security.MessageDigest.isEqual(hmac, computedHmac)) { "HMAC verification failed" }

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray = SCrypt.generate(
        password.toByteArray(Charsets.UTF_8),
        salt,
        SCRYPT_N,
        SCRYPT_R,
        SCRYPT_P,
        KEY_LEN
    )
}
