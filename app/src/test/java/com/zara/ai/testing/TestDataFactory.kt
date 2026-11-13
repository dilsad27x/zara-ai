package com.zara.ai.testing

import com.zara.ai.ai.agent.model.AgentExecutionResult
import com.zara.ai.ai.agent.model.AgentStep
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.AgentTaskStatus
import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MemoryEntity
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import java.util.UUID

object TestDataFactory {

    fun createConversation(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Conversation"
    ): ConversationEntity = ConversationEntity(
        id = id,
        title = title,
        createdAt = System.currentTimeMillis()
    )

    fun createMessage(
        conversationId: String,
        content: String = "Test Message",
        role: MessageRole = MessageRole.USER
    ): MessageEntity = MessageEntity(
        id = UUID.randomUUID().toString(),
        conversationId = conversationId,
        role = role,
        content = content,
        timestamp = System.currentTimeMillis()
    )

    fun createMemory(
        key: String = "test_key",
        value: String = "test_value",
        category: MemoryCategory = MemoryCategory.FACT
    ): MemoryEntity = MemoryEntity(
        id = UUID.randomUUID().toString(),
        key = key,
        value = value,
        category = category
    )

    fun createAgentTask(
        title: String = "Test Autonomous Task",
        status: AgentTaskStatus = AgentTaskStatus.COMPLETED
    ): AgentTaskEntity = AgentTaskEntity(
        id = UUID.randomUUID().toString(),
        title = title,
        description = "Task description for testing",
        status = status,
        progress = 1.0f
    )
}
