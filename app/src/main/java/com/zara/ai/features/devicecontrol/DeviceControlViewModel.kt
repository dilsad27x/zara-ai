package com.zara.ai.features.devicecontrol

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.android.AndroidSystemBridge
import com.zara.ai.ai.android.model.DeviceMetrics
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.permissions.PermissionManager
import com.zara.ai.core.permissions.model.ZaraPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceControlUiState(
    val deviceMetrics: DeviceMetrics? = null,
    val isAccessibilityEnabled: Boolean = false,
    val isNotificationAccessEnabled: Boolean = false,
    val isScreenCaptureAuthorized: Boolean = false, // Part 03 requirement
    val isOverlayGranted: Boolean = false,
    val currentVolume: Int = 0,
    val maxVolume: Int = 15,
    val quickLaunchApps: List<Pair<String, String>> = listOf(
        "Settings" to "com.android.settings",
        "Files" to "com.google.android.documentsui",
        "Chrome" to "com.android.chrome"
    )
)

@HiltViewModel
class DeviceControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemBridge: AndroidSystemBridge,
    private val permissionManager: PermissionManager,
    private val agentTaskDao: AgentTaskDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceControlUiState())
    val uiState: StateFlow<DeviceControlUiState> = _uiState.asStateFlow()

    val taskHistory: StateFlow<List<AgentTaskEntity>> =
        agentTaskDao.getAllTasks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshState()
    }

    fun refreshState() {
        viewModelScope.launch {
            val metrics = systemBridge.getDeviceMetrics()
            val volume = systemBridge.getVolumeLevel()
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            val accessibility = permissionManager.isPermissionGranted(ZaraPermission.ACCESSIBILITY_SERVICE)
            val notificationAccess = permissionManager.isPermissionGranted(ZaraPermission.NOTIFICATION_LISTENER)
            val overlay = permissionManager.isPermissionGranted(ZaraPermission.SYSTEM_ALERT_WINDOW)

            _uiState.value = _uiState.value.copy(
                deviceMetrics = metrics,
                isAccessibilityEnabled = accessibility,
                isNotificationAccessEnabled = notificationAccess,
                isOverlayGranted = overlay,
                currentVolume = volume,
                maxVolume = maxVol
            )
        }
    }

    fun setVolume(level: Int) {
        viewModelScope.launch {
            systemBridge.setVolumeLevel(level)
            _uiState.value = _uiState.value.copy(currentVolume = level)
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    systemBridge.showToast("Application not found on this device: $packageName")
                }
            } catch (e: Exception) {
                systemBridge.showToast("Cannot launch: ${e.localizedMessage}")
            }
        }
    }

    fun copyDiagnostics() {
        val m = _uiState.value.deviceMetrics ?: return
        viewModelScope.launch {
            val report = "ZARA AI Hardware Telemetry\nModel: ${m.deviceModel}\nOS: ${m.osVersion}\nBattery: ${m.batteryLevel}% (Charging: ${m.isCharging})\nRAM: ${m.availableRamMb}MB/${m.totalRamMb}MB\nAccessibility: ${_uiState.value.isAccessibilityEnabled}\nOverlay: ${_uiState.value.isOverlayGranted}"
            systemBridge.copyToClipboard("Zara Diagnostics", report)
            systemBridge.showToast("System diagnostic report copied to clipboard")
        }
    }
}
