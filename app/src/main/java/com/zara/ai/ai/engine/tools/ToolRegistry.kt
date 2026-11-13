package com.zara.ai.ai.engine.tools

import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val logger: ZaraLogger
) {
    private val toolsMap = ConcurrentHashMap<String, ZaraTool>()
    private val _toolsState = MutableStateFlow<List<ZaraTool>>(emptyList())
    val toolsState: StateFlow<List<ZaraTool>> = _toolsState.asStateFlow()

    fun register(tool: ZaraTool) {
        toolsMap[tool.name] = tool
        _toolsState.value = toolsMap.values.toList()
        logger.i("ToolRegistry", "Registered tool: ${tool.name} [${tool.category}]")
    }

    fun unregister(toolName: String) {
        toolsMap.remove(toolName)
        _toolsState.value = toolsMap.values.toList()
        logger.i("ToolRegistry", "Unregistered tool: $toolName")
    }

    fun getTool(name: String): ZaraTool? = toolsMap[name]

    fun getAllTools(): List<ZaraTool> = toolsMap.values.toList()

    /**
     * Builds the Google Gemini functionDeclarations JSON array for all registered tools.
     */
    fun buildGeminiToolsJson(): JSONArray {
        val functionDeclarations = JSONArray()
        for (tool in toolsMap.values) {
            functionDeclarations.put(tool.toGeminiFunctionDeclaration())
        }
        return functionDeclarations
    }
}
