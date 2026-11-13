package com.zara.ai

import com.zara.ai.core.voice.model.VoiceConfiguration
import com.zara.ai.core.voice.vad.VoiceActivityDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

class VoiceActivityDetectorTest {

    private lateinit var vad: VoiceActivityDetector

    @Before
    fun setup() {
        val config = VoiceConfiguration(
            sampleRate = 16000,
            vadSensitivity = 0.5f,
            silenceHangoverMs = 500L,
            minSpeechDurationMs = 100L
        )
        vad = VoiceActivityDetector(config)
    }

    @Test
    fun processFrame_withSilence_returnsZeroRmsAndNoSpeech() {
        val silentBytes = ByteArray(1280) // 640 samples of silence
        val frame = vad.processFrame(silentBytes)

        assertEquals(0.0f, frame.rmsEnergy, 0.001f)
        assertFalse(frame.isSpeech)
    }

    @Test
    fun processFrame_withLoudSineWave_triggersSpeechOnset() {
        var speechStartedFired = false
        vad.onSpeechStarted = { speechStartedFired = true }

        // Generate synthetic 440Hz sine wave at high amplitude
        val sampleRate = 16000
        val sampleCount = 640
        val buffer = ByteBuffer.allocate(sampleCount * 2).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until sampleCount) {
            val sample = (sin(2.0 * Math.PI * 440.0 * i / sampleRate) * 20000.0).toInt().toShort()
            buffer.putShort(sample)
        }
        val speechBytes = buffer.array()

        // Process first frame
        val frame1 = vad.processFrame(speechBytes)
        assertTrue(frame1.rmsEnergy > 0.1f)

        // Process second consecutive frame to satisfy onset filter
        val frame2 = vad.processFrame(speechBytes)
        assertTrue(frame2.rmsEnergy > 0.1f)
        assertTrue(speechStartedFired)
        assertTrue(frame2.isSpeech)
    }

    @Test
    fun calculateVisualAmplitude_clampsBetweenZeroAndOne() {
        val silentAmp = vad.calculateVisualAmplitude(0.0f)
        assertEquals(0.0f, silentAmp, 0.001f)

        val loudAmp = vad.calculateVisualAmplitude(1.0f)
        assertEquals(1.0f, loudAmp, 0.001f)

        val normalSpeechAmp = vad.calculateVisualAmplitude(0.1f)
        assertTrue(normalSpeechAmp in 0.0f..1.0f)
    }

    @Test
    fun reset_restoresCleanBaseline() {
        vad.reset()
        val silentBytes = ByteArray(1280)
        val frame = vad.processFrame(silentBytes)
        assertFalse(frame.isSpeech)
    }
}
