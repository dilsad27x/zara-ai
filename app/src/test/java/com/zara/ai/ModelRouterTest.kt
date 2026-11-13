package com.zara.ai

import com.zara.ai.ai.engine.provider.AIProviderRegistry
import com.zara.ai.ai.engine.provider.gemini.GeminiProvider
import com.zara.ai.ai.engine.router.ModelRouter
import com.zara.ai.ai.engine.tools.ToolRegistry
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.security.SecurityManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModelRouterTest {

    private lateinit var modelRouter: ModelRouter
    private lateinit var providerRegistry: AIProviderRegistry

    @Before
    fun setUp() {
        val logger = ZaraLogger()
        val securityManager = mockk<SecurityManager>(relaxed = true)
        val toolRegistry = ToolRegistry(logger)
        val testDispatcher = UnconfinedTestDispatcher()

        val geminiProvider = GeminiProvider(securityManager, toolRegistry, logger, testDispatcher)
        providerRegistry = AIProviderRegistry(geminiProvider, logger)
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
