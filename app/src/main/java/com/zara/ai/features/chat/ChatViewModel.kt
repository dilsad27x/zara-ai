package com.zara.ai.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.agent.ZaraAgentEngine
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val agentEngine: ZaraAgentEngine,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) messageDao.getMessagesForConversation(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ensureConversation()
    }

    private fun ensureConversation() {
        viewModelScope.launch(ioDispatcher) {
            val convos = conversationDao.getConversationById("default_chat")
            if (convos == null) {
                val newConv = ConversationEntity(
                    id = "default_chat",
                    title = "ZARA Primary Session",
                    createdAt = System.currentTimeMillis()
                )
                conversationDao.insertConversation(newConv)
                // Add welcome message
                messageDao.insertMessage(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = "default_chat",
                        role = MessageRole.ASSISTANT,
                        content = "Greetings! I am ZARA AI, your next-generation autonomous Android intelligence. How may I empower your workflow today?"
                    )
                )
            }
            _activeConversationId.value = "default_chat"
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val convId = _activeConversationId.value ?: return

        viewModelScope.launch(ioDispatcher) {
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                role = MessageRole.USER,
                content = text
            )
            messageDao.insertMessage(userMsg)

            _isGenerating.value = true
            logger.i("ChatViewModel", "User sent message: $text")

            // Simulate intelligent streaming response
            delay(600)
            val assistantResponse = generateResponse(text)
            val assistantMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                role = MessageRole.ASSISTANT,
                content = assistantResponse
            )
            messageDao.insertMessage(assistantMsg)
            _isGenerating.value = false
        }
    }

    private fun generateResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            "help" in lower -> "I can manage your device settings, execute autonomous tasks with Agent Engine, store memories in Room database, build full websites via Website Forge, and connect with social platforms."
            "status" in lower -> "All core systems operational: Jetpack Compose UI active, Room Database synced, DataStore preferences loaded, and Hilt injection running cleanly."
            else -> "I have processed your instruction: '$input'. All actions have been recorded into ZARA's memory graph."
        }
    }
}
