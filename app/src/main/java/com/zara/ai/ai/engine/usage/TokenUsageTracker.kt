package com.zara.ai.ai.engine.usage

import com.zara.ai.ai.engine.model.TokenUsage
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenUsageTracker @Inject constructor(
    private val logger: ZaraLogger
) {
    private val _sessionUsage = MutableStateFlow(TokenUsage())
    val sessionUsage: StateFlow<TokenUsage> = _sessionUsage.asStateFlow()

    private val _lifetimeTokens = MutableStateFlow(0)
    val lifetimeTokens: StateFlow<Int> = _lifetimeTokens.asStateFlow()

    fun recordUsage(usage: TokenUsage) {
        val current = _sessionUsage.value
        val updatedInput = current.promptTokens + usage.promptTokens
        val updatedOutput = current.completionTokens + usage.completionTokens
        val updatedTotal = current.totalTokens + usage.totalTokens

        // Gemini 1.5 Flash approx pricing: $0.075 / 1M prompt, $0.30 / 1M completion
        val cost = (updatedInput * 0.000000075) + (updatedOutput * 0.00000030)

        _sessionUsage.value = TokenUsage(
            promptTokens = updatedInput,
            completionTokens = updatedOutput,
            totalTokens = updatedTotal,
            estimatedCostUsd = cost
        )
        _lifetimeTokens.value = _lifetimeTokens.value + usage.totalTokens

        logger.d("TokenUsageTracker", "Recorded token usage: +${usage.totalTokens} (Session Total: $updatedTotal)")
    }

    fun resetSession() {
        _sessionUsage.value = TokenUsage()
    }
}
