package com.zara.ai.core.voice.provider

import com.zara.ai.core.voice.model.AudioFrame
import com.zara.ai.core.voice.model.VoiceConfiguration
import com.zara.ai.core.voice.model.VoiceEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Common contract for real-time bidirectional voice AI providers.
 * Supports streaming audio frames, streaming text transcripts, speech activity events,
 * and conversational interruptions.
 */
interface RealtimeVoiceProvider {

    /**
     * Observable connection state.
     */
    val isConnected: StateFlow<Boolean>

    /**
     * Stream of voice events emitted by the provider (transcripts, audio chunks, errors).
     */
    val events: SharedFlow<VoiceEvent>

    /**
     * Establish real-time connection with provider.
     */
    suspend fun connect(config: VoiceConfiguration): Result<Unit>

    /**
     * Terminate active real-time connection.
     */
    suspend fun disconnect()

    /**
     * Stream a raw audio frame from the microphone to the provider.
     */
    suspend fun sendAudioFrame(frame: AudioFrame)

    /**
     * Transmit text prompt or context directly to the active voice session.
     */
    suspend fun sendText(text: String)

    /**
     * Instantly interrupt ongoing assistant audio generation.
     */
    fun interrupt()
}
