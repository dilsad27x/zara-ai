package com.zara.ai

import com.zara.ai.core.common.result.ZaraResult
import com.zara.ai.core.common.result.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZaraResultTest {

    @Test
    fun `success result holds data and maps correctly`() {
        val result = ZaraResult.Success("ZARA")
        val mapped = result.map { it.length }

        assertTrue(mapped is ZaraResult.Success)
        assertEquals(4, (mapped as ZaraResult.Success).data)
    }

    @Test
    fun `error result propagates failure`() {
        val exception = IllegalStateException("Test failure")
        val result = ZaraResult.Error(exception)
        val mapped = result.map { "Mapped" }

        assertTrue(mapped is ZaraResult.Error)
        assertEquals("Test failure", (mapped as ZaraResult.Error).message)
    }
}
