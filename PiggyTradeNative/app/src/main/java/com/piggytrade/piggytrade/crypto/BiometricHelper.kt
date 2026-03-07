package com.piggytrade.piggytrade.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricHelper {

    companion object {
        private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_NAME = "PiggyTradeBiometricKey"
    }

    /**
     * Replaces Chaquopy hacks with native Android Keystore logic.
     * Ties `BiometricPrompt.CryptoObject` directly to AES cipher.
     */
    fun createCryptoObject(): BiometricPrompt.CryptoObject {
        val cipher = getCipher()
        val secretKey = getSecretKey() ?: generateSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return BiometricPrompt.CryptoObject(cipher)
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}")
    }

    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER)
        keyStore.load(null)
        return keyStore.getKey(KEY_NAME, null) as SecretKey?
    }

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEY_STORE_PROVIDER
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}
