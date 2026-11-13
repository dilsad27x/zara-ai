package com.zara.ai.core.voice.model

/**
 * State representing the active lifecycle of a ZARA voice assistant session.
 */
enum class VoiceSessionState {
    IDLE,
    INITIALIZING,
    LISTENING,
    USER_SPEAKING,
    PROCESSING,
    SPEAKING,
    INTERRUPTED,
    PAUSED,
    ERROR
}

/**
 * Audio frame containing raw PCM data and acoustic metrics.
 */
data class AudioFrame(
    val data: ByteArray,
    val timestampMs: Long = System.currentTimeMillis(),
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val rmsEnergy: Float = 0.0f,
    val isSpeech: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioFrame
        return data.contentEquals(other.data) &&
                timestampMs == other.timestampMs &&
                sampleRate == other.sampleRate &&
                channels == other.channels &&
                rmsEnergy == other.rmsEnergy &&
                isSpeech == other.isSpeech
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + timestampMs.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channels
        result = 31 * result + rmsEnergy.hashCode()
        result = 31 * result + isSpeech.hashCode()
        return result
    }
}

/**
 * Configuration options for the ZARA Voice Engine.
 */
data class VoiceConfiguration(
    val sampleRate: Int = 16000,
    val bufferSize: Int = 1280, // ~40ms at 16kHz mono 16-bit
    val vadSensitivity: Float = 0.5f,
    val silenceHangoverMs: Long = 1000L,
    val minSpeechDurationMs: Long = 300L,
    val bargeInEnabled: Boolean = true,
    val preferredLanguage: VoiceLanguage = VoiceLanguage.HINGLISH,
    val voiceName: String = "Puck",
    val ttsPitch: Float = 1.0f,
    val ttsSpeechRate: Float = 1.05f,
    val continuousMode: Boolean = true
)

/**
 * Supported conversational languages for ZARA.
 */
enum class VoiceLanguage(val code: String, val displayName: String, val bcp47: String) {
    HINDI("hi", "हिन्दी (Hindi)", "hi-IN"),
    HINGLISH("hinglish", "Hinglish (Hindi + English)", "en-IN"),
    ENGLISH("en", "English", "en-US")
}

/**
 * Interaction modes for voice communication.
 */
enum class VoiceMode {
    CONTINUOUS,
    PUSH_TO_TALK
}

/**
 * Granular events emitted by the voice assistant subsystem.
 */
sealed class VoiceEvent {
    data class StateChanged(val state: VoiceSessionState) : VoiceEvent()
    data class SpeechStarted(val timestampMs: Long) : VoiceEvent()
    data class SpeechEnded(val durationMs: Long) : VoiceEvent()
    data class UserTranscript(val text: String, val isFinal: Boolean) : VoiceEvent()
    data class AssistantAudioChunk(val audioData: ByteArray) : VoiceEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as AssistantAudioChunk
            return audioData.contentEquals(other.audioData)
        }
        override fun hashCode(): Int = audioData.contentHashCode()
    }
    data class AssistantTranscriptChunk(val text: String) : VoiceEvent()
    data class AssistantSpeechStarted(val timestampMs: Long) : VoiceEvent()
    data class AssistantSpeechEnded(val timestampMs: Long) : VoiceEvent()
    object Interrupted : VoiceEvent()
    data class Error(val message: String, val isFatal: Boolean = false) : VoiceEvent()
}
