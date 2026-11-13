package com.zara.ai.ai.engine.tools

import org.json.JSONArray
import org.json.JSONObject

enum class ToolParameterType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY
}

data class ToolParameter(
    val name: String,
    val type: ToolParameterType,
    val description: String,
    val isRequired: Boolean = true,
    val enumValues: List<String> = emptyList()
)

data class ToolExecutionResult(
    val success: Boolean,
    val output: String,
    val errorMessage: String? = null,
    val executionTimeMs: Long = 0
)

data class ZaraTool(
    val name: String,
    val description: String,
    val category: String,
    val parameters: List<ToolParameter> = emptyList(),
    val requiresPermission: Boolean = false,
    val permissionName: String? = null,
    val execute: suspend (args: Map<String, Any?>) -> ToolExecutionResult
) {
    /**
     * Converts this tool definition into the official Google Gemini function declaration JSON schema.
     */
    fun toGeminiFunctionDeclaration(): JSONObject {
        val root = JSONObject()
        root.put("name", name)
        root.put("description", description)

        val parametersObj = JSONObject()
        parametersObj.put("type", "object")

        val propertiesObj = JSONObject()
        val requiredList = JSONArray()

        for (param in parameters) {
            val paramObj = JSONObject()
            paramObj.put("type", param.type.name.lowercase())
            paramObj.put("description", param.description)

            if (param.enumValues.isNotEmpty()) {
                val enumArray = JSONArray()
                param.enumValues.forEach { enumArray.put(it) }
                paramObj.put("enum", enumArray)
            }

            propertiesObj.put(param.name, paramObj)
            if (param.isRequired) {
                requiredList.put(param.name)
            }
        }

        parametersObj.put("properties", propertiesObj)
        if (requiredList.length() > 0) {
            parametersObj.put("required", requiredList)
        }

        root.put("parameters", parametersObj)
        return root
    }
}
