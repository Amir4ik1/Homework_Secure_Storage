package com.otus.securehomework.data.security

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class UtilsAes @Inject constructor() {

    fun encryptAes(data: CharSequence, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encodedBytes = cipher.doFinal(data.toString().toByteArray(Charsets.UTF_8))
        
        val combined = iv + encodedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: CharSequence, key: Key): String {
        val combined = Base64.decode(encrypted.toString(), Base64.NO_WRAP)
        if (combined.size < GCM_IV_LENGTH) return ""

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        val decoded = cipher.doFinal(ciphertext)
        return String(decoded, Charsets.UTF_8)
    }

    companion object {
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
    }
}
