package com.zara.ai.ai.engine.model

enum class AIRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}

data class AIToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, Any?>
)

data class AIToolResult(
    val toolCallId: String,
    val toolName: String,
    val result: String,
    val isError: Boolean = false
)

data class AIMessage(
    val role: AIRole,
    val content: String,
    val toolCalls: List<AIToolCall> = emptyList(),
    val toolResults: List<AIToolResult> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val estimatedCostUsd: Double = 0.0
)

data class AIRequest(
    val messages: List<AIMessage>,
    val systemInstruction: String? = null,
    val tools: List<String> = emptyList(),
    val model: String? = null,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 4096,
    val stream: Boolean = true
)

data class AIResponse(
    val text: String,
    val toolCalls: List<AIToolCall> = emptyList(),
    val finishReason: String = "STOP",
    val tokenUsage: TokenUsage = TokenUsage(),
    val modelName: String = "",
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class AIStreamChunk(
    val textChunk: String = "",
    val toolCallChunk: AIToolCall? = null,
    val isFinished: Boolean = false,
    val finishReason: String? = null,
    val tokenUsage: TokenUsage? = null
)

data class AIConfiguration(
    val activeProvider: String = "Google Gemini",
    val activeModel: String = "gemini-1.5-flash",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val streamSpeedMs: Int = 20
)

data class ProviderHealth(
    val providerName: String,
    val isAvailable: Boolean,
    val latencyMs: Long = 0,
    val message: String = "Ready"
)

data class TaskPlanStep(
    val stepIndex: Int,
    val description: String,
    val toolRequired: String? = null,
    val isCompleted: Boolean = false,
    val outputSummary: String? = null
)

data class TaskPlan(
    val goal: String,
    val steps: List<TaskPlanStep>,
    val estimatedComplexity: String = "Moderate",
    val isExecutable: Boolean = true
)
