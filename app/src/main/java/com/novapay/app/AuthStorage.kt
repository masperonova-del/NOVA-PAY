package com.novapay.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

data class AuthUser(
    val name: String,
    val email: String,
    val phone: String,
    val phoneVerified: Boolean
)

enum class AuthStorageResult {
    SUCCESS,
    ACCOUNT_EXISTS,
    INVALID_DATA,
    ERROR
}

class AuthStorage(context: Context) {

    private val appContext = context.applicationContext

    private val prefs = appContext.getSharedPreferences(
        "nova_pay_auth",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ALIAS = "NOVA_PAY_AUTH_KEY"
        private const val ITERATIONS = 210000
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val specification = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .build()

        generator.init(specification)

        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val encrypted = cipher.doFinal(
            value.toByteArray(Charsets.UTF_8)
        )

        val combined = cipher.iv + encrypted

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    private fun decrypt(value: String): String? {
        return try {
            val combined = Base64.decode(
                value,
                Base64.NO_WRAP
            )

            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")

            cipher.init(
                Cipher.DECRYPT_MODE,
                getSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH, iv)
            )

            String(
                cipher.doFinal(encrypted),
                Charsets.UTF_8
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun hashPassword(
        password: String,
        salt: ByteArray
    ): ByteArray {

        val specification = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_SIZE
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(specification)
                .encoded
        } finally {
            specification.clearPassword()
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): AuthStorageResult {

        if (
            name.isBlank() ||
            !email.contains("@") ||
            phone.isBlank() ||
            password.length < 10
        ) {
            return AuthStorageResult.INVALID_DATA
        }

        if (prefs.contains("account_exists")) {
            return AuthStorageResult.ACCOUNT_EXISTS
        }

        return try {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)

            val passwordHash = hashPassword(password, salt)

            val editor = prefs.edit()
                .putString("name", encrypt(name.trim()))
                .putString("email", encrypt(email.trim().lowercase()))
                .putString("phone", encrypt(phone.trim()))
                .putString(
                    "salt",
                    Base64.encodeToString(salt, Base64.NO_WRAP)
                )
                .putString(
                    "password_hash",
                    Base64.encodeToString(
                        passwordHash,
                        Base64.NO_WRAP
                    )
                )
                .putString("phone_verified", encrypt("false"))
                .putString("account_exists", encrypt("true"))
                .putString("session", encrypt("true"))

            if (editor.commit()) {
                AuthStorageResult.SUCCESS
            } else {
                AuthStorageResult.ERROR
            }

        } catch (_: Exception) {
            AuthStorageResult.ERROR
        }
    }

    fun login(email: String, password: String): Boolean {
        return try {
            val savedEmail = prefs.getString("email", null)
                ?.let { decrypt(it) }
                ?: return false

            if (savedEmail != email.trim().lowercase()) {
                return false
            }

            val salt = Base64.decode(
                prefs.getString("salt", null) ?: return false,
                Base64.NO_WRAP
            )

            val savedHash = Base64.decode(
                prefs.getString("password_hash", null) ?: return false,
                Base64.NO_WRAP
            )

            val enteredHash = hashPassword(password, salt)

            if (!MessageDigest.isEqual(savedHash, enteredHash)) {
                return false
            }

            prefs.edit()
                .putString("session", encrypt("true"))
                .commit()

        } catch (_: Exception) {
            false
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getString("session", null)
            ?.let { decrypt(it) == "true" }
            ?: false
    }

    fun getCurrentUser(): AuthUser? {
        if (!isLoggedIn()) return null

        return try {
            AuthUser(
                name = decrypt(
                    prefs.getString("name", null) ?: return null
                ) ?: return null,

                email = decrypt(
                    prefs.getString("email", null) ?: return null
                ) ?: return null,

                phone = decrypt(
                    prefs.getString("phone", null) ?: return null
                ) ?: return null,

                phoneVerified = decrypt(
                    prefs.getString("phone_verified", null) ?: return null
                ) == "true"
            )
        } catch (_: Exception) {
            null
        }
    }

    fun logout() {
        try {
            prefs.edit()
                .putString("session", encrypt("false"))
                .commit()
        } catch (_: Exception) {
            prefs.edit().remove("session").commit()
        }
    }
