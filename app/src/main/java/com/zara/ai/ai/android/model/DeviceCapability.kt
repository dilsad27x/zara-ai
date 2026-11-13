package com.zara.ai.ai.android.model

data class DeviceMetrics(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isWifiConnected: Boolean,
    val osVersion: String,
    val deviceModel: String,
    val totalRamMb: Long,
    val availableRamMb: Long
)
