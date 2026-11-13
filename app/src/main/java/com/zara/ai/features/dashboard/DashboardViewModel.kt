package com.zara.ai.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.agent.ZaraAgentEngine
import com.zara.ai.core.common.lifecycle.AppLifecycleManager
import com.zara.ai.core.common.lifecycle.AppLifecycleState
import com.zara.ai.core.common.logger.LogEntry
import com.zara.ai.core.common.logger.LogLevel
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val logger: ZaraLogger,
    private val agentTaskDao: AgentTaskDao,
    private val agentEngine: ZaraAgentEngine,
    private val lifecycleManager: AppLifecycleManager
) : ViewModel() {

    val logs: StateFlow<List<LogEntry>> = logger.logsState

    val tasks: StateFlow<List<AgentTaskEntity>> = agentTaskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lifecycleState: StateFlow<AppLifecycleState> = lifecycleManager.lifecycleState

    private val _isExecutingAgent = MutableStateFlow(false)
    val isExecutingAgent: StateFlow<Boolean> = _isExecutingAgent.asStateFlow()

    fun runDiagnosticTask() {
        if (_isExecutingAgent.value) return
        _isExecutingAgent.value = true
        viewModelScope.launch {
            try {
                agentEngine.runAutonomousTask(
                    title = "System Diagnostics & Security Audit",
                    prompt = "Verify Room schemas, Keystore crypto engine, and Android system connectivity."
                )
            } finally {
                _isExecutingAgent.value = false
            }
        }
    }

    fun clearLogs() {
        logger.clearLogs()
    }
}
