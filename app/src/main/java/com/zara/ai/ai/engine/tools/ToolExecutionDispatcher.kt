package com.zara.ai.ai.engine.tools

import com.zara.ai.ai.engine.model.AIToolCall
import com.zara.ai.ai.engine.model.AIToolResult
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolExecutionDispatcher @Inject constructor(
    private val toolRegistry: ToolRegistry,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    private val defaultTimeoutMs = 15_000L

    suspend fun dispatch(toolCall: AIToolCall): AIToolResult = withContext(ioDispatcher) {
        val startTime = System.currentTimeMillis()
        val tool = toolRegistry.getTool(toolCall.name)

        if (tool == null) {
            logger.w("ToolExecutionDispatcher", "Tool not found: ${toolCall.name}")
            return@withContext AIToolResult(
                toolCallId = toolCall.id,
                toolName = toolCall.name,
                result = "Error: Tool '${toolCall.name}' is not registered or supported.",
                isError = true
            )
        }

        // Validate required arguments
        val validationError = validateArguments(tool, toolCall.arguments)
        if (validationError != null) {
            logger.w("ToolExecutionDispatcher", "Validation failed for ${toolCall.name}: $validationError")
            return@withContext AIToolResult(
                toolCallId = toolCall.id,
                toolName = toolCall.name,
                result = "Error: $validationError",
                isError = true
            )
        }

        logger.agent("ToolExecutionDispatcher", "Executing tool: ${toolCall.name} with args: ${toolCall.arguments}")

        try {
            val executionResult = withTimeoutOrNull(defaultTimeoutMs) {
                tool.execute(toolCall.arguments)
            }

            val elapsed = System.currentTimeMillis() - startTime

            if (executionResult == null) {
                logger.e("ToolExecutionDispatcher", "Tool '${toolCall.name}' timed out after ${defaultTimeoutMs}ms")
                AIToolResult(
                    toolCallId = toolCall.id,
                    toolName = toolCall.name,
                    result = "Error: Tool execution timed out after ${defaultTimeoutMs / 1000} seconds.",
                    isError = true
                )
            } else if (!executionResult.success) {
                logger.w("ToolExecutionDispatcher", "Tool '${toolCall.name}' failed: ${executionResult.errorMessage}")
                AIToolResult(
                    toolCallId = toolCall.id,
                    toolName = toolCall.name,
                    result = executionResult.errorMessage ?: "Unknown tool error",
                    isError = true
                )
            } else {
                logger.i("ToolExecutionDispatcher", "Tool '${toolCall.name}' succeeded in ${elapsed}ms")
                AIToolResult(
                    toolCallId = toolCall.id,
                    toolName = toolCall.name,
                    result = executionResult.output,
                    isError = false
                )
            }
        } catch (e: Exception) {
            logger.e("ToolExecutionDispatcher", "Exception running tool '${toolCall.name}'", e)
            AIToolResult(
                toolCallId = toolCall.id,
                toolName = toolCall.name,
                result = "Error executing tool: ${e.localizedMessage ?: e.javaClass.simpleName}",
                isError = true
            )
        }
    }

    private fun validateArguments(tool: ZaraTool, arguments: Map<String, Any?>): String? {
        for (param in tool.parameters) {
            if (param.isRequired && (!arguments.containsKey(param.name) || arguments[param.name] == null)) {
                return "Missing required parameter: '${param.name}' (${param.description})"
            }
        }
        return null
    }
}
