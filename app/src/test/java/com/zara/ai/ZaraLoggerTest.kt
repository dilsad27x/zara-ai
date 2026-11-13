package com.zara.ai

import com.zara.ai.core.common.logger.LogLevel
import com.zara.ai.core.common.logger.ZaraLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZaraLoggerTest {

    @Test
    fun `agent log adds entry to log buffer`() {
        val logger = ZaraLogger()
        logger.agent("TestTag", "Agent step completed")

        val logs = logger.logsState.value
        assertEquals(1, logs.size)
        assertEquals(LogLevel.AGENT, logs.first().level)
        assertEquals("TestTag", logs.first().tag)
        assertEquals("Agent step completed", logs.first().message)
    }

    @Test
    fun `clearLogs flushes all entries`() {
        val logger = ZaraLogger()
        logger.i("Tag", "Info message")
        logger.clearLogs()

        assertTrue(logger.logsState.value.isEmpty())
    }
}
