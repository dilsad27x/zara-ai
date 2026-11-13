package com.zara.ai.features.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
import com.zara.ai.core.designsystem.components.ZaraStatusBadge
import com.zara.ai.core.designsystem.components.ZaraStatusType
import com.zara.ai.core.designsystem.theme.ZaraCyan
import com.zara.ai.core.designsystem.theme.ZaraPurple
import com.zara.ai.features.navigation.ZaraDestination

@Composable
fun HomeScreen(
    onNavigateToDestination: (ZaraDestination) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentConversations by viewModel.recentConversations.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "ZARA AI",
                actions = {
                    ZaraStatusBadge(
                        status = ZaraStatusType.ONLINE,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDestination(ZaraDestination.Chat) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            item {
                ZaraGlassmorphicSurface(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(ZaraCyan.copy(alpha = 0.2f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null,
                                    tint = ZaraCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Autonomous Intelligence",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Ready to assist with reasoning, device control & agents",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (uiState.deviceMetrics != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricChip(
                                    icon = Icons.Filled.PhoneAndroid,
                                    label = uiState.deviceMetrics?.deviceModel ?: "Android"
                                )
                                MetricChip(
                                    icon = Icons.Filled.BatteryChargingFull,
                                    label = "${uiState.deviceMetrics?.batteryLevel}%"
                                )
                                MetricChip(
                                    icon = Icons.Filled.Memory,
                                    label = "${uiState.deviceMetrics?.availableRamMb} MB Free"
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Matrix
            item {
                Text(
                    text = "Modules & Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickLaunchTile(
                            title = "Voice AI",
                            subtitle = "Real-time speech",
                            icon = Icons.Filled.Mic,
                            tint = ZaraCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Voice) }
                        )
                        QuickLaunchTile(
                            title = "Neural Memory",
                            subtitle = "Knowledge graph",
                            icon = Icons.Filled.Psychology,
                            tint = ZaraPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Memory) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickLaunchTile(
                            title = "Website Forge",
                            subtitle = "Build websites",
                            icon = Icons.Filled.Web,
                            tint = ZaraCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.WebsiteForge) }
                        )
                        QuickLaunchTile(
                            title = "Social",
                            subtitle = "Multi-platform",
                            icon = Icons.Filled.Share,
                            tint = ZaraPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToDestination(ZaraDestination.Social) }
                        )
                    }
                }
            }

            // Quick Prompts
            item {
                Text(
                    text = "Suggested Prompts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(uiState.quickPrompts) { prompt ->
                ZaraCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToDestination(ZaraDestination.Chat) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Recent Conversations Header
            if (recentConversations.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Conversations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(recentConversations.take(3)) { conv ->
                    ZaraCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToDestination(ZaraDestination.Chat) }
                    ) {
                        Text(
                            text = conv.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLaunchTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
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
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricChip(
    icon: ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
