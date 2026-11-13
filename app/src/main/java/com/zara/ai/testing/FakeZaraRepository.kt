package com.zara.ai.testing

import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MemoryEntity
import com.zara.ai.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeZaraRepository {

    private val conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    private val messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    private val memories = MutableStateFlow<List<MemoryEntity>>(emptyList())

    fun getAllConversations(): Flow<List<ConversationEntity>> = conversations.asStateFlow()

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
        messages.map { list -> list.filter { it.conversationId == conversationId } }

    fun getAllMemories(): Flow<List<MemoryEntity>> = memories.asStateFlow()

    suspend fun saveConversation(conversation: ConversationEntity) {
        conversations.value = conversations.value + conversation
    }

    suspend fun saveMessage(message: MessageEntity) {
        messages.value = messages.value + message
    }

    suspend fun saveMemory(memory: MemoryEntity) {
        memories.value = memories.value + memory
    }

    fun clear() {
        conversations.value = emptyList()
        messages.value = emptyList()
        memories.value = emptyList()
    }
}
