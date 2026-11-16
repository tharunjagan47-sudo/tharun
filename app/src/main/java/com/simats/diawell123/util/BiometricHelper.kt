package com.simats.diawell123.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    private val AUTHENTICATORS: Int = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    fun canAuthenticate(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(AUTHENTICATORS)
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return canAuthenticate(context) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getBiometricStatusMessage(context: Context): String {
        return when (canAuthenticate(context)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric authentication is available and ready."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric features available on this device."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric features are currently unavailable."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled. Please register a fingerprint or face in Android Settings."
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required for biometric authentication."
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometric features are unsupported on this system."
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Biometric status is unknown."
            else -> "Biometric authentication is not available."
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock DiaWell",
        subtitle: String? = "Confirm your fingerprint or face to proceed",
        negativeButtonText: String = "Use Passcode",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            if (!subtitle.isNullOrBlank()) {
                setSubtitle(subtitle)
            }
            setNegativeButtonText(negativeButtonText)
            setAllowedAuthenticators(AUTHENTICATORS)
        }.build()

        biometricPrompt.authenticate(promptInfo)
    }
}
