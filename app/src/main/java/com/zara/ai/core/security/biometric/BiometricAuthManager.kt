package com.zara.ai.core.security.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.zara.ai.core.common.error.SecurityFailureReason
import com.zara.ai.core.common.error.ZaraError
import com.zara.ai.core.common.result.ZaraResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCancellableCoroutine

enum class BiometricStatus {
    AVAILABLE,
    NOT_ENROLLED,
    HARDWARE_UNAVAILABLE,
    UNSUPPORTED
}

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun checkBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.HARDWARE_UNAVAILABLE
            else -> BiometricStatus.UNSUPPORTED
        }
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Verify identity for ZARA AI secure access"
    ): ZaraResult<Unit> = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (continuation.isActive) continuation.resume(ZaraResult.Success(Unit))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (continuation.isActive) {
                        continuation.resume(
                            ZaraResult.Error(
                                ZaraError.SecurityError(
                                    SecurityFailureReason.BIOMETRIC_FAILED,
                                    message = errString.toString()
                                )
                            )
                        )
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Prompt remains open for retry on transient failure
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
