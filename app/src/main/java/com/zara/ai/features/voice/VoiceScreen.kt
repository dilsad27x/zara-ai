package com.zara.ai.features.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraAiOrb
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
import com.zara.ai.core.designsystem.components.ZaraStatusBadge
import com.zara.ai.core.designsystem.components.ZaraStatusType
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSecondaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary
import com.zara.ai.core.voice.model.VoiceLanguage

@Composable
fun VoiceScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val statusType = when (uiState.state) {
        VoiceAssistantState.IDLE -> ZaraStatusType.ONLINE
        VoiceAssistantState.LISTENING -> ZaraStatusType.THINKING
        VoiceAssistantState.PROCESSING -> ZaraStatusType.BUSY
        VoiceAssistantState.SPEAKING -> ZaraStatusType.THINKING
        VoiceAssistantState.PAUSED -> ZaraStatusType.OFFLINE
        VoiceAssistantState.ERROR -> ZaraStatusType.ERROR
    }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "Voice Intelligence",
                actions = {
                    ZaraStatusBadge(
                        status = statusType,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        containerColor = ZaraBackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Status indicator and Multilingual selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Status Indicator Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ZaraSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (uiState.state) {
                                    VoiceAssistantState.LISTENING -> ZaraElectricBlue
                                    VoiceAssistantState.PROCESSING -> ZaraSecondaryAccent
                                    VoiceAssistantState.SPEAKING -> ZaraPrimaryAccent
                                    else -> Color.Gray
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.state.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = ZaraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Multilingual Selector Chips
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(ZaraSurfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VoiceLanguage.values().forEach { lang ->
                        val isSelected = uiState.selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ZaraPrimaryAccent else Color.Transparent)
                                .clickable { viewModel.setLanguage(lang) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    VoiceLanguage.HINDI -> "हिन्दी"
                                    VoiceLanguage.HINGLISH -> "Hinglish"
                                    VoiceLanguage.ENGLISH -> "English"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else ZaraTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // LARGE CENTERPIECE AI ORB (Driven by Real Acoustic Amplitude)
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                ZaraAiOrb(
                    state = uiState.orbState,
                    size = 220.dp,
                    audioLevel = uiState.simulatedAudioLevel
                )
            }

            // Live Transcription Glass Display
            ZaraGlassmorphicSurface(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.userTranscript.isNotEmpty()) {
                        Text(
                            text = uiState.userTranscript,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ZaraElectricBlue,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = uiState.assistantTranscript.ifEmpty { "..." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = ZaraTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Control Actions Dock: Mute, Push-to-Talk / Main Trigger, Interrupt, End
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Toggle
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(ZaraSurfaceVariant, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Mute",
                        tint = if (uiState.isMuted) ZaraError else ZaraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Primary Trigger Mic Button
                IconButton(
                    onClick = { viewModel.toggleListening() },
                    modifier = Modifier
                        .size(76.dp)
                        .background(ZaraPrimaryAccent, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Activate Voice",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Interrupt / Stop Speaking Button
                IconButton(
                    onClick = { viewModel.interruptAssistant() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(ZaraSurfaceVariant, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Interrupt",
                        tint = ZaraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // End Conversation
                IconButton(
                    onClick = { viewModel.endConversation(onNavigateBack) },
                    modifier = Modifier
                        .size(54.dp)
                        .background(ZaraError.copy(alpha = 0.2f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End Call",
                        tint = ZaraError,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
