package com.zara.ai.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.android.AndroidSystemBridge
import com.zara.ai.ai.android.model.DeviceMetrics
import com.zara.ai.ai.engine.gateway.AIGateway
import com.zara.ai.ai.engine.router.ModelRouter
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.AgentTaskStatus
import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import com.zara.ai.core.designsystem.components.OrbState
import com.zara.ai.core.network.connectivity.NetworkConnectivityObserver
import com.zara.ai.core.network.model.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class HomeUiState(
    val greeting: String = "",
    val deviceMetrics: DeviceMetrics? = null,
    val networkStatus: NetworkStatus = NetworkStatus.Available,
    val aiProviderName: String = "Google Gemini (gemini-1.5-flash)",
    val orbState: OrbState = OrbState.IDLE,
    val quickActions: List<String> = listOf(
        "ZARA, phone ka battery aur RAM status batao",
        "Inspect Android device telemetry",
        "Save memory: My favorite programming language is Kotlin",
        "Summarize active knowledge memories"
    )
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val aiGateway: AIGateway,
    private val modelRouter: ModelRouter,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val agentTaskDao: AgentTaskDao,
    private val systemBridge: AndroidSystemBridge,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val logger: ZaraLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = computeGreeting()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val recentConversations: StateFlow<List<ConversationEntity>> =
        conversationDao.getAllConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<AgentTaskEntity>> =
        agentTaskDao.getTasksByStatus(AgentTaskStatus.RUNNING)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMetrics()
        observeNetwork()
        observeAiEngine()
    }

    private fun computeGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    private fun loadMetrics() {
        viewModelScope.launch {
            try {
                val metrics = systemBridge.getDeviceMetrics()
                _uiState.value = _uiState.value.copy(deviceMetrics = metrics)
            } catch (e: Exception) {
                logger.e("HomeViewModel", "Failed to retrieve device telemetry", e)
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _uiState.value = _uiState.value.copy(
                    networkStatus = status
                )
            }
        }
    }

    private fun observeAiEngine() {
        viewModelScope.launch {
            aiGateway.engineOrbState.collect { orbState ->
                _uiState.value = _uiState.value.copy(orbState = orbState)
            }
        }
        viewModelScope.launch {
            modelRouter.selectedModel.collect { modelName ->
                _uiState.value = _uiState.value.copy(
                    aiProviderName = "Google Gemini ($modelName)"
                )
            }
        }
    }

    fun sendQuickPrompt(prompt: String, onNavigateToChat: () -> Unit) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            val convId = "default_chat"
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                role = MessageRole.USER,
                content = prompt
            )
            messageDao.insertMessage(userMsg)
            onNavigateToChat()
        }
    }
}
