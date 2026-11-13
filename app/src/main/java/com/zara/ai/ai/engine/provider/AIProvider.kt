package com.zara.ai.ai.engine.provider

import com.zara.ai.ai.engine.model.AIRequest
import com.zara.ai.ai.engine.model.AIResponse
import com.zara.ai.ai.engine.model.AIStreamChunk
import com.zara.ai.ai.engine.model.ProviderHealth
import kotlinx.coroutines.flow.Flow

interface AIProvider {
    val name: String

    suspend fun chat(request: AIRequest): Result<AIResponse>

    fun streamChat(request: AIRequest): Flow<AIStreamChunk>

    suspend fun checkHealth(): ProviderHealth
}
