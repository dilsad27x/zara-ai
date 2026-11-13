package com.zara.ai.core.common.error

/**
 * Domain-specific centralized error hierarchy for ZARA AI.
 */
sealed class ZaraError(
    val code: String,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data class NetworkError(
        val statusCode: Int? = null,
        val endpoint: String? = null,
        override val message: String = "Network connection failed. Please verify your internet link.",
        override val cause: Throwable? = null
    ) : ZaraError("ERR_NETWORK", message, cause)

    data class SecurityError(
        val reason: SecurityFailureReason,
        override val message: String = reason.description,
        override val cause: Throwable? = null
    ) : ZaraError("ERR_SECURITY", message, cause)

    data class DatabaseError(
        val operation: String,
        override val message: String = "Database operation failed: $operation",
        override val cause: Throwable? = null
    ) : ZaraError("ERR_DATABASE", message, cause)

    data class AgentExecutionError(
        val taskId: String,
        val step: String,
        override val message: String = "Agent failed during step '$step' in task $taskId",
        override val cause: Throwable? = null
    ) : ZaraError("ERR_AGENT", message, cause)

    data class PermissionDeniedError(
        val permissionName: String,
        override val message: String = "Permission required: $permissionName",
        override val cause: Throwable? = null
    ) : ZaraError("ERR_PERMISSION", message, cause)

    data class UnknownError(
        override val message: String = "An unexpected error occurred.",
        override val cause: Throwable? = null
    ) : ZaraError("ERR_UNKNOWN", message, cause)
}

enum class SecurityFailureReason(val description: String) {
    KEYSTORE_CORRUPTED("Android Keystore could not be accessed."),
    BIOMETRIC_FAILED("Biometric authentication failed or was cancelled."),
    DECRYPTION_FAILED("Decryption of sensitive secure data failed."),
    INVALID_KEY("Supplied API key or token signature is invalid."),
    HARDWARE_UNAVAILABLE("Secure hardware enclave is unavailable.")
}
