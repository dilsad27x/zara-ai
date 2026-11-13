package com.zara.ai.core.voice.interruption

import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.voice.audio.AudioPlaybackManager
import com.zara.ai.core.voice.model.VoiceEvent
import com.zara.ai.core.voice.model.VoiceSessionState
import com.zara.ai.core.voice.provider.RealtimeVoiceProvider
import com.zara.ai.core.voice.provider.tts.AndroidTtsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller responsible for conversational barge-in (user speech interruption).
 * Instantly terminates active assistant audio, flushes playback pipelines,
 * and notifies active voice providers when the user starts speaking over the assistant.
 */
@Singleton
class VoiceInterruptionController @Inject constructor(
    private val audioPlaybackManager: AudioPlaybackManager,
    private val ttsManager: AndroidTtsManager
) {
    private val logger = ZaraLogger("VoiceInterruptionController")

    private val _interruptionEvents = MutableSharedFlow<VoiceEvent.Interrupted>(extraBufferCapacity = 16)
    val interruptionEvents: SharedFlow<VoiceEvent.Interrupted> = _interruptionEvents.asSharedFlow()

    private var realtimeProvider: RealtimeVoiceProvider? = null

    fun attachRealtimeProvider(provider: RealtimeVoiceProvider?) {
        this.realtimeProvider = provider
    }

    /**
     * Evaluates whether an incoming speech event should trigger conversational barge-in.
     * Returns true if interruption was executed.
     */
    fun onUserSpeechDetected(currentState: VoiceSessionState): Boolean {
        val isAssistantActive = currentState == VoiceSessionState.SPEAKING ||
                audioPlaybackManager.isPlaying.value ||
                ttsManager.isSpeaking.value

        if (isAssistantActive) {
            logger.i("Barge-in triggered: user interrupted assistant speech")
            executeInterruption()
            return true
        }
        return false
    }

    /**
     * Instantly halt assistant audio playback, clear synthesizer queues,
     * signal the AI provider, and emit the Interrupted event.
     */
    fun executeInterruption() {
        logger.d("Executing conversational speech interruption")

        // 1. Immediately flush streaming AudioTrack
        audioPlaybackManager.flushAndHalt()

        // 2. Immediately stop native Android TTS
        ttsManager.stop()

        // 3. Signal Gemini Live / realtime WebSocket
        realtimeProvider?.interrupt()

        // 4. Emit event to listeners
        _interruptionEvents.tryEmit(VoiceEvent.Interrupted)
    }
}
