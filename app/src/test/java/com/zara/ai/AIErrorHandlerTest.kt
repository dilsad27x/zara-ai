package com.zara.ai

import com.zara.ai.ai.engine.error.AIErrorCategory
import com.zara.ai.ai.engine.error.AIErrorHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException

class AIErrorHandlerTest {

    private lateinit var errorHandler: AIErrorHandler

    @Before
    fun setUp() {
        errorHandler = AIErrorHandler()
    }

    @Test
    fun `categorizes missing api key correctly`() {
        val exception = IllegalStateException("Gemini API key is not configured. Please add your API key in Settings.")
        val error = errorHandler.categorize(exception)

        assertEquals(AIErrorCategory.AUTHENTICATION_MISSING, error.category)
        assertFalse(error.canRetry)
        assertTrue(error.actionableGuidance.contains("Settings"))
    }

    @Test
    fun `categorizes rate limit 429 correctly`() {
        val exception = RuntimeException("HTTP 429: Resource has been exhausted (rate limit)")
        val error = errorHandler.categorize(exception)

        assertEquals(AIErrorCategory.RATE_LIMIT, error.category)
        assertTrue(error.canRetry)
    }

    @Test
    fun `categorizes connection timeout correctly`() {
        val exception = SocketTimeoutException("Connect timed out")
        val error = errorHandler.categorize(exception)

        assertEquals(AIErrorCategory.NETWORK_TIMEOUT, error.category)
        assertTrue(error.canRetry)
    }

    @Test
    fun `categorizes server overload 503 correctly`() {
        val exception = RuntimeException("HTTP 503: The model is overloaded")
        val error = errorHandler.categorize(exception)

        assertEquals(AIErrorCategory.SERVER_OVERLOAD, error.category)
        assertTrue(error.canRetry)
    }
}
