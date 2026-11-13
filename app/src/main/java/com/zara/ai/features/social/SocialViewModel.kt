package com.zara.ai.features.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialChannel(
    val id: String,
    val name: String,
    val handle: String,
    val status: String,
    val isConnected: Boolean
)

@HiltViewModel
class SocialViewModel @Inject constructor() : ViewModel() {

    private val _channels = MutableStateFlow(
        listOf(
            SocialChannel("1", "X (Twitter)", "@ZaraAIOfficial", "Active Sync", true),
            SocialChannel("2", "Telegram Bot", "@ZaraAgent_Bot", "Listening", true),
            SocialChannel("3", "Discord Agent", "ZARA#0001", "Connected", true),
            SocialChannel("4", "LinkedIn Agent", "Zara Enterprise", "Ready", false)
        )
    )
    val channels: StateFlow<List<SocialChannel>> = _channels.asStateFlow()

    fun toggleConnection(id: String) {
        viewModelScope.launch {
            _channels.value = _channels.value.map { ch ->
                if (ch.id == id) ch.copy(isConnected = !ch.isConnected) else ch
            }
        }
    }
}
