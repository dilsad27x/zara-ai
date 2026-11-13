package com.zara.ai.features.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.designsystem.components.OrbState
import com.zara.ai.core.permissions.PermissionManager
import com.zara.ai.core.permissions.model.ZaraPermission
import com.zara.ai.core.voice.engine.VoiceEngine
import com.zara.ai.core.voice.model.VoiceLanguage
import com.zara.ai.core.voice.model.VoiceSessionState
import com.zara.ai.core.voice.service.ZaraVoiceService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val permissionGranted: Boolean = true,
    val selectedLanguage: VoiceLanguage = VoiceLanguage.HINGLISH
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val voiceEngine: VoiceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    init {
        checkPermission()
        observeVoiceEngine()
    }

    fun checkPermission() {
        val granted = permissionManager.isPermissionGranted(ZaraPermission.RECORD_AUDIO)
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
    }

    fun toggleListening() {
        checkPermission()
        if (!_uiState.value.permissionGranted) {
            _uiState.value = _uiState.value.copy(
                assistantTranscript = "Microphone permission is required to capture voice input."
            )
            return
        }

        if (_uiState.value.state == VoiceAssistantState.LISTENING ||
            _uiState.value.state == VoiceAssistantState.SPEAKING ||
            _uiState.value.state == VoiceAssistantState.PROCESSING
        ) {
            stopListening()
        } else {
            startVoiceCycle()
        }
    }

    fun toggleMute() {
        val isMuted = voiceEngine.toggleMute()
        _uiState.value = _uiState.value.copy(
            isMuted = isMuted,
            orbState = if (isMuted) OrbState.PAUSED else OrbState.LISTENING
        )
    }

    fun interruptAssistant() {
        voiceEngine.interruptAssistant()
        _uiState.value = _uiState.value.copy(
            state = VoiceAssistantState.LISTENING,
            orbState = OrbState.LISTENING,
            assistantTranscript = "Listening..."
        )
    }

    fun setLanguage(language: VoiceLanguage) {
        voiceEngine.updateConfiguration(
            voiceEngine.config.value.copy(preferredLanguage = language)
        )
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun endConversation(onNavigateBack: () -> Unit) {
        stopListening()
        onNavigateBack()
    }

    private fun startVoiceCycle() {
        try {
            ZaraVoiceService.start(context)
        } catch (_: Exception) {
            voiceEngine.startSession()
        }
    }

    private fun stopListening() {
        try {
            ZaraVoiceService.stop(context)
        } catch (_: Exception) {
            voiceEngine.stopSession()
        }
    }

    private fun observeVoiceEngine() {
        viewModelScope.launch {
            voiceEngine.sessionState.collect { sessionState ->
                val (mappedState, orb) = when (sessionState) {
                    VoiceSessionState.IDLE -> VoiceAssistantState.IDLE to OrbState.IDLE
                    VoiceSessionState.INITIALIZING -> VoiceAssistantState.PROCESSING to OrbState.PROCESSING
                    VoiceSessionState.LISTENING -> VoiceAssistantState.LISTENING to OrbState.LISTENING
                    VoiceSessionState.USER_SPEAKING -> VoiceAssistantState.LISTENING to OrbState.LISTENING
                    VoiceSessionState.PROCESSING -> VoiceAssistantState.PROCESSING to OrbState.PROCESSING
                    VoiceSessionState.SPEAKING -> VoiceAssistantState.SPEAKING to OrbState.SPEAKING
                    VoiceSessionState.INTERRUPTED -> VoiceAssistantState.LISTENING to OrbState.LISTENING
                    VoiceSessionState.PAUSED -> VoiceAssistantState.PAUSED to OrbState.PAUSED
                    VoiceSessionState.ERROR -> VoiceAssistantState.ERROR to OrbState.ERROR
                }

                _uiState.value = _uiState.value.copy(
                    state = mappedState,
                    orbState = orb
                )
            }
        }

        viewModelScope.launch {
            voiceEngine.audioAmplitude.collect { amplitude ->
                _uiState.value = _uiState.value.copy(
                    simulatedAudioLevel = amplitude
                )
            }
        }

        viewModelScope.launch {
            voiceEngine.currentTranscript.collect { transcript ->
                if (transcript.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(userTranscript = transcript)
                }
            }
        }

        viewModelScope.launch {
            voiceEngine.assistantResponse.collect { response ->
                if (response.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(assistantTranscript = response)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            ZaraVoiceService.stop(context)
        } catch (_: Exception) {
            voiceEngine.stopSession()
        }
    }
}
