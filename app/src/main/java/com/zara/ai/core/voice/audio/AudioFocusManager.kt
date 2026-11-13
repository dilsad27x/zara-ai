package com.zara.ai.core.voice.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.zara.ai.core.common.logger.ZaraLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Focus status reported by AudioManager.
 */
enum class FocusStatus {
    GAINED,
    LOST,
    LOST_TRANSIENT,
    DUCKED
}

/**
 * Manages audio focus for seamless coexistence with other media players,
 * phone calls, and navigation apps.
 */
@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val logger = ZaraLogger("AudioFocusManager")

    private val _focusState = MutableStateFlow(FocusStatus.LOST)
    val focusState: StateFlow<FocusStatus> = _focusState.asStateFlow()

    private var audioFocusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                logger.d("Audio focus gained")
                _focusState.value = FocusStatus.GAINED
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                logger.d("Audio focus lost permanently")
                _focusState.value = FocusStatus.LOST
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                logger.d("Audio focus lost transiently")
                _focusState.value = FocusStatus.LOST_TRANSIENT
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                logger.d("Audio focus lost transiently can duck")
                _focusState.value = FocusStatus.DUCKED
            }
        }
    }

    /**
     * Request transient audio focus for voice assistant conversation.
     */
    fun requestFocus(): Boolean {
        return try {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build()

                audioFocusRequest = request
                audioManager.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
                )
            }

            val gained = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (gained) {
                _focusState.value = FocusStatus.GAINED
                logger.d("Audio focus successfully acquired")
            } else {
                logger.w("Audio focus request denied: $result")
            }
            gained
        } catch (e: Exception) {
            logger.e("Error requesting audio focus: ${e.message}")
            false
        }
    }

    /**
     * Abandon audio focus and restore normal media playback for other apps.
     */
    fun abandonFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    audioManager.abandonAudioFocusRequest(request)
                    audioFocusRequest = null
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(focusChangeListener)
            }
            _focusState.value = FocusStatus.LOST
            logger.d("Audio focus abandoned")
        } catch (e: Exception) {
            logger.e("Error abandoning audio focus: ${e.message}")
        }
    }
}
