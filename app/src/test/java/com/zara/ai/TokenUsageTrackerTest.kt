package com.zara.ai

import com.zara.ai.ai.engine.model.TokenUsage
import com.zara.ai.ai.engine.usage.TokenUsageTracker
import com.zara.ai.core.common.logger.ZaraLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenUsageTrackerTest {

    private lateinit var tracker: TokenUsageTracker

    @Before
    fun setUp() {
        tracker = TokenUsageTracker(ZaraLogger())
    }

    @Test
    fun `recordUsage accumulates tokens and calculates cost`() {
        val usage1 = TokenUsage(promptTokens = 100, completionTokens = 50, totalTokens = 150)
        tracker.recordUsage(usage1)

        assertEquals(100, tracker.sessionUsage.value.promptTokens)
        assertEquals(50, tracker.sessionUsage.value.completionTokens)
        assertEquals(150, tracker.sessionUsage.value.totalTokens)
        assertEquals(150, tracker.lifetimeTokens.value)
        assertTrue(tracker.sessionUsage.value.estimatedCostUsd > 0.0)

        val usage2 = TokenUsage(promptTokens = 200, completionTokens = 100, totalTokens = 300)
        tracker.recordUsage(usage2)

        assertEquals(300, tracker.sessionUsage.value.promptTokens)
        assertEquals(150, tracker.sessionUsage.value.completionTokens)
        assertEquals(450, tracker.sessionUsage.value.totalTokens)
        assertEquals(450, tracker.lifetimeTokens.value)
    }

    @Test
    fun `resetSession clears session but preserves lifetime tokens`() {
        tracker.recordUsage(TokenUsage(promptTokens = 50, completionTokens = 50, totalTokens = 100))
        tracker.resetSession()

        assertEquals(0, tracker.sessionUsage.value.totalTokens)
        assertEquals(100, tracker.lifetimeTokens.value)
    }
}
