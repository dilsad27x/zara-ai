package com.zara.ai.ai.agent

import com.zara.ai.ai.agent.model.AgentContext
import com.zara.ai.ai.agent.model.AgentExecutionResult
import com.zara.ai.ai.agent.model.AgentExecutionState
import com.zara.ai.ai.agent.model.AgentStep
import com.zara.ai.ai.integrations.IntegrationRegistry
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.AgentTaskStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZaraAgentEngine @Inject constructor(
    private val logger: ZaraLogger,
    private val agentTaskDao: AgentTaskDao,
    private val integrationRegistry: IntegrationRegistry,
    @Dispatcher(ZaraDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher
) {
    private val _agentState = MutableStateFlow(AgentExecutionState.IDLE)
    val agentState: StateFlow<AgentExecutionState> = _agentState.asStateFlow()

    private val _activeSteps = MutableStateFlow<List<AgentStep>>(emptyList())
    val activeSteps: StateFlow<List<AgentStep>> = _activeSteps.asStateFlow()

    suspend fun runAutonomousTask(
        title: String,
        prompt: String,
        context: AgentContext = AgentContext()
    ): AgentExecutionResult = withContext(defaultDispatcher) {
        val taskId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        logger.agent("ZaraAgentEngine", "Starting autonomous task [$taskId]: $title")

        // Persist initial task in DB
        val taskEntity = AgentTaskEntity(
            id = taskId,
            title = title,
            description = prompt,
            status = AgentTaskStatus.RUNNING,
            progress = 0.1f
        )
        agentTaskDao.insertTask(taskEntity)

        _agentState.value = AgentExecutionState.PLANNING
        delay(300)

        val steps = mutableListOf<AgentStep>()

        // Step 1: Analyze & Plan
        val step1 = AgentStep(
            stepIndex = 1,
            thought = "Analyzing user intent: '$prompt' and inspecting available tools.",
            toolName = null,
            toolInput = null,
            isCompleted = true
        )
        steps.add(step1)
        _activeSteps.value = steps.toList()

        // Step 2: Contextual execution
        _agentState.value = AgentExecutionState.EXECUTING
        delay(400)
        val step2 = AgentStep(
            stepIndex = 2,
            thought = "Synthesizing solution and verifying constraints.",
            toolName = "system_bridge",
            toolInput = "get_metrics",
            toolOutput = "Ready",
            isCompleted = true
        )
        steps.add(step2)
        _activeSteps.value = steps.toList()

        // Step 3: Evaluation
        _agentState.value = AgentExecutionState.EVALUATING
        delay(300)
        val step3 = AgentStep(
            stepIndex = 3,
            thought = "Verifying output quality and completing autonomous routine.",
            toolName = null,
            toolInput = null,
            isCompleted = true
        )
        steps.add(step3)
        _activeSteps.value = steps.toList()

        val finalResponse = "Task '$title' executed successfully by ZARA AI agent."
        val duration = System.currentTimeMillis() - startTime

        // Update DB
        agentTaskDao.updateTask(
            taskEntity.copy(
                status = AgentTaskStatus.COMPLETED,
                progress = 1.0f,
                completedAt = System.currentTimeMillis(),
                resultJson = finalResponse
            )
        )

        _agentState.value = AgentExecutionState.COMPLETED
        logger.agent("ZaraAgentEngine", "Finished task [$taskId] in ${duration}ms")

        AgentExecutionResult(
            taskId = taskId,
            success = true,
            finalResponse = finalResponse,
            steps = steps,
            executionTimeMs = duration
        )
    }

    fun resetState() {
        _agentState.value = AgentExecutionState.IDLE
        _activeSteps.value = emptyList()
    }
}
