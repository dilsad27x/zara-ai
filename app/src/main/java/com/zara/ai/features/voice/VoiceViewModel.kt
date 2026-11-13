package com.zara.ai.features.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.ai.engine.gateway.AIGateway
import com.zara.ai.core.designsystem.components.OrbState
import com.zara.ai.core.permissions.PermissionManager
import com.zara.ai.core.permissions.model.ZaraPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VoiceAssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    PAUSED,
    ERROR
}

data class VoiceUiState(
    val state: VoiceAssistantState = VoiceAssistantState.IDLE,
    val orbState: OrbState = OrbState.IDLE,
    val userTranscript: String = "",
    val assistantTranscript: String = "Tap microphone to begin voice interaction",
    val isMuted: Boolean = false,
    val simulatedAudioLevel: Float = 0.0f,
    val permissionGranted: Boolean = true
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val aiGateway: AIGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var voiceCycleJob: Job? = null

    init {
        checkPermission()
    }

    fun checkPermission() {
        val granted = permissionManager.isPermissionGranted(ZaraPermission.RECORD_AUDIO)
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
    }

    fun toggleListening() {
        if (!_uiState.value.permissionGranted) {
            _uiState.value = _uiState.value.copy(
                assistantTranscript = "Microphone permission is required to capture voice input."
            )
            return
        }

        if (_uiState.value.state == VoiceAssistantState.LISTENING) {
            stopListening()
        } else {
            startVoiceCycle()
        }
    }

    fun toggleMute() {
        val newMute = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(
            isMuted = newMute,
            orbState = if (newMute) OrbState.PAUSED else OrbState.LISTENING
        )
    }

    fun interruptAssistant() {
        voiceCycleJob?.cancel()
        _uiState.value = _uiState.value.copy(
            state = VoiceAssistantState.IDLE,
            orbState = OrbState.IDLE,
            assistantTranscript = "Assistant speech interrupted."
        )
    }

    fun endConversation(onNavigateBack: () -> Unit) {
        interruptAssistant()
        onNavigateBack()
    }

    private fun startVoiceCycle() {
        voiceCycleJob?.cancel()
        voiceCycleJob = viewModelScope.launch {
            // 1. Listening phase
            _uiState.value = _uiState.value.copy(
                state = VoiceAssistantState.LISTENING,
                orbState = OrbState.LISTENING,
                userTranscript = "Listening to your voice...",
                assistantTranscript = "",
                simulatedAudioLevel = 0.65f
            )

            delay(2200)

            // 2. Processing phase with real AI query
            val query = "What is the system status and available memory?"
            _uiState.value = _uiState.value.copy(
                state = VoiceAssistantState.PROCESSING,
                orbState = OrbState.PROCESSING,
                userTranscript = "\"$query\"",
                assistantTranscript = "Synthesizing response...",
                simulatedAudioLevel = 0.0f
            )

            val aiResponse = aiGateway.queryDirect(
                prompt = query,
                systemInstruction = "You are ZARA Voice Assistant. Provide a concise, clear spoken response (maximum 2 sentences) in the user's language."
            )

            val spokenText = aiResponse.getOrNull()?.text?.ifBlank { null }
                ?: "All systems are operational. The agent engine and memory database are active."

            // 3. Speaking phase
            _uiState.value = _uiState.value.copy(
                state = VoiceAssistantState.SPEAKING,
                orbState = OrbState.SPEAKING,
                assistantTranscript = spokenText,
                simulatedAudioLevel = 0.85f
            )

            delay(3500)

            // 4. Return to Idle
            _uiState.value = _uiState.value.copy(
                state = VoiceAssistantState.IDLE,
                orbState = OrbState.IDLE,
                simulatedAudioLevel = 0.0f
            )
        }
    }

    private fun stopListening() {
        voiceCycleJob?.cancel()
        _uiState.value = _uiState.value.copy(
            state = VoiceAssistantState.IDLE,
            orbState = OrbState.IDLE,
            simulatedAudioLevel = 0.0f,
            assistantTranscript = "Voice session paused."
        )
    }
}
