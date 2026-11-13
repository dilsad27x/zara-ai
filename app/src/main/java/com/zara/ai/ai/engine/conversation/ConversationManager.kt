package com.zara.ai.ai.engine.conversation

import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationManager @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun createNewConversation(customTitle: String? = null): String = withContext(ioDispatcher) {
        val id = UUID.randomUUID().toString()
        val title = customTitle ?: "Session ${System.currentTimeMillis().toString().takeLast(4)}"
        val entity = ConversationEntity(
            id = id,
            title = title,
            createdAt = System.currentTimeMillis()
        )
        conversationDao.insertConversation(entity)
        logger.i("ConversationManager", "Created conversation [$id]: $title")
        id
    }

    suspend fun saveMessage(
        conversationId: String,
        role: MessageRole,
        content: String,
        tokenCount: Int = 0
    ): MessageEntity = withContext(ioDispatcher) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            content = content,
            timestamp = System.currentTimeMillis(),
            tokenCount = tokenCount
        )
        messageDao.insertMessage(message)
        message
    }

    suspend fun deleteConversation(conversationId: String) = withContext(ioDispatcher) {
        messageDao.deleteMessagesByConversationId(conversationId)
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            conversationDao.deleteConversation(conv)
        }
        logger.i("ConversationManager", "Deleted conversation: $conversationId")
    }

    suspend fun updateConversationTitle(conversationId: String, newTitle: String) = withContext(ioDispatcher) {
        val existing = conversationDao.getConversationById(conversationId)
        if (existing != null) {
            conversationDao.updateConversation(existing.copy(title = newTitle))
        }
    }
}
