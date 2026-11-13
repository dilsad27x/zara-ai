package com.zara.ai.core.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zara.ai.core.common.error.SecurityFailureReason
import com.zara.ai.core.common.error.ZaraError
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.common.result.ZaraResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure settings and credentials architecture using Android Keystore and AES-GCM-256.
 */
@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: ZaraLogger
) {
    private val keyStoreAlias = "ZaraKeyStoreMasterKey"
    private val keyStoreType = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val gcmTagLength = 128
    private val ivLength = 12

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "zara_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        ensureSecretKeyExists()
    }

    private fun ensureSecretKeyExists() {
        try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            if (!keyStore.containsAlias(keyStoreAlias)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    keyStoreType
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
                logger.i("SecurityManager", "Generated new Android Keystore master key: $keyStoreAlias")
            }
        } catch (e: Exception) {
            logger.e("SecurityManager", "Failed to initialize Android Keystore key", e)
        }
    }

    fun storeSecureString(key: String, value: String): ZaraResult<Unit> {
        return try {
            securePrefs.edit().putString(key, value).apply()
            ZaraResult.Success(Unit)
        } catch (e: Exception) {
            logger.e("SecurityManager", "Failed to store secure key: $key", e)
            ZaraResult.Error(ZaraError.SecurityError(SecurityFailureReason.KEYSTORE_CORRUPTED, cause = e))
        }
    }

    fun getSecureString(key: String, defaultValue: String? = null): String? {
        return try {
            securePrefs.getString(key, defaultValue)
        } catch (e: Exception) {
            logger.e("SecurityManager", "Failed to retrieve secure key: $key", e)
            defaultValue
        }
    }

    fun removeSecureKey(key: String) {
        securePrefs.edit().remove(key).apply()
    }

    fun encryptPayload(plainText: String): ZaraResult<String> {
        return try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            val secretKey = keyStore.getKey(keyStoreAlias, null) as SecretKey
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            val base64Encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            ZaraResult.Success(base64Encoded)
        } catch (e: Exception) {
            logger.e("SecurityManager", "Payload encryption failed", e)
            ZaraResult.Error(ZaraError.SecurityError(SecurityFailureReason.DECRYPTION_FAILED, cause = e))
        }
    }

    fun decryptPayload(encryptedBase64: String): ZaraResult<String> {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val iv = ByteArray(ivLength)
            System.arraycopy(combined, 0, iv, 0, ivLength)

            val cipherTextSize = combined.size - ivLength
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(combined, ivLength, cipherText, 0, cipherTextSize)

            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            val secretKey = keyStore.getKey(keyStoreAlias, null) as SecretKey
            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            ZaraResult.Success(String(decryptedBytes, StandardCharsets.UTF_8))
        } catch (e: Exception) {
            logger.e("SecurityManager", "Payload decryption failed", e)
            ZaraResult.Error(ZaraError.SecurityError(SecurityFailureReason.DECRYPTION_FAILED, cause = e))
        }
    }
}
