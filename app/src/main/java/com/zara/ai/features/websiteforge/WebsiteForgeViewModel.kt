package com.zara.ai.features.websiteforge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GeneratedProject(
    val title: String,
    val description: String,
    val htmlPreview: String,
    val generatedAt: Long = System.currentTimeMillis()
)

@HiltViewModel
class WebsiteForgeViewModel @Inject constructor(
    @Dispatcher(ZaraDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _isForging = MutableStateFlow(false)
    val isForging: StateFlow<Boolean> = _isForging.asStateFlow()

    private val _latestProject = MutableStateFlow<GeneratedProject?>(
        GeneratedProject(
            title = "ZARA AI Landing Portal",
            description = "High-conversion glassmorphic landing page with dynamic ambient glow.",
            htmlPreview = "<!DOCTYPE html><html><head><title>ZARA AI</title></head><body style='background:#0B0E14;color:#00E5FF;font-family:sans-serif;text-align:center;padding:50px;'><h1>ZARA AI FORGE</h1><p>Autonomous Mobile Intelligence Framework</p></body></html>"
        )
    )
    val latestProject: StateFlow<GeneratedProject?> = _latestProject.asStateFlow()

    fun forgeWebsite(prompt: String) {
        if (prompt.isBlank() || _isForging.value) return
        _isForging.value = true
        viewModelScope.launch(defaultDispatcher) {
            delay(1500)
            _latestProject.value = GeneratedProject(
                title = "Forge: ${prompt.take(24)}...",
                description = "Custom autonomous web application tailored to: '$prompt'",
                htmlPreview = "<!DOCTYPE html><html><body style='background:#10141E;color:#FFF;'><h1>Project: $prompt</h1></body></html>"
            )
            _isForging.value = false
        }
    }
}
