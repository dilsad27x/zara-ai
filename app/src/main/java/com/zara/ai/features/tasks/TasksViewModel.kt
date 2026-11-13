package com.zara.ai.features.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.agent.ZaraAgentEngine
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.AgentTaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val agentTaskDao: AgentTaskDao,
    private val agentEngine: ZaraAgentEngine,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val allTasks: StateFlow<List<AgentTaskEntity>> =
        agentTaskDao.getAllTasks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isCreatingTask = MutableStateFlow(false)
    val isCreatingTask: StateFlow<Boolean> = _isCreatingTask.asStateFlow()

    fun launchNewAutonomousTask(title: String, prompt: String) {
        if (title.isBlank() || _isCreatingTask.value) return
        _isCreatingTask.value = true
        viewModelScope.launch {
            try {
                agentEngine.runAutonomousTask(title, prompt)
            } finally {
                _isCreatingTask.value = false
            }
        }
    }

    fun pauseTask(task: AgentTaskEntity) {
        viewModelScope.launch(ioDispatcher) {
            agentTaskDao.updateTask(task.copy(status = AgentTaskStatus.PENDING))
        }
    }

    fun resumeTask(task: AgentTaskEntity) {
        viewModelScope.launch(ioDispatcher) {
            agentTaskDao.updateTask(task.copy(status = AgentTaskStatus.RUNNING))
        }
    }

    fun cancelTask(task: AgentTaskEntity) {
        viewModelScope.launch(ioDispatcher) {
            agentTaskDao.updateTask(task.copy(status = AgentTaskStatus.CANCELLED))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(ioDispatcher) {
            agentTaskDao.deleteTaskById(taskId)
        }
    }
}
