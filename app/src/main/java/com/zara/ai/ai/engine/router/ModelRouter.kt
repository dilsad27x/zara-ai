package com.zara.ai.ai.engine.router

import com.zara.ai.ai.engine.model.AIRequest
import com.zara.ai.ai.engine.model.AIResponse
import com.zara.ai.ai.engine.model.AIStreamChunk
import com.zara.ai.ai.engine.model.ProviderHealth
import com.zara.ai.ai.engine.provider.AIProvider
import com.zara.ai.ai.engine.provider.AIProviderRegistry
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRouter @Inject constructor(
    private val providerRegistry: AIProviderRegistry,
    private val logger: ZaraLogger
) {
    private val _selectedModel = MutableStateFlow("gemini-1.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val availableModels = listOf(
        "gemini-1.5-flash",
        "gemini-1.5-pro",
        "gemini-2.0-flash"
    )

    fun getAvailableModels(): List<String> = availableModels

    fun setModel(modelName: String): Boolean {
        if (availableModels.contains(modelName)) {
            _selectedModel.value = modelName
            logger.i("ModelRouter", "Switched active AI Model to: $modelName")
            return true
        }
        logger.w("ModelRouter", "Model $modelName is not in standard list, using as custom model identifier")
        _selectedModel.value = modelName
        return true
    }

    fun getActiveProvider(): AIProvider {
        return providerRegistry.getActiveProvider()
    }

    suspend fun routeChat(request: AIRequest): Result<AIResponse> {
        val configuredRequest = request.copy(model = request.model ?: _selectedModel.value)
        val provider = getActiveProvider()
        logger.d("ModelRouter", "Routing chat request to ${provider.name} [model=${configuredRequest.model}]")
        return provider.chat(configuredRequest)
    }

    fun routeStreamChat(request: AIRequest): Flow<AIStreamChunk> {
        val configuredRequest = request.copy(model = request.model ?: _selectedModel.value)
        val provider = getActiveProvider()
        logger.d("ModelRouter", "Routing stream chat request to ${provider.name} [model=${configuredRequest.model}]")
        return provider.streamChat(configuredRequest)
    }

    suspend fun checkHealth(): ProviderHealth {
        return getActiveProvider().checkHealth()
    }
}
