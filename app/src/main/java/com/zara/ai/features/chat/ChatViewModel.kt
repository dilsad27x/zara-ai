package com.zara.ai.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.engine.gateway.AIGateway
import com.zara.ai.ai.engine.gateway.GatewayEvent
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import com.zara.ai.core.network.connectivity.NetworkConnectivityObserver
import com.zara.ai.core.network.model.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiGateway: AIGateway,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _activeConversationId = MutableStateFlow("default_chat")
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingMessageText = MutableStateFlow("")
    val streamingMessageText: StateFlow<String> = _streamingMessageText.asStateFlow()

    private val _toolExecutionStatus = MutableStateFlow<String?>(null)
    val toolExecutionStatus: StateFlow<String?> = _toolExecutionStatus.asStateFlow()

    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private var activeGenerationJob: Job? = null

    val allConversations: StateFlow<List<ConversationEntity>> =
        conversationDao.getAllConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id -> messageDao.getMessagesForConversation(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ensureDefaultSession()
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            connectivityObserver.observe().collect {
                _networkStatus.value = it
            }
        }
    }

    private fun ensureDefaultSession() {
        viewModelScope.launch(ioDispatcher) {
            val existing = conversationDao.getConversationById("default_chat")
            if (existing == null) {
                conversationDao.insertConversation(
                    ConversationEntity(
                        id = "default_chat",
                        title = "Primary Intelligence Session"
                    )
                )
                messageDao.insertMessage(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = "default_chat",
                        role = MessageRole.ASSISTANT,
                        content = "Greetings. I am ZARA, your sovereign AI intelligence engine connected to Google Gemini.\n\nI support English, Hindi (हिन्दी), and Hinglish naturally. Ask me questions, request device telemetry, manage audio volume, or plan autonomous tasks."
                    )
                )
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch(ioDispatcher) {
            val newId = UUID.randomUUID().toString()
            val newSession = ConversationEntity(
                id = newId,
                title = "Session ${System.currentTimeMillis().toString().takeLast(4)}"
            )
            conversationDao.insertConversation(newSession)
            _activeConversationId.value = newId
        }
    }

    fun switchConversation(id: String) {
        _activeConversationId.value = id
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return
        val convId = _activeConversationId.value

        viewModelScope.launch(ioDispatcher) {
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                role = MessageRole.USER,
                content = text
            )
            messageDao.insertMessage(userMsg)

            // Stream response through AIGateway
            streamAssistantResponse(text, convId)
        }
    }

    fun regenerateLatestResponse() {
        val currentMessages = messages.value
        val lastUserMsg = currentMessages.lastOrNull { it.role == MessageRole.USER } ?: return
        val convId = _activeConversationId.value

        viewModelScope.launch(ioDispatcher) {
            streamAssistantResponse(lastUserMsg.content, convId)
        }
    }

    private fun streamAssistantResponse(prompt: String, conversationId: String) {
        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch(ioDispatcher) {
            _isGenerating.value = true
            _streamingMessageText.value = ""
            _toolExecutionStatus.value = null

            val accumulated = StringBuilder()

            try {
                aiGateway.processConversationStream(conversationId, prompt).collect { event ->
                    when (event) {
                        is GatewayEvent.TextChunk -> {
                            accumulated.append(event.text)
                            _streamingMessageText.value = accumulated.toString()
                        }
                        is GatewayEvent.ToolExecuting -> {
                            _toolExecutionStatus.value = "Executing tool: ${event.toolName}..."
                        }
                        is GatewayEvent.ToolCompleted -> {
                            _toolExecutionStatus.value = null
                        }
                        is GatewayEvent.Finished -> {
                            val finalText = accumulated.toString()
                            if (finalText.isNotBlank()) {
                                messageDao.insertMessage(
                                    MessageEntity(
                                        id = UUID.randomUUID().toString(),
                                        conversationId = conversationId,
                                        role = MessageRole.ASSISTANT,
                                        content = finalText,
                                        tokenCount = event.tokenUsage?.totalTokens ?: 0
                                    )
                                )
                            }
                            _streamingMessageText.value = ""
                            _toolExecutionStatus.value = null
                            _isGenerating.value = false
                        }
                        is GatewayEvent.Error -> {
                            accumulated.append("\n\n⚠️ ${event.message}")
                            _streamingMessageText.value = accumulated.toString()
                            _toolExecutionStatus.value = null
                            _isGenerating.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                logger.e("ChatViewModel", "Streaming error", e)
                val fallbackText = accumulated.toString().ifBlank {
                    "⚠️ Connection to Gemini API failed: ${e.localizedMessage ?: "Unknown error"}. Please verify your API key in Settings."
                }
                messageDao.insertMessage(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversationId,
                        role = MessageRole.ASSISTANT,
                        content = fallbackText
                    )
                )
                _streamingMessageText.value = ""
                _toolExecutionStatus.value = null
                _isGenerating.value = false
            }
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        val currentText = _streamingMessageText.value
        if (currentText.isNotBlank()) {
            viewModelScope.launch(ioDispatcher) {
                messageDao.insertMessage(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = _activeConversationId.value,
                        role = MessageRole.ASSISTANT,
                        content = "$currentText\n\n*(Generation stopped by user)*"
                    )
                )
            }
        }
        _streamingMessageText.value = ""
        _toolExecutionStatus.value = null
        _isGenerating.value = false
    }
}
