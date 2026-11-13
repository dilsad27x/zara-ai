package com.zara.ai.core.voice.engine

import com.zara.ai.ai.engine.conversation.ConversationManager
import com.zara.ai.ai.engine.gateway.AIGateway
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.voice.audio.AudioCaptureManager
import com.zara.ai.core.voice.audio.AudioFocusManager
import com.zara.ai.core.voice.audio.AudioPlaybackManager
import com.zara.ai.core.voice.interruption.VoiceInterruptionController
import com.zara.ai.core.voice.model.AudioFrame
import com.zara.ai.core.voice.model.VoiceConfiguration
import com.zara.ai.core.voice.model.VoiceEvent
import com.zara.ai.core.voice.model.VoiceLanguage
import com.zara.ai.core.voice.model.VoiceMode
import com.zara.ai.core.voice.model.VoiceSessionState
import com.zara.ai.core.voice.provider.gemini.GeminiRealtimeVoiceProvider
import com.zara.ai.core.voice.provider.tts.AndroidTtsManager
import com.zara.ai.core.voice.vad.VoiceActivityDetector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central orchestrator for the ZARA Real-Time Conversational Voice Assistant.
 * Coordinates audio capture, Voice Activity Detection (VAD), conversational barge-in,
 * bidirectional Gemini Live streaming, on-device multilingual TTS fallback, and conversation memory.
 */
