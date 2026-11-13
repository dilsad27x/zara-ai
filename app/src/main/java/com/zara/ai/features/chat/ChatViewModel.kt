package com.zara.ai.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                        content = "Greetings. I am ZARA, your native autonomous intelligence.\n\nYou can issue commands, ask for code, or explore device integrations. Try asking me:\n```kotlin\nval agent = ZaraAI.initialize()\nagent.execute(goal = \"Build landing page\")\n```"
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

            // Stream response
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

            val fullResponse = generateIntelligentResponse(prompt)
            val chunks = fullResponse.split(" ")

            val stringBuilder = StringBuilder()
            for (chunk in chunks) {
                stringBuilder.append(chunk).append(" ")
                _streamingMessageText.value = stringBuilder.toString()
                delay(40) // Realistic token stream cadence
            }

            // Commit complete response to Room DB
            messageDao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = MessageRole.ASSISTANT,
                    content = fullResponse
                )
            )

            _streamingMessageText.value = ""
            _isGenerating.value = false
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
        _isGenerating.value = false
    }

    private fun generateIntelligentResponse(input: String): String {
        val query = input.lowercase()
        return when {
            "website" in query || "forge" in query ->
                "### Website Forge Autonomous Routine\n\nI have initialized the Website Forge generator for your request:\n```html\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"UTF-8\">\n  <title>ZARA AI Portal</title>\n</head>\n<body style=\"background:#080810;color:#FFF;\">\n  <h1>Autonomous Mobile Platform</h1>\n</body>\n</html>\n```\nYou can inspect this in the **Website Forge** dashboard."

            "device" in query || "battery" in query || "volume" in query ->
                "### Device Control Status\n\nAndroid hardware bridge is actively monitoring telemetry. Current streams are mapped to Android system services:\n- **AudioManager**: Audio volume sync\n- **BatteryManager**: Voltage and charging states\n- **Clipboard**: Direct clipboard bridge"

            "task" in query || "agent" in query ->
                "### Autonomous Agent Subsystem\n\nAgent engine is loaded with **TaskPlanner** and **ToolRegistry**. Tasks persist in Room database with state rollback capabilities."

            else ->
                "### Intelligence Synthesis\n\nI have processed your instruction: **\"$input\"**.\n\nAll session events are saved to the persistent Room database under conversation ID `${_activeConversationId.value}`."
        }
    }
}
