package com.zara.ai.core.voice.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zara.ai.MainActivity
import com.zara.ai.R
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.voice.engine.VoiceEngine
import com.zara.ai.core.voice.model.VoiceSessionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground Service enabling continuous background voice interaction for ZARA AI.
 * Displays an ongoing notification with real-time status and quick action controls.
 */
@AndroidEntryPoint
class ZaraVoiceService : Service() {

    @Inject
    lateinit var voiceEngine: VoiceEngine

    private val logger = ZaraLogger("ZaraVoiceService")
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var stateObservationJob: Job? = null

    companion object {
        const val CHANNEL_ID = "zara_voice_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_VOICE = "com.zara.ai.action.START_VOICE"
        const val ACTION_STOP_VOICE = "com.zara.ai.action.STOP_VOICE"
        const val ACTION_TOGGLE_MUTE = "com.zara.ai.action.TOGGLE_MUTE"

        fun start(context: Context) {
            val intent = Intent(context, ZaraVoiceService::class.java).apply {
                action = ACTION_START_VOICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ZaraVoiceService::class.java).apply {
                action = ACTION_STOP_VOICE
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_VOICE -> {
                logger.d("Received STOP_VOICE action")
                voiceEngine.stopSession()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_MUTE -> {
                logger.d("Received TOGGLE_MUTE action")
                voiceEngine.toggleMute()
            }
            ACTION_START_VOICE, null -> {
                logger.d("Received START_VOICE action")
                startAsForeground()
                voiceEngine.startSession()
                observeVoiceState()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        logger.d("ZaraVoiceService destroyed")
        stateObservationJob?.cancel()
        voiceEngine.stopSession()
        super.onDestroy()
    }

    private fun startAsForeground() {
        val notification = buildNotification("Listening for voice...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observeVoiceState() {
        stateObservationJob?.cancel()
        stateObservationJob = serviceScope.launch {
            voiceEngine.sessionState.collect { state ->
                val statusText = when (state) {
                    VoiceSessionState.LISTENING -> "Listening to your voice..."
                    VoiceSessionState.USER_SPEAKING -> "Capturing speech..."
                    VoiceSessionState.PROCESSING -> "Thinking and reasoning..."
                    VoiceSessionState.SPEAKING -> "Speaking..."
                    VoiceSessionState.PAUSED -> "Microphone muted"
                    VoiceSessionState.ERROR -> "Voice error occurred"
                    else -> "Voice standby"
                }
                updateNotification(statusText)
            }
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ZaraVoiceService::class.java).apply {
            action = ACTION_STOP_VOICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ZARA Voice Assistant")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ZARA Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notifications for ZARA real-time conversational voice interaction"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
