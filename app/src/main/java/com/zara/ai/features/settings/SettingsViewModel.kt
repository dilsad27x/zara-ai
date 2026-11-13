package com.zara.ai.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.BuildConfig
import com.zara.ai.core.designsystem.theme.ThemeConfig
import com.zara.ai.core.designsystem.theme.ThemeManager
import com.zara.ai.core.designsystem.theme.ThemeMode
import com.zara.ai.core.security.SecurityManager
import com.zara.ai.core.security.biometric.BiometricAuthManager
import com.zara.ai.core.security.biometric.BiometricStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKeyMasked: String = "••••••••••••••••",
    val biometricStatus: BiometricStatus = BiometricStatus.AVAILABLE,
    val buildVariant: String = "${BuildConfig.BUILD_TYPE} (v${BuildConfig.VERSION_NAME})",
    val apiEndpoint: String = BuildConfig.BASE_URL
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val securityManager: SecurityManager,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = themeManager.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeConfig())

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val bioStatus = biometricAuthManager.checkBiometricStatus()
        val key = securityManager.getSecureString("master_api_key")
        val masked = if (!key.isNullOrEmpty()) {
            key.take(4) + "••••••••" + key.takeLast(4)
        } else {
            "Not configured"
        }
        _uiState.value = _uiState.value.copy(
            biometricStatus = bioStatus,
            apiKeyMasked = masked
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDynamicColor(enabled)
        }
    }

    fun saveApiKey(rawKey: String) {
        if (rawKey.isNotBlank()) {
            securityManager.storeSecureString("master_api_key", rawKey.trim())
            loadSettings()
        }
    }
}
