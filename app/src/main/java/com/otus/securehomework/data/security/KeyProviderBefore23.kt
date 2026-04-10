package com.otus.securehomework.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class KeyProviderBefore23(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : KeyProvider {

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun getAesSecretKey(): SecretKey {
        val encryptedKeyBase64 = getSecretKeyFromSharedPreferences()
        return if (encryptedKeyBase64 != null) {
            val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)
            SecretKeySpec(key, AES_ALGORITHM)
        } else {
            generateAndSaveAesKey()
        }
    }

    private fun getSecretKeyFromSharedPreferences() =
        sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)

    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey
            ?: generateRsaSecretKey().private
    }

    private fun generateAndSaveAesKey(): SecretKey {
        val key = ByteArray(AES_KEY_LENGTH)
        SecureRandom().nextBytes(key)

        val encryptedKey = rsaEncryptKey(key)
        val encryptedKeyBase64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
        sharedPreferences.edit()
            .putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64)
            .apply()

        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)
    }

    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey
            ?: generateRsaSecretKey().public
    }

    private fun generateRsaSecretKey(): java.security.KeyPair {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)

        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    companion object {
        private const val AES_KEY_LENGTH = 16 
        private const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName"
        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        private const val RSA_KEY_ALIAS = "RSA_OTUS_DEMO"
        private const val RSA_ALGORITHM = "RSA"
        private const val AES_ALGORITHM = "AES"
        private const val KEY_PROVIDER = "AndroidKeyStore"
    }
}
