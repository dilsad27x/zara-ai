package com.zara.ai.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.common.lifecycle.AppLifecycleManager
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.designsystem.theme.ThemeManager
import com.zara.ai.core.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InitializationState {
    data object Initializing : InitializationState
    data class Ready(val sessionActive: Boolean) : InitializationState
    data class Error(val message: String) : InitializationState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val conversationDao: ConversationDao,
    private val securityManager: SecurityManager,
    private val themeManager: ThemeManager,
    private val lifecycleManager: AppLifecycleManager,
    private val logger: ZaraLogger
) : ViewModel() {

    private val _initState = MutableStateFlow<InitializationState>(InitializationState.Initializing)
    val initState: StateFlow<InitializationState> = _initState.asStateFlow()

    init {
        runInitialization()
    }

    private fun runInitialization() {
        viewModelScope.launch {
            try {
                logger.i("SplashViewModel", "Initializing ZARA AI core systems...")

                // 1. Verify ThemeManager DataStore
                themeManager.themeConfig.first()

                // 2. Verify Database readiness
                conversationDao.getConversationById("default_chat")

                // 3. Verify App Lifecycle Manager
                lifecycleManager.getUptimeMillis()

                logger.i("SplashViewModel", "All foundation subsystems ready.")
                _initState.value = InitializationState.Ready(sessionActive = true)
            } catch (e: Exception) {
                logger.e("SplashViewModel", "Initialization encountered failure", e)
                _initState.value = InitializationState.Error(
                    e.localizedMessage ?: "Subsystem verification failed"
                )
            }
        }
    }
}
