package com.zara.ai.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.animation.zaraPressEffect
import com.zara.ai.core.designsystem.components.ZaraAiOrb
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
import com.zara.ai.core.designsystem.components.ZaraTextField
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSecondaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSuccess
import com.zara.ai.core.designsystem.theme.ZaraSurface
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary
import com.zara.ai.features.navigation.ZaraDestination

@Composable
fun HomeScreen(
    onNavigateToDestination: (ZaraDestination) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentConversations by viewModel.recentConversations.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "ZARA AI",
                actions = {
                    // Settings Shortcut
                    IconButton(onClick = { onNavigateToDestination(ZaraDestination.Settings) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = ZaraTextSecondary
                        )
                    }
                }
            )
        },
        containerColor = ZaraBackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Pills Header (Connection Status & AI Provider)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Connection Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ZaraSurfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.networkStatus.isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = if (uiState.networkStatus.isConnected) ZaraSuccess else ZaraError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.networkStatus.isConnected) "Connected" else "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZaraTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // AI Provider Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ZaraPrimaryAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ZaraElectricBlue, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.aiProviderName,
                            style = MaterialTheme.typography.labelSmall,
                            color = ZaraPrimaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // User Greeting & Central AI Orb
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "${uiState.greeting}, Human",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZaraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "I am ready for reasoning, device control & tasks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZaraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // THE CENTRAL ANIMATED AI ORB
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(210.dp)
                            .zaraPressEffect {
                                onNavigateToDestination(ZaraDestination.Voice)
                            }
                    ) {
                        ZaraAiOrb(
                            state = uiState.orbState,
                            size = 200.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap Orb to Activate Voice Interface",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZaraElectricBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Interactive Text Input Field + Send + Voice trigger
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ZaraSurface)
                        .border(1.dp, ZaraBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigateToDestination(ZaraDestination.Voice) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Input",
                            tint = ZaraPrimaryAccent
                        )
                    }

                    ZaraTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = "Ask ZARA anything...",
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                viewModel.sendQuickPrompt(promptInput.trim()) {
                                    onNavigateToDestination(ZaraDestination.Chat)
                                }
                                promptInput = ""
                            }
                        },
                        enabled = promptInput.isNotBlank(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (promptInput.isNotBlank()) ZaraPrimaryAccent else ZaraTextSecondary.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Active Tasks Indicator (if any tasks are currently running)
            if (activeTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZaraTextPrimary
                    )
                }

                items(activeTasks, key = { it.id }) { task ->
                    ZaraGlassmorphicSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDestination(ZaraDestination.Tasks) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ZaraTextPrimary
                                )
                                Text(
                                    text = "${(task.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZaraPrimaryAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { task.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ZaraPrimaryAccent,
                                trackColor = ZaraSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Action Shortcuts Matrix
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Voice Assistant",
                            subtitle = "Real-time speech",
                            icon = Icons.Filled.Mic,
                            tint = ZaraPrimaryAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Voice) }
                        )
                        QuickActionCard(
                            title = "Neural Memory",
                            subtitle = "Knowledge base",
                            icon = Icons.Filled.Psychology,
                            tint = ZaraSecondaryAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Memory) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Device Control",
                            subtitle = "Hardware actions",
                            icon = Icons.Filled.DevicesOther,
                            tint = ZaraElectricBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.DeviceControl) }
                        )
                        QuickActionCard(
                            title = "Social Agent",
                            subtitle = "Multi-platform",
                            icon = Icons.Filled.Share,
                            tint = ZaraPrimaryAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Social) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Active Tasks",
                            subtitle = "Task monitor",
                            icon = Icons.Filled.Assignment,
                            tint = ZaraSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Tasks) }
                        )
                        QuickActionCard(
                            title = "Website Forge",
                            subtitle = "Code generation",
                            icon = Icons.Filled.Web,
                            tint = ZaraElectricBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.WebsiteForge) }
                        )
                    }
                }
            }

            // Conversation History Access
            if (recentConversations.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Sessions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZaraTextPrimary
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZaraPrimaryAccent,
                            modifier = Modifier.clickable { onNavigateToDestination(ZaraDestination.Chat) }
                        )
                    }
                }

                items(recentConversations.take(3), key = { it.id }) { conv ->
                    ZaraCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToDestination(ZaraDestination.Chat) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = null,
                                tint = ZaraPrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = conv.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = ZaraTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ZaraCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ZaraTextPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = ZaraTextSecondary
        )
    }
}
