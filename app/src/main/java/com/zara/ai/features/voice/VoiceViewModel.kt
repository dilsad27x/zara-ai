package com.zara.ai.features.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.permissions.PermissionManager
import com.zara.ai.core.permissions.model.ZaraPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VoiceState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _transcript = MutableStateFlow("Tap the microphone to speak with ZARA...")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    fun toggleListening() {
        if (_voiceState.value == VoiceState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (!permissionManager.isPermissionGranted(ZaraPermission.RECORD_AUDIO)) {
            _transcript.value = "Microphone permission is required for voice interaction."
            return
        }

        _voiceState.value = VoiceState.LISTENING
        _transcript.value = "Listening to your voice..."

        viewModelScope.launch {
            delay(2500)
            _voiceState.value = VoiceState.PROCESSING
            _transcript.value = "Analyzing acoustic tokens & semantic intent..."
            delay(1200)
            _voiceState.value = VoiceState.SPEAKING
            _transcript.value = "\"I hear you clearly! I am ready to perform your commands or answer questions.\""
            delay(3000)
            _voiceState.value = VoiceState.IDLE
        }
    }

    private fun stopListening() {
        _voiceState.value = VoiceState.IDLE
        _transcript.value = "Voice session paused."
    }
}
