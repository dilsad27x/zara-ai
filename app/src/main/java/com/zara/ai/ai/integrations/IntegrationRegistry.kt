package com.zara.ai.ai.integrations

import com.zara.ai.ai.integrations.model.ToolDefinition
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrationRegistry @Inject constructor(
    private val logger: ZaraLogger
) {
    private val toolsMap = ConcurrentHashMap<String, ToolDefinition>()
    private val _registeredTools = MutableStateFlow<List<ToolDefinition>>(emptyList())
    val registeredTools: StateFlow<List<ToolDefinition>> = _registeredTools.asStateFlow()

    fun registerTool(tool: ToolDefinition) {
        toolsMap[tool.name] = tool
        _registeredTools.value = toolsMap.values.toList()
        logger.i("IntegrationRegistry", "Registered AI tool: ${tool.name} [${tool.category}]")
    }

    fun unregisterTool(name: String) {
        toolsMap.remove(name)
        _registeredTools.value = toolsMap.values.toList()
        logger.i("IntegrationRegistry", "Unregistered AI tool: $name")
    }

    fun getTool(name: String): ToolDefinition? = toolsMap[name]

    suspend fun executeTool(name: String, params: Map<String, Any?>): Result<String> {
        val tool = toolsMap[name] ?: return Result.failure(IllegalArgumentException("Tool not found: $name"))
        logger.agent("IntegrationRegistry", "Executing tool: $name with params: $params")
        return tool.execute(params)
    }
}
