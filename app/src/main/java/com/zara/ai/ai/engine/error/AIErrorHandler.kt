package com.zara.ai.ai.engine.error

import javax.inject.Inject
import javax.inject.Singleton

enum class AIErrorCategory {
    AUTHENTICATION_MISSING,
    AUTHENTICATION_INVALID,
    RATE_LIMIT,
    NETWORK_TIMEOUT,
    NETWORK_OFFLINE,
    SERVER_OVERLOAD,
    SAFETY_BLOCKED,
    TOOL_FAILED,
    UNKNOWN
}

data class FormattedAIError(
    val category: AIErrorCategory,
    val title: String,
    val userMessage: String,
    val actionableGuidance: String,
    val canRetry: Boolean = true
)

@Singleton
class AIErrorHandler @Inject constructor() {

    fun categorize(throwable: Throwable): FormattedAIError {
        val msg = throwable.localizedMessage ?: throwable.message ?: ""

        return when {
            msg.contains("API key is not configured", ignoreCase = true) || msg.contains("missing", ignoreCase = true) && msg.contains("key", ignoreCase = true) -> {
                FormattedAIError(
                    category = AIErrorCategory.AUTHENTICATION_MISSING,
                    title = "Gemini API Key Required",
                    userMessage = "ZARA cannot reach Google Gemini because your API key has not been configured.",
                    actionableGuidance = "Open Settings → General & API Key and enter your Gemini API key.",
                    canRetry = false
                )
            }
            msg.contains("401", ignoreCase = true) || msg.contains("403", ignoreCase = true) || msg.contains("API_KEY_INVALID", ignoreCase = true) -> {
                FormattedAIError(
                    category = AIErrorCategory.AUTHENTICATION_INVALID,
                    title = "Invalid API Key",
                    userMessage = "The provided Gemini API key was rejected by Google's authentication servers.",
                    actionableGuidance = "Please verify your key from Google AI Studio and re-enter it in Settings.",
                    canRetry = false
                )
            }
            msg.contains("429", ignoreCase = true) || msg.contains("quota", ignoreCase = true) || msg.contains("rate limit", ignoreCase = true) -> {
                FormattedAIError(
                    category = AIErrorCategory.RATE_LIMIT,
                    title = "Rate Limit Reached",
                    userMessage = "Gemini API rate limit has been exceeded for your current tier.",
                    actionableGuidance = "Please wait a few seconds before sending another message.",
                    canRetry = true
                )
            }
            msg.contains("timeout", ignoreCase = true) || msg.contains("SocketTimeoutException", ignoreCase = true) -> {
                FormattedAIError(
                    category = AIErrorCategory.NETWORK_TIMEOUT,
                    title = "Connection Timeout",
                    userMessage = "The request to Gemini servers timed out.",
                    actionableGuidance = "Check your phone's network connection and tap 'Regenerate'.",
                    canRetry = true
                )
            }
            msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("offline", ignoreCase = true) -> {
                FormattedAIError(
                    category = AIErrorCategory.NETWORK_OFFLINE,
                    title = "No Internet Connection",
                    userMessage = "Your smartphone appears to be offline.",
                    actionableGuidance = "Please check your Wi-Fi or mobile data connection.",
                    canRetry = true
                )
            }
            msg.contains("500", ignoreCase = true) || msg.contains("503", ignoreCase = true) || msg.contains("overloaded", ignoreCase = true) -> {
                FormattedAIError(
                    category = AIErrorCategory.SERVER_OVERLOAD,
                    title = "Gemini Server Overloaded",
                    userMessage = "Google Gemini servers are experiencing temporary high load.",
                    actionableGuidance = "Please wait a moment and tap 'Regenerate'.",
                    canRetry = true
                )
            }
            else -> {
                FormattedAIError(
                    category = AIErrorCategory.UNKNOWN,
                    title = "AI Engine Notice",
                    userMessage = msg.ifBlank { "An unexpected error occurred during AI processing." },
                    actionableGuidance = "Please try asking again or check Settings.",
                    canRetry = true
                )
            }
        }
    }
}
