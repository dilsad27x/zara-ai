package com.zara.ai

import com.zara.ai.core.voice.model.AudioFrame
import com.zara.ai.core.voice.model.VoiceConfiguration
import com.zara.ai.core.voice.model.VoiceEvent
import com.zara.ai.core.voice.model.VoiceLanguage
import com.zara.ai.core.voice.model.VoiceSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceModelsTest {

    @Test
    fun audioFrame_equalityAndHashCode_workProperly() {
        val bytes1 = byteArrayOf(1, 2, 3, 4)
        val bytes2 = byteArrayOf(1, 2, 3, 4)
        val bytes3 = byteArrayOf(5, 6, 7, 8)

        val frame1 = AudioFrame(data = bytes1, timestampMs = 1000L, sampleRate = 16000, rmsEnergy = 0.5f, isSpeech = true)
        val frame2 = AudioFrame(data = bytes2, timestampMs = 1000L, sampleRate = 16000, rmsEnergy = 0.5f, isSpeech = true)
        val frame3 = AudioFrame(data = bytes3, timestampMs = 1000L, sampleRate = 16000, rmsEnergy = 0.5f, isSpeech = true)

        assertEquals(frame1, frame2)
        assertEquals(frame1.hashCode(), frame2.hashCode())
        assertNotEquals(frame1, frame3)
    }

    @Test
    fun voiceConfiguration_defaultValues_areValid() {
        val config = VoiceConfiguration()
        assertEquals(16000, config.sampleRate)
        assertEquals(VoiceLanguage.HINGLISH, config.preferredLanguage)
        assertEquals("Puck", config.voiceName)
        assertTrue(config.bargeInEnabled)
        assertTrue(config.continuousMode)
    }

    @Test
    fun voiceLanguage_bcp47Tags_areAccurate() {
        assertEquals("hi-IN", VoiceLanguage.HINDI.bcp47)
        assertEquals("en-IN", VoiceLanguage.HINGLISH.bcp47)
        assertEquals("en-US", VoiceLanguage.ENGLISH.bcp47)
    }

    @Test
    fun voiceEvents_instantiateProperly() {
        val stateEvent = VoiceEvent.StateChanged(VoiceSessionState.LISTENING)
        assertEquals(VoiceSessionState.LISTENING, stateEvent.state)

        val audioChunk = VoiceEvent.AssistantAudioChunk(byteArrayOf(10, 20, 30))
        val sameChunk = VoiceEvent.AssistantAudioChunk(byteArrayOf(10, 20, 30))
        assertEquals(audioChunk, sameChunk)

        val errorEvent = VoiceEvent.Error("Network failure", isFatal = false)
        assertFalse(errorEvent.isFatal)
        assertEquals("Network failure", errorEvent.message)
    }
}
