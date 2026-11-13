package com.zara.ai.ai.android

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.widget.Toast
import com.zara.ai.ai.android.model.DeviceMetrics
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSystemBridge @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(ZaraDispatchers.Main) private val mainDispatcher: CoroutineDispatcher
) {
    suspend fun getDeviceMetrics(): DeviceMetrics = withContext(ioDispatcher) {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memoryInfo)

        DeviceMetrics(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            isWifiConnected = true,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            totalRamMb = memoryInfo.totalMem / (1024 * 1024),
            availableRamMb = memoryInfo.availMem / (1024 * 1024)
        )
    }

    suspend fun copyToClipboard(label: String, text: String) = withContext(mainDispatcher) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        logger.i("AndroidSystemBridge", "Copied to clipboard: $label")
    }

    suspend fun showToast(message: String) = withContext(mainDispatcher) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    suspend fun getVolumeLevel(): Int = withContext(ioDispatcher) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    suspend fun setVolumeLevel(level: Int) = withContext(ioDispatcher) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val clamped = level.coerceIn(0, max)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
    }
}
