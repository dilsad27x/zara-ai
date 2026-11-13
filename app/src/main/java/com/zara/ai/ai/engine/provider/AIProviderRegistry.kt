package com.zara.ai.ai.engine.provider

import com.zara.ai.ai.engine.provider.gemini.GeminiProvider
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIProviderRegistry(
    initialProvider: AIProvider,
    private val logger: ZaraLogger
) {
    private val providers = ConcurrentHashMap<String, AIProvider>()
    private val _activeProviderName = MutableStateFlow(initialProvider.name)
    val activeProviderName: StateFlow<String> = _activeProviderName.asStateFlow()

    init {
        register(initialProvider)
    }

    @Inject
    constructor(geminiProvider: GeminiProvider, logger: ZaraLogger) : this(geminiProvider as AIProvider, logger)

    fun register(provider: AIProvider) {
        providers[provider.name] = provider
        logger.i("AIProviderRegistry", "Registered AI Provider: ${provider.name}")
    }

    fun setActiveProvider(providerName: String): Boolean {
        if (providers.containsKey(providerName)) {
            _activeProviderName.value = providerName
            logger.i("AIProviderRegistry", "Switched active AI Provider to: $providerName")
            return true
        }
        logger.w("AIProviderRegistry", "Provider not found: $providerName")
        return false
    }

    fun getActiveProvider(): AIProvider {
        val currentName = _activeProviderName.value
        return providers[currentName] ?: providers.values.first()
    }

    fun getAllProviders(): List<AIProvider> = providers.values.toList()
}
