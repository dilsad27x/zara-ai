package com.zara.ai.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.android.AndroidSystemBridge
import com.zara.ai.ai.android.model.DeviceMetrics
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.ConversationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val deviceMetrics: DeviceMetrics? = null,
    val quickPrompts: List<String> = listOf(
        "Analyze my daily schedule & prioritize tasks",
        "Generate a responsive landing page with Website Forge",
        "Inspect Android device memory & optimize background processes",
        "Summarize conversations and extract new memories"
    )
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conversationDao: ConversationDao,
    private val agentTaskDao: AgentTaskDao,
    private val memoryDao: MemoryDao,
    private val systemBridge: AndroidSystemBridge,
    private val logger: ZaraLogger
) : ViewModel() {

    val recentConversations: StateFlow<List<ConversationEntity>> =
        conversationDao.getAllConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<AgentTaskEntity>> =
        agentTaskDao.getAllTasks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMetrics()
    }

    fun loadMetrics() {
        viewModelScope.launch {
            try {
                val metrics = systemBridge.getDeviceMetrics()
                _uiState.value = _uiState.value.copy(deviceMetrics = metrics)
                logger.d("HomeViewModel", "Loaded device metrics: ${metrics.deviceModel}")
            } catch (e: Exception) {
                logger.e("HomeViewModel", "Failed to load device metrics", e)
            }
        }
    }
}
