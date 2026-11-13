package com.zara.ai.features.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MemoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryStats(
    val shortTermContextCount: Int = 0,
    val longTermMemoryCount: Int = 0,
    val factCount: Int = 0,
    val preferenceCount: Int = 0
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryDao: MemoryDao,
    private val messageDao: MessageDao,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<MemoryCategory?>(null)
    val selectedCategory: StateFlow<MemoryCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _autoExtractEnabled = MutableStateFlow(true)
    val autoExtractEnabled: StateFlow<Boolean> = _autoExtractEnabled.asStateFlow()

    val memories: StateFlow<List<MemoryEntity>> = combine(
        _selectedCategory.flatMapLatest { cat ->
            if (cat != null) memoryDao.getMemoriesByCategory(cat) else memoryDao.getAllMemories()
        },
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.key.contains(query, ignoreCase = true) || it.value.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryStats: StateFlow<MemoryStats> = memoryDao.getAllMemories().combine(messageDao.getTotalMessageCount()) { memList, msgCount ->
        MemoryStats(
            shortTermContextCount = msgCount,
            longTermMemoryCount = memList.size,
            factCount = memList.count { it.category == MemoryCategory.FACT },
            preferenceCount = memList.count { it.category == MemoryCategory.PREFERENCE }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MemoryStats())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: MemoryCategory?) {
        _selectedCategory.value = category
    }

    fun toggleAutoExtract() {
        _autoExtractEnabled.value = !_autoExtractEnabled.value
    }

    fun addMemory(key: String, value: String, category: MemoryCategory) {
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch(ioDispatcher) {
            memoryDao.insertMemory(
                MemoryEntity(
                    key = key.trim(),
                    value = value.trim(),
                    category = category
                )
            )
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch(ioDispatcher) {
            memoryDao.deleteMemoryById(id)
        }
    }

    fun clearShortTermConversationMemory() {
        viewModelScope.launch(ioDispatcher) {
            messageDao.deleteMessagesByConversationId("default_chat")
        }
    }
}
