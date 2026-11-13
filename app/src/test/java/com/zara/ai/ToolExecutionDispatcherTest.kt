package com.zara.ai

import com.zara.ai.ai.engine.model.AIToolCall
import com.zara.ai.ai.engine.tools.ToolExecutionDispatcher
import com.zara.ai.ai.engine.tools.ToolExecutionResult
import com.zara.ai.ai.engine.tools.ToolParameter
import com.zara.ai.ai.engine.tools.ToolParameterType
import com.zara.ai.ai.engine.tools.ToolRegistry
import com.zara.ai.ai.engine.tools.ZaraTool
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToolExecutionDispatcherTest {

    private lateinit var registry: ToolRegistry
    private lateinit var dispatcher: ToolExecutionDispatcher
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        val logger = ZaraLogger()
        registry = ToolRegistry(logger)
        dispatcher = ToolExecutionDispatcher(registry, logger, testDispatcher)
    }

    @Test
    fun `dispatch successfully executes registered tool`() = runTest {
        registry.register(
            ZaraTool(
                name = "echo_tool",
                description = "Echoes input text",
                category = "Test",
                parameters = listOf(
                    ToolParameter("text", ToolParameterType.STRING, "Text to echo", isRequired = true)
                ),
                execute = { args ->
                    val input = args["text"]?.toString() ?: ""
                    ToolExecutionResult(success = true, output = "Echo: $input")
                }
            )
        )

        val call = AIToolCall(id = "call_1", name = "echo_tool", arguments = mapOf("text" to "Hello ZARA"))
        val result = dispatcher.dispatch(call)

        assertFalse(result.isError)
        assertEquals("Echo: Hello ZARA", result.result)
    }

    @Test
    fun `dispatch fails when required parameter is missing`() = runTest {
        registry.register(
            ZaraTool(
                name = "strict_tool",
                description = "Requires non-null parameter",
                category = "Test",
                parameters = listOf(
                    ToolParameter("mandatory_field", ToolParameterType.STRING, "Must be provided", isRequired = true)
                ),
                execute = { ToolExecutionResult(success = true, output = "Success") }
            )
        )

        val call = AIToolCall(id = "call_2", name = "strict_tool", arguments = emptyMap())
        val result = dispatcher.dispatch(call)

        assertTrue(result.isError)
        assertTrue(result.result.contains("Missing required parameter"))
    }

    @Test
    fun `dispatch fails gracefully when tool does not exist`() = runTest {
        val call = AIToolCall(id = "call_3", name = "non_existent_tool", arguments = emptyMap())
        val result = dispatcher.dispatch(call)

        assertTrue(result.isError)
        assertTrue(result.result.contains("not registered"))
    }
}
