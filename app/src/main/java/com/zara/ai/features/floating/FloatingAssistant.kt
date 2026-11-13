package com.zara.ai.features.floating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zara.ai.core.designsystem.animation.zaraPressEffect
import com.zara.ai.core.designsystem.components.OrbState
import com.zara.ai.core.designsystem.components.ZaraAiOrb
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
import com.zara.ai.core.designsystem.components.ZaraTextField
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSurface
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary
import kotlin.math.roundToInt

enum class FloatingWindowMode {
    MINI_ORB,
    MINI_CHAT,
    EXPANDED_PANEL,
    DISMISSED
}

@Composable
fun FloatingAssistant(
    onNavigateToChat: () -> Unit,
    onNavigateToVoice: () -> Unit,
    modifier: Modifier = Modifier,
    initialMode: FloatingWindowMode = FloatingWindowMode.MINI_ORB
) {
    var mode by remember { mutableStateOf(initialMode) }
    var offsetX by remember { mutableFloatStateOf(20f) }
    var offsetY by remember { mutableFloatStateOf(300f) }
    var chatInput by remember { mutableStateOf("") }

    if (mode == FloatingWindowMode.DISMISSED) return

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        when (mode) {
            // A. Mini Orb
            FloatingWindowMode.MINI_ORB -> {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ZaraBackgroundPrimary)
                        .border(1.5.dp, ZaraPrimaryAccent, CircleShape)
                        .zaraPressEffect {
                            mode = FloatingWindowMode.MINI_CHAT
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ZaraAiOrb(
                        state = OrbState.IDLE,
                        size = 54.dp
                    )
                }
            }

            // B. Mini Chat
            FloatingWindowMode.MINI_CHAT -> {
                ZaraGlassmorphicSurface(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Title bar with Window Controls: Expand, Minimize, Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ZARA Quick Chat",
                                style = MaterialTheme.typography.titleSmall,
                                color = ZaraElectricBlue,
                                fontWeight = FontWeight.Bold
                            )

                            Row {
                                IconButton(onClick = { mode = FloatingWindowMode.EXPANDED_PANEL }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.OpenInFull, contentDescription = "Expand", tint = ZaraTextSecondary, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { mode = FloatingWindowMode.MINI_ORB }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Minimize", tint = ZaraTextSecondary, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { mode = FloatingWindowMode.DISMISSED }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZaraError, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ZARA is listening. Enter query or trigger voice:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZaraTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ZaraTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = "Quick message...",
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        onNavigateToChat()
                                        mode = FloatingWindowMode.MINI_ORB
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = ZaraPrimaryAccent)
                            }
                        }
                    }
                }
            }

            // C. Expanded Control Panel
            FloatingWindowMode.EXPANDED_PANEL -> {
                ZaraGlassmorphicSurface(
                    modifier = Modifier.width(320.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ZARA Floating Command",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZaraPrimaryAccent
                            )
                            Row {
                                IconButton(onClick = { mode = FloatingWindowMode.MINI_CHAT }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Fullscreen, contentDescription = "Collapse", tint = ZaraTextSecondary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { mode = FloatingWindowMode.MINI_ORB }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Minimize", tint = ZaraTextSecondary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { mode = FloatingWindowMode.DISMISSED }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZaraError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ZaraButton(
                                onClick = {
                                    onNavigateToVoice()
                                    mode = FloatingWindowMode.MINI_ORB
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Mode")
                            }

                            ZaraButton(
                                onClick = {
                                    onNavigateToChat()
                                    mode = FloatingWindowMode.MINI_ORB
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Full Chat")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZaraSurfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.StopCircle, contentDescription = null, tint = ZaraError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency Stop Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZaraTextSecondary
                            )
                        }
                    }
                }
            }

            FloatingWindowMode.DISMISSED -> {}
        }
    }
}