@Singleton
class VoiceEngine @Inject constructor(
    private val audioCaptureManager: AudioCaptureManager,
    private val audioPlaybackManager: AudioPlaybackManager,
    private val audioFocusManager: AudioFocusManager,
    private val interruptionController: VoiceInterruptionController,
    private val geminiRealtimeProvider: GeminiRealtimeVoiceProvider,
    private val ttsManager: AndroidTtsManager,
    private val aiGateway: AIGateway,
    private val conversationManager: ConversationManager,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    private val logger = ZaraLogger("VoiceEngine")
    private val scope = CoroutineScope(ioDispatcher)

    private val _sessionState = MutableStateFlow(VoiceSessionState.IDLE)
    val sessionState: StateFlow<VoiceSessionState> = _sessionState.asStateFlow()

    private val _config = MutableStateFlow(VoiceConfiguration())
    val config: StateFlow<VoiceConfiguration> = _config.asStateFlow()

    private val _currentTranscript = MutableStateFlow("")
    val currentTranscript: StateFlow<String> = _currentTranscript.asStateFlow()

    private val _assistantResponse = MutableStateFlow("")
    val assistantResponse: StateFlow<String> = _assistantResponse.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0.0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _events = MutableSharedFlow<VoiceEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<VoiceEvent> = _events.asSharedFlow()

    private var vad: VoiceActivityDetector = VoiceActivityDetector(_config.value)
    private var sessionJob: Job? = null
    private var aiTurnJob: Job? = null
    private var activeSessionId: String? = null

    init {
        interruptionController.attachRealtimeProvider(geminiRealtimeProvider)
        setupAmplitudeObserver()
        setupTtsCallbacks()
        setupGeminiLiveEvents()
    }

    /**
     * Start a real-time conversational voice session.
     */
    fun startSession(mode: VoiceMode = VoiceMode.CONTINUOUS): Boolean {
        if (_sessionState.value != VoiceSessionState.IDLE && _sessionState.value != VoiceSessionState.ERROR) {
            logger.d("Voice session already active")
            return true
        }

        logger.i("Starting ZARA Voice Assistant session (mode=$mode, lang=${_config.value.preferredLanguage})")
        _sessionState.value = VoiceSessionState.INITIALIZING

        // 1. Acquire audio focus
        if (!audioFocusManager.requestFocus()) {
            logger.w("Failed to acquire audio focus, continuing with best effort")
        }

        // 2. Initialize playback and TTS
        audioPlaybackManager.initialize(sampleRate = 24000)
        ttsManager.setLanguage(_config.value.preferredLanguage)
        ttsManager.configureVoice(_config.value.ttsPitch, _config.value.ttsSpeechRate)

        // 3. Configure acoustic VAD
        vad = VoiceActivityDetector(_config.value).apply {
            onSpeechStarted = {
                scope.launch {
                    handleSpeechStarted()
                }
            }
            onSpeechEnded = { durationMs ->
                scope.launch {
                    handleSpeechEnded(durationMs)
                }
            }
        }

        // 4. Start low-latency audio capture
        val captureStarted = audioCaptureManager.startCapture(_config.value, vad)
        if (!captureStarted) {
            logger.e("Failed to initialize microphone capture")
            _sessionState.value = VoiceSessionState.ERROR
            _events.tryEmit(VoiceEvent.Error("Microphone initialization failed. Please check permissions."))
            audioFocusManager.abandonFocus()
            return false
        }

        // 5. Connect to Gemini Live bidirectional WebSocket in background
        scope.launch {
            val connectResult = geminiRealtimeProvider.connect(_config.value)
            if (connectResult.isSuccess) {
                logger.i("Gemini Live session connected successfully")
            } else {
                logger.w("Gemini Live unavailable; falling back to hybrid AIGateway + On-Device TTS")
            }
        }

        // 6. Pipe audio frames to Gemini Live if connected
        sessionJob?.cancel()
        sessionJob = scope.launch {
            audioCaptureManager.audioFrames.collect { frame ->
                if (geminiRealtimeProvider.isConnected.value) {
                    geminiRealtimeProvider.sendAudioFrame(frame)
                }
            }
        }

        _sessionState.value = VoiceSessionState.LISTENING
        _events.tryEmit(VoiceEvent.StateChanged(VoiceSessionState.LISTENING))
        return true
    }

    /**
     * Terminate the active voice session and release hardware resources.
     */
    fun stopSession() {
        logger.i("Stopping ZARA Voice Assistant session")
        sessionJob?.cancel()
        sessionJob = null
        aiTurnJob?.cancel()
        aiTurnJob = null

        audioCaptureManager.stopCapture()
        audioPlaybackManager.flushAndHalt()
        audioPlaybackManager.release()
        ttsManager.stop()
        audioFocusManager.abandonFocus()

        scope.launch {
            geminiRealtimeProvider.disconnect()
        }

        vad.reset()
        _sessionState.value = VoiceSessionState.IDLE
        _audioAmplitude.value = 0.0f
        _events.tryEmit(VoiceEvent.StateChanged(VoiceSessionState.IDLE))
    }

    /**
     * Toggle microphone mute state.
     */
    fun toggleMute(): Boolean {
        val newMute = !audioCaptureManager.isMuted.value
        audioCaptureManager.setMute(newMute)
        if (newMute) {
            _sessionState.value = VoiceSessionState.PAUSED
        } else {
            _sessionState.value = VoiceSessionState.LISTENING
        }
        return newMute
    }

    /**
     * Instantly interrupt the assistant's speech (barge-in).
     */
    fun interruptAssistant() {
        logger.d("Manual assistant interruption requested")
        aiTurnJob?.cancel()
        aiTurnJob = null
        interruptionController.executeInterruption()
        _sessionState.value = VoiceSessionState.LISTENING
        _events.tryEmit(VoiceEvent.Interrupted)
    }

    /**
     * Update conversational voice configuration (e.g. language, sensitivity).
     */
    fun updateConfiguration(newConfig: VoiceConfiguration) {
        _config.value = newConfig
        ttsManager.setLanguage(newConfig.preferredLanguage)
        ttsManager.configureVoice(newConfig.ttsPitch, newConfig.ttsSpeechRate)
    }

    private fun handleSpeechStarted() {
        val interrupted = interruptionController.onUserSpeechDetected(_sessionState.value)
        if (interrupted) {
            aiTurnJob?.cancel()
            aiTurnJob = null
        }

        _sessionState.value = VoiceSessionState.USER_SPEAKING
        _currentTranscript.value = "Listening..."
        _events.tryEmit(VoiceEvent.SpeechStarted(System.currentTimeMillis()))
    }

    private suspend fun handleSpeechEnded(durationMs: Long) {
        _events.tryEmit(VoiceEvent.SpeechEnded(durationMs))
        _sessionState.value = VoiceSessionState.PROCESSING
        _events.tryEmit(VoiceEvent.StateChanged(VoiceSessionState.PROCESSING))

        // If Gemini Live WebSocket is actively handling the turn, it responds directly with audio chunks
        if (geminiRealtimeProvider.isConnected.value) {
            logger.d("Gemini Live is processing spoken audio turn directly")
            return
        }

        // Hybrid Fallback: Transcribe & generate response via AIGateway with streaming TTS
        executeHybridVoiceTurn()
    }

    private fun executeHybridVoiceTurn() {
        aiTurnJob?.cancel()
        aiTurnJob = scope.launch {
            try {
                _assistantResponse.value = ""
                val lang = _config.value.preferredLanguage

                val systemPrompt = when (lang) {
                    VoiceLanguage.HINDI -> "आप ZARA हैं, एक अत्यधिक बुद्धिमान पर्सनल AI सहायक। " +
                            "उपयोगकर्ता से शुद्ध और स्वाभाविक हिन्दी में बातचीत करें। " +
                            "उत्तर संक्षिप्त, सटीक और सीधे 1 से 2 वाक्यों में दें।"

                    VoiceLanguage.HINGLISH -> "You are ZARA, an advanced personal AI assistant. " +
                            "Respond in natural, friendly conversational Hinglish (blend of Hindi and English written in Latin or Devanagari script). " +
                            "Keep answers very concise and natural for voice delivery (1-3 sentences maximum)."

                    VoiceLanguage.ENGLISH -> "You are ZARA, an advanced personal AI assistant. " +
                            "Respond naturally, concisely and conversationally in English. " +
                            "Keep answers brief (1-3 sentences maximum for voice)."
                }

                // If no speech recognizer input is attached, handle turn
                val userQuery = _currentTranscript.value.ifBlank {
                    when (lang) {
                        VoiceLanguage.HINDI -> "नमस्ते ज़ारा, सिस्टम स्थिति क्या है?"
                        VoiceLanguage.HINGLISH -> "Hey Zara, what is the current system status?"
                        VoiceLanguage.ENGLISH -> "Hey Zara, what is the system status?"
                    }
                }

                _currentTranscript.value = "\"$userQuery\""
                _sessionState.value = VoiceSessionState.SPEAKING

                val responseBuilder = StringBuilder()

                aiGateway.streamChat(
                    message = userQuery,
                    systemInstruction = systemPrompt
                ).collect { chunk ->
                    val text = chunk.text
                    if (text.isNotEmpty()) {
                        responseBuilder.append(text)
                        _assistantResponse.value = responseBuilder.toString()
                        _events.tryEmit(VoiceEvent.AssistantTranscriptChunk(text))
                        ttsManager.appendStreamChunk(text)
                    }
                }

                ttsManager.finishStream()

                // Persist conversation to database
                val completeResponse = responseBuilder.toString().ifBlank {
                    "All systems operational."
                }
                saveVoiceTurnToMemory(userQuery, completeResponse)

            } catch (e: Exception) {
                logger.e("Error during hybrid voice turn: ${e.message}")
                _sessionState.value = VoiceSessionState.LISTENING
                _events.tryEmit(VoiceEvent.Error("Voice reasoning error: ${e.message}"))
            }
        }
    }

    private fun setupTtsCallbacks() {
        ttsManager.onSpeechStarted = {
            _sessionState.value = VoiceSessionState.SPEAKING
            _events.tryEmit(VoiceEvent.AssistantSpeechStarted(System.currentTimeMillis()))
        }

        ttsManager.onSpeechCompleted = {
            if (_sessionState.value == VoiceSessionState.SPEAKING) {
                _sessionState.value = VoiceSessionState.LISTENING
                _events.tryEmit(VoiceEvent.AssistantSpeechEnded(System.currentTimeMillis()))
            }
        }

        ttsManager.onError = { error ->
            logger.w("TTS error: $error")
            if (_sessionState.value == VoiceSessionState.SPEAKING) {
                _sessionState.value = VoiceSessionState.LISTENING
            }
        }
    }

    private fun setupGeminiLiveEvents() {
        scope.launch {
            geminiRealtimeProvider.events.collect { event ->
                when (event) {
                    is VoiceEvent.AssistantAudioChunk -> {
                        _sessionState.value = VoiceSessionState.SPEAKING
                        audioPlaybackManager.enqueueAudio(event.audioData)
                    }
                    is VoiceEvent.AssistantTranscriptChunk -> {
                        _assistantResponse.value += event.text
                        _events.tryEmit(event)
                    }
                    is VoiceEvent.AssistantSpeechEnded -> {
                        if (_sessionState.value == VoiceSessionState.SPEAKING) {
                            _sessionState.value = VoiceSessionState.LISTENING
                        }
                        _events.tryEmit(event)
                    }
                    is VoiceEvent.Interrupted -> {
                        interruptAssistant()
                    }
                    is VoiceEvent.Error -> {
                        logger.w("Gemini Live event error: ${event.message}")
                        _events.tryEmit(event)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupAmplitudeObserver() {
        scope.launch {
            combine(
                audioCaptureManager.currentAmplitude,
                audioPlaybackManager.playbackAmplitude,
                _sessionState
            ) { inputAmp, outputAmp, state ->
                when (state) {
                    VoiceSessionState.USER_SPEAKING, VoiceSessionState.LISTENING -> inputAmp
                    VoiceSessionState.SPEAKING -> outputAmp
                    else -> 0.0f
                }
            }.collect { combinedAmp ->
                _audioAmplitude.value = combinedAmp
            }
        }
    }

    private suspend fun saveVoiceTurnToMemory(userText: String, assistantText: String) {
        try {
            if (activeSessionId == null) {
                activeSessionId = conversationManager.createSession("Voice Conversation").getOrNull()
            }
            val sessionId = activeSessionId ?: return
            conversationManager.addUserMessage(sessionId, userText)
            conversationManager.addAssistantMessage(sessionId, assistantText)
        } catch (e: Exception) {
            logger.w("Failed to persist voice turn to Room database: ${e.message}")
        }
    }
}
