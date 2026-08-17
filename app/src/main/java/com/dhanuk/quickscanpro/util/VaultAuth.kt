package com.dhanuk.quickscanpro.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Vault security is fully delegated to the device's own secure lock
 * (biometrics or PIN/pattern/password). `BiometricPrompt` with
 * DEVICE_CREDENTIAL runs the real system lock UI, so it can never be
 * bypassed through app-level mistakes.
 */
object VaultAuth {

    // DEVICE_CREDENTIAL lets the system's own PIN/pattern/password screen
    // unlock the vault on devices without biometrics. Do NOT combine with
    // BIOMETRIC_STRONG, and do NOT set a negative button text — both crash
    // BiometricPrompt when DEVICE_CREDENTIAL is present.
    private const val AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun hasDeviceLock(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun openSecuritySettings(context: Context) {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }.onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }

    fun unlock(
        activity: FragmentActivity,
        title: String = "Unlock Vault",
        subtitle: String = "Use your device's lock to view protected scans",
        onSuccess: () -> Unit,
        onCancel: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) onCancel()
                    else onError(errString.toString())
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build()
        )
    }
}
