package com.zara.ai.core.voice.vad

import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.voice.model.AudioFrame
import com.zara.ai.core.voice.model.VoiceConfiguration
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance acoustic Voice Activity Detector (VAD).
 * Utilizes short-time Root Mean Square (RMS) energy, zero-crossing rate (ZCR),
 * and dynamic adaptive noise floor tracking to reliably distinguish speech from silence.
 */
class VoiceActivityDetector(
    private val config: VoiceConfiguration = VoiceConfiguration()
) {
    private val logger = ZaraLogger("VoiceActivityDetector")

    private var isSpeechActive = false
    private var speechStartTimeMs = 0L
    private var consecutiveSpeechFrames = 0
    private var silenceStartTimeMs = 0L

    // Adaptive noise floor baseline
    private var noiseFloorRms = 0.015f
    private val minThresholdRms = 0.035f * (1.2f - config.vadSensitivity * 0.4f)

    var onSpeechStarted: (() -> Unit)? = null
    var onSpeechEnded: ((durationMs: Long) -> Unit)? = null

    /**
     * Process a raw 16-bit PCM AudioFrame and evaluate speech activity.
     * Returns an enriched AudioFrame containing computed RMS energy and isSpeech flag.
     */
    fun processFrame(rawBytes: ByteArray, sampleRate: Int = config.sampleRate): AudioFrame {
        val now = System.currentTimeMillis()
        val shortBuffer = ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val sampleCount = shortBuffer.remaining()

        if (sampleCount == 0) {
            return AudioFrame(
                data = rawBytes,
                timestampMs = now,
                sampleRate = sampleRate,
                rmsEnergy = 0.0f,
                isSpeech = false
            )
        }

        var sumSquare = 0.0
        var zeroCrossings = 0
        var prevSample = 0

        for (i in 0 until sampleCount) {
            val sample = shortBuffer.get(i).toInt()
            val normalized = sample / 32768.0f
            sumSquare += (normalized * normalized)

            if (i > 0) {
                if ((sample >= 0 && prevSample < 0) || (sample < 0 && prevSample >= 0)) {
                    zeroCrossings++
                }
            }
            prevSample = sample
        }

        val rms = sqrt(sumSquare / sampleCount).toFloat()
        val zcr = zeroCrossings.toFloat() / sampleCount

        // Adaptive threshold computation
        val dynamicThreshold = max(minThresholdRms, noiseFloorRms * 2.5f)
        val frameIsVoice = rms > dynamicThreshold && zcr > 0.02f

        // Adapt noise floor slowly during silence
        if (!frameIsVoice && !isSpeechActive) {
            noiseFloorRms = (noiseFloorRms * 0.96f) + (rms * 0.04f)
        }

        // State Machine Evaluation
        if (frameIsVoice) {
            consecutiveSpeechFrames++
            silenceStartTimeMs = 0L

            if (!isSpeechActive && consecutiveSpeechFrames >= 2) {
                isSpeechActive = true
                speechStartTimeMs = now
                logger.d("Speech onset detected (RMS: $rms, Thresh: $dynamicThreshold)")
                onSpeechStarted?.invoke()
            }
        } else {
            consecutiveSpeechFrames = 0

            if (isSpeechActive) {
                if (silenceStartTimeMs == 0L) {
                    silenceStartTimeMs = now
                } else if (now - silenceStartTimeMs >= config.silenceHangoverMs) {
                    val speechDuration = silenceStartTimeMs - speechStartTimeMs
                    isSpeechActive = false
                    silenceStartTimeMs = 0L

                    if (speechDuration >= config.minSpeechDurationMs) {
                        logger.d("Speech utterance ended. Duration: ${speechDuration}ms")
                        onSpeechEnded?.invoke(speechDuration)
                    } else {
                        logger.d("Filtered short transient noise: ${speechDuration}ms")
                    }
                }
            }
        }

        return AudioFrame(
            data = rawBytes,
            timestampMs = now,
            sampleRate = sampleRate,
            rmsEnergy = rms,
            isSpeech = isSpeechActive
        )
    }

    /**
     * Compute a normalized [0.0, 1.0] audio amplitude from RMS energy
     * for visualizer and UI orb waveform scaling.
     */
    fun calculateVisualAmplitude(rms: Float): Float {
        if (rms <= 0.001f) return 0.0f
        val db = 20.0f * log10(rms) // typical range: -60 dB (quiet) to 0 dB (loud)
        val normalized = (db + 45.0f) / 45.0f
        return min(1.0f, max(0.0f, normalized))
    }

    /**
     * Reset the VAD state machine and noise baseline.
     */
    fun reset() {
        isSpeechActive = false
        speechStartTimeMs = 0L
        consecutiveSpeechFrames = 0
        silenceStartTimeMs = 0L
        noiseFloorRms = 0.015f
    }
}
