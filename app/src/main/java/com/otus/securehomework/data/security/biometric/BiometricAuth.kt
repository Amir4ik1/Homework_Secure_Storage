package com.otus.securehomework.data.security.biometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

class BiometricAuth @Inject constructor() {

    suspend fun run(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        when {
            activity.isStrongBiometricAvailable() -> runStrongBiometric(activity, onSuccess, onFailure)
            activity.isWeakBiometricAvailable() -> runWeakBiometric(activity, onSuccess, onFailure)
            else -> onFailure.invoke()
        }
    }

    private fun FragmentActivity.isStrongBiometricAvailable() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && isBiometricAvailable(BIOMETRIC_STRONG)

    private fun FragmentActivity.isWeakBiometricAvailable() =
        isBiometricAvailable(BIOMETRIC_WEAK)

    private fun FragmentActivity.isBiometricAvailable(authenticator: Int) =
        BiometricManager.from(this).canAuthenticate(authenticator) == BiometricManager.BIOMETRIC_SUCCESS

    private suspend fun runStrongBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss")
            .setConfirmationRequired(true)
            .build()
        try {
            authPrompt.authenticate(AuthPromptHost(activity))
            onSuccess.invoke()
        } catch (e: Throwable) {
            onFailure.invoke()
        }
    }

    private suspend fun runWeakBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val authPrompt = Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss")
            .setConfirmationRequired(true)
            .build()
        try {
            authPrompt.authenticate(AuthPromptHost(activity))
            onSuccess.invoke()
        } catch (e: Throwable) {
            onFailure.invoke()
        }
    }
}
