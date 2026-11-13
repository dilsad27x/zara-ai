package com.zara.ai.ai.integrations.model

data class ToolDefinition(
    val name: String,
    val description: String,
    val category: String,
    val parameters: Map<String, String>,
    val requiresPermission: Boolean = false,
    val execute: suspend (parameters: Map<String, Any?>) -> Result<String>
)
