package com.zara.ai

import com.zara.ai.ai.engine.tools.ToolParameter
import com.zara.ai.ai.engine.tools.ToolParameterType
import com.zara.ai.ai.engine.tools.ToolRegistry
import com.zara.ai.ai.engine.tools.ZaraTool
import com.zara.ai.core.common.logger.ZaraLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToolRegistryTest {

    private lateinit var registry: ToolRegistry

    @Before
    fun setUp() {
        registry = ToolRegistry(ZaraLogger())
    }

    @Test
    fun `register and retrieve tool by name`() {
        val testTool = ZaraTool(
            name = "test_calculator",
            description = "Adds two numbers together",
            category = "Math",
            parameters = listOf(
                ToolParameter(
                    name = "a",
                    type = ToolParameterType.NUMBER,
                    description = "First operand",
                    isRequired = true
                )
            ),
            execute = { com.zara.ai.ai.engine.tools.ToolExecutionResult(success = true, output = "Result: 42") }
        )

        registry.register(testTool)
        val retrieved = registry.getTool("test_calculator")

        assertNotNull(retrieved)
        assertEquals("test_calculator", retrieved?.name)
        assertEquals("Math", retrieved?.category)
    }

    @Test
    fun `buildGeminiToolsJson generates valid JSON schema`() {
        val testTool = ZaraTool(
            name = "set_brightness",
            description = "Sets screen brightness",
            category = "Device",
            parameters = listOf(
                ToolParameter(
                    name = "percentage",
                    type = ToolParameterType.INTEGER,
                    description = "Target brightness percentage 0-100",
                    isRequired = true
                )
            ),
            execute = { com.zara.ai.ai.engine.tools.ToolExecutionResult(success = true, output = "Brightness set") }
        )

        registry.register(testTool)
        val geminiJson = registry.buildGeminiToolsJson()

        assertTrue(geminiJson.length() >= 1)
        val firstFunc = geminiJson.getJSONObject(0)
        assertEquals("set_brightness", firstFunc.getString("name"))
        assertTrue(firstFunc.getJSONObject("parameters").getJSONObject("properties").has("percentage"))
    }
}
