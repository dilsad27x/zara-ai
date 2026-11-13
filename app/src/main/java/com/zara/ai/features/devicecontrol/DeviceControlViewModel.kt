package com.zara.ai.features.devicecontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.android.AndroidSystemBridge
import com.zara.ai.ai.android.model.DeviceMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceControlViewModel @Inject constructor(
    private val systemBridge: AndroidSystemBridge
) : ViewModel() {

    private val _deviceMetrics = MutableStateFlow<DeviceMetrics?>(null)
    val deviceMetrics: StateFlow<DeviceMetrics?> = _deviceMetrics.asStateFlow()

    private val _currentVolume = MutableStateFlow(0)
    val currentVolume: StateFlow<Int> = _currentVolume.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _deviceMetrics.value = systemBridge.getDeviceMetrics()
            _currentVolume.value = systemBridge.getVolumeLevel()
        }
    }

    fun setVolume(volume: Int) {
        viewModelScope.launch {
            systemBridge.setVolumeLevel(volume)
            _currentVolume.value = volume
        }
    }

    fun copyDiagnostics() {
        val metrics = _deviceMetrics.value ?: return
        viewModelScope.launch {
            val diag = "ZARA AI Diagnostic\nModel: ${metrics.deviceModel}\nOS: ${metrics.osVersion}\nBattery: ${metrics.batteryLevel}%\nRAM: ${metrics.availableRamMb}MB/${metrics.totalRamMb}MB"
            systemBridge.copyToClipboard("Zara Diagnostics", diag)
            systemBridge.showToast("Diagnostics copied to clipboard")
        }
    }
}
