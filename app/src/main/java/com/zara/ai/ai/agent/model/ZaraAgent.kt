package com.zara.ai.ai.agent.model

import java.util.UUID

enum class AgentExecutionState {
    IDLE, PLANNING, EXECUTING, EVALUATING, PAUSED, COMPLETED, ERROR
}

data class AgentStep(
    val id: String = UUID.randomUUID().toString(),
    val stepIndex: Int,
    val thought: String,
    val toolName: String?,
    val toolInput: String?,
    val toolOutput: String? = null,
    val isCompleted: Boolean = false
)

data class AgentContext(
    val userId: String = "user_default",
    val activeConversationId: String? = null,
    val availableTools: List<String> = emptyList(),
    val memoryContext: Map<String, String> = emptyMap()
)

data class AgentExecutionResult(
    val taskId: String,
    val success: Boolean,
    val finalResponse: String,
    val steps: List<AgentStep>,
    val executionTimeMs: Long
)
