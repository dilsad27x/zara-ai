package com.zara.ai

import com.zara.ai.ai.engine.model.AIRequest
import com.zara.ai.ai.engine.model.AIResponse
import com.zara.ai.ai.engine.model.AIStreamChunk
import com.zara.ai.ai.engine.model.ProviderHealth
import com.zara.ai.ai.engine.provider.AIProvider
import com.zara.ai.ai.engine.provider.AIProviderRegistry
import com.zara.ai.ai.engine.router.ModelRouter
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ModelRouterTest {

    private lateinit var modelRouter: ModelRouter
    private lateinit var providerRegistry: AIProviderRegistry

    @Before
    fun setUp() {
        val logger = ZaraLogger()
        val fakeProvider = object : AIProvider {
            override val name: String = "Google Gemini"
            override suspend fun chat(request: AIRequest): Result<AIResponse> =
                Result.success(AIResponse(text = "ok", finishReason = "STOP"))
            override fun streamChat(request: AIRequest): Flow<AIStreamChunk> = emptyFlow()
            override suspend fun checkHealth(): ProviderHealth =
                ProviderHealth(providerName = "Google Gemini", isAvailable = true, latencyMs = 10L)
        }
        providerRegistry = AIProviderRegistry(fakeProvider, logger)
        modelRouter = ModelRouter(providerRegistry, logger)
    }

    @Test
    fun `default model is gemini-1_5-flash`() {
        assertEquals("gemini-1.5-flash", modelRouter.selectedModel.value)
        assertEquals("Google Gemini", modelRouter.getActiveProvider().name)
    }

    @Test
    fun `switching model updates selectedModel`() {
        modelRouter.setModel("gemini-1.5-pro")
        assertEquals("gemini-1.5-pro", modelRouter.selectedModel.value)

        modelRouter.setModel("gemini-2.0-flash")
        assertEquals("gemini-2.0-flash", modelRouter.selectedModel.value)
    }

    @Test
    fun `available models contains standard Gemini family`() {
        val models = modelRouter.getAvailableModels()
        assertTrue(models.contains("gemini-1.5-flash"))
        assertTrue(models.contains("gemini-1.5-pro"))
        assertTrue(models.contains("gemini-2.0-flash"))
    }
}
