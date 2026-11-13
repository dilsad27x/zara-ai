package com.zara.ai.ai.engine.gateway

import com.zara.ai.ai.engine.context.ContextManager
import com.zara.ai.ai.engine.model.AIMessage
import com.zara.ai.ai.engine.model.AIRequest
import com.zara.ai.ai.engine.model.AIResponse
import com.zara.ai.ai.engine.model.AIRole
import com.zara.ai.ai.engine.model.AIStreamChunk
import com.zara.ai.ai.engine.model.AIToolCall
import com.zara.ai.ai.engine.model.AIToolResult
import com.zara.ai.ai.engine.model.TokenUsage
import com.zara.ai.ai.engine.router.ModelRouter
import com.zara.ai.ai.engine.tools.ToolExecutionDispatcher
import com.zara.ai.ai.engine.usage.TokenUsageTracker
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.designsystem.components.OrbState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class GatewayEvent {
    data class TextChunk(val text: String) : GatewayEvent()
    data class ToolExecuting(val toolName: String) : GatewayEvent()
    data class ToolCompleted(val toolName: String, val resultSummary: String) : GatewayEvent()
    data class Finished(val fullText: String, val tokenUsage: TokenUsage?) : GatewayEvent()
    data class Error(val message: String) : GatewayEvent()
}

@Singleton
class AIGateway @Inject constructor(
    private val modelRouter: ModelRouter,
    private val contextManager: ContextManager,
    private val toolDispatcher: ToolExecutionDispatcher,
    private val tokenTracker: TokenUsageTracker,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    private val _engineOrbState = MutableStateFlow(OrbState.IDLE)
    val engineOrbState: StateFlow<OrbState> = _engineOrbState.asStateFlow()

    private val maxToolRecursionDepth = 5

    /**
     * Streams conversation response with automated structured function calling loop.
     */
    fun processConversationStream(
        conversationId: String,
        prompt: String,
        history: List<AIMessage> = emptyList()
    ): Flow<GatewayEvent> = flow {
        _engineOrbState.value = OrbState.THINKING
        logger.i("AIGateway", "Processing conversation stream for: '${prompt.take(40)}...'")

        val systemInstruction = contextManager.buildSystemInstruction()
        val messages = mutableListOf<AIMessage>()

        // Add history and system context
        if (history.isNotEmpty()) {
            messages.addAll(history)
        } else {
            messages.addAll(contextManager.buildConversationContext(conversationId, prompt))
        }

        var recursionDepth = 0
        val accumulatedText = StringBuilder()
        var lastUsage: TokenUsage? = null

        while (recursionDepth < maxToolRecursionDepth) {
            recursionDepth++
            val request = AIRequest(
                messages = messages.toList(),
                systemInstruction = systemInstruction,
                stream = true
            )

            var pendingToolCall: AIToolCall? = null

            _engineOrbState.value = OrbState.PROCESSING

            modelRouter.routeStreamChat(request).collect { chunk ->
                if (chunk.textChunk.isNotEmpty()) {
                    _engineOrbState.value = OrbState.SPEAKING
                    accumulatedText.append(chunk.textChunk)
                    emit(GatewayEvent.TextChunk(chunk.textChunk))
                }
                if (chunk.toolCallChunk != null) {
                    pendingToolCall = chunk.toolCallChunk
                }
                if (chunk.tokenUsage != null) {
                    lastUsage = chunk.tokenUsage
                    tokenTracker.recordUsage(chunk.tokenUsage)
                }
            }

            // Check if Gemini requested a tool execution
            if (pendingToolCall != null) {
                val toolCall = pendingToolCall!!
                _engineOrbState.value = OrbState.EXECUTING
                emit(GatewayEvent.ToolExecuting(toolCall.name))

                // Record model's assistant turn with tool call
                messages.add(
                    AIMessage(
                        role = AIRole.ASSISTANT,
                        content = accumulatedText.toString(),
                        toolCalls = listOf(toolCall)
                    )
                )

                // Execute the tool
                val toolResult: AIToolResult = toolDispatcher.dispatch(toolCall)
                emit(GatewayEvent.ToolCompleted(toolCall.name, toolResult.result.take(60)))

                // Add tool result to conversation messages for next model turn
                messages.add(
                    AIMessage(
                        role = AIRole.TOOL,
                        content = toolResult.result,
                        toolResults = listOf(toolResult)
                    )
                )

                accumulatedText.clear()
                _engineOrbState.value = OrbState.THINKING
                // Loop continues to let Gemini produce the final answer with tool result
            } else {
                // No further tool calls, finished!
                break
            }
        }

        _engineOrbState.value = OrbState.IDLE
        emit(GatewayEvent.Finished(accumulatedText.toString(), lastUsage))
    }.flowOn(ioDispatcher)

    /**
     * Non-streaming direct intelligence query.
     */
    suspend fun queryDirect(
        prompt: String,
        systemInstruction: String? = null
    ): Result<AIResponse> = withContext(ioDispatcher) {
        _engineOrbState.value = OrbState.THINKING
        val sys = systemInstruction ?: contextManager.buildSystemInstruction()
        val request = AIRequest(
            messages = listOf(AIMessage(role = AIRole.USER, content = prompt)),
            systemInstruction = sys,
            stream = false
        )

        val result = modelRouter.routeChat(request)
        _engineOrbState.value = OrbState.IDLE

        result.onSuccess { resp ->
            tokenTracker.recordUsage(resp.tokenUsage)
        }
        result
    }

    fun setOrbState(state: OrbState) {
        _engineOrbState.value = state
    }
}
