package com.zara.ai.features.voice

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraStatusBadge
import com.zara.ai.core.designsystem.components.ZaraStatusType
import com.zara.ai.core.designsystem.theme.ZaraCyan
import com.zara.ai.core.designsystem.theme.ZaraPurple

@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val transcript by viewModel.transcript.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "Voice Intelligence",
                actions = {
                    val status = when (voiceState) {
                        VoiceState.IDLE -> ZaraStatusType.ONLINE
                        VoiceState.LISTENING -> ZaraStatusType.THINKING
                        VoiceState.PROCESSING -> ZaraStatusType.BUSY
                        VoiceState.SPEAKING -> ZaraStatusType.THINKING
                    }
                    ZaraStatusBadge(status = status, modifier = Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // AI Glowing Orb
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                if (voiceState != VoiceState.IDLE) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        ZaraCyan.copy(alpha = 0.4f),
                                        ZaraPurple.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ZaraCyan, ZaraPurple)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Real-time transcript display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = voiceState.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = transcript,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Trigger Button
            IconButton(
                onClick = { viewModel.toggleListening() },
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Trigger Voice",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
