package com.zara.ai.features.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MemoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryDao: MemoryDao,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<MemoryCategory?>(null)
    val selectedCategory: StateFlow<MemoryCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val memories: StateFlow<List<MemoryEntity>> = _selectedCategory
        .flatMapLatest { category ->
            if (category != null) {
                memoryDao.getMemoriesByCategory(category)
            } else {
                memoryDao.getAllMemories()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedInitialMemories()
    }

    private fun seedInitialMemories() {
        viewModelScope.launch(ioDispatcher) {
            val existing = memoryDao.getMemoryByKey("ai_identity")
            if (existing == null) {
                memoryDao.insertMemory(
                    MemoryEntity(
                        key = "ai_identity",
                        value = "ZARA: Autonomous Native Android Cognitive Assistant",
                        category = MemoryCategory.IDENTITY
                    )
                )
                memoryDao.insertMemory(
                    MemoryEntity(
                        key = "preferred_architecture",
                        value = "Jetpack Compose + Material 3 + Room + Hilt",
                        category = MemoryCategory.RULE
                    )
                )
                memoryDao.insertMemory(
                    MemoryEntity(
                        key = "security_protocol",
                        value = "Hardware Keystore AES-256 GCM encrypted",
                        category = MemoryCategory.SYSTEM_CAPABILITY
                    )
                )
            }
        }
    }

    fun setCategory(category: MemoryCategory?) {
        _selectedCategory.value = category
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
}
