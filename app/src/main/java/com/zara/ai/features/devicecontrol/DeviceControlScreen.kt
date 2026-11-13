package com.zara.ai.features.devicecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraButtonVariant
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSuccess
import com.zara.ai.core.designsystem.theme.ZaraSurface
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary
import com.zara.ai.core.designsystem.theme.ZaraWarning

@Composable
fun DeviceControlScreen(
    viewModel: DeviceControlViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.taskHistory.collectAsState()

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "Device Control",
                actions = {
                    IconButton(onClick = { viewModel.refreshState() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Telemetry",
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
            // Android System Privileges & Service Status Cards
            item {
                Text(
                    text = "System Privileges & Service Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServiceStatusRow(
                        title = "Accessibility Service",
                        subtitle = "Autonomous UI touch & gesture control",
                        icon = Icons.Filled.AccessibilityNew,
                        isGranted = uiState.isAccessibilityEnabled
                    )
                    ServiceStatusRow(
                        title = "Notification Interceptor",
                        subtitle = "Read & auto-reply to incoming notifications",
                        icon = Icons.Filled.NotificationsActive,
                        isGranted = uiState.isNotificationAccessEnabled
                    )
                    ServiceStatusRow(
                        title = "Floating Window Overlay",
                        subtitle = "Draw over apps for global Mini Orb",
                        icon = Icons.Filled.Layers,
                        isGranted = uiState.isOverlayGranted
                    )
                    ServiceStatusRow(
                        title = "Screen Capture Authorization",
                        subtitle = "Live UI perception & visual reasoning (Scheduled for Part 03)",
                        icon = Icons.Filled.ScreenShare,
                        isGranted = uiState.isScreenCaptureAuthorized,
                        statusText = "Pending Part 03"
                    )
                }
            }

            // Real Hardware Telemetry
            item {
                Text(
                    text = "Hardware Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
            }

            item {
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    uiState.deviceMetrics?.let { m ->
                        Text(
                            text = m.deviceModel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZaraElectricBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = m.osVersion,
                            style = MaterialTheme.typography.bodySmall,
                            color = ZaraTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Battery Level: ${m.batteryLevel}%", color = ZaraTextPrimary)
                            Text("Charging: ${m.isCharging}", color = if (m.isCharging) ZaraSuccess else ZaraTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Available RAM: ${m.availableRamMb} MB", color = ZaraTextPrimary)
                            Text("Total RAM: ${m.totalRamMb} MB", color = ZaraTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ZaraButton(
                        onClick = { viewModel.copyDiagnostics() },
                        variant = ZaraButtonVariant.OUTLINED,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Full Telemetry Report")
                    }
                }
            }

            // Audio Stream Control
            item {
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Media Audio Stream",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZaraTextPrimary
                        )
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = ZaraPrimaryAccent)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Level: ${uiState.currentVolume} / ${uiState.maxVolume}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZaraTextSecondary
                    )
                    Slider(
                        value = uiState.currentVolume.toFloat(),
                        onValueChange = { viewModel.setVolume(it.toInt()) },
                        valueRange = 0f..uiState.maxVolume.toFloat(),
                        steps = (uiState.maxVolume - 1).coerceAtLeast(0)
                    )
                }
            }

            // Application Launch Controls
            item {
                Text(
                    text = "Application Launch Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.quickLaunchApps.forEach { (name, pkg) ->
                        ZaraCard(
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.launchApp(pkg) }
                        ) {
                            Icon(Icons.Filled.Apps, contentDescription = name, tint = ZaraElectricBlue)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Launch", style = MaterialTheme.typography.labelSmall, color = ZaraPrimaryAccent)
                        }
                    }
                }
            }

            // Execution History from AgentTaskDao
            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Operations History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZaraTextPrimary
                    )
                }

                items(history.take(4), key = { it.id }) { task ->
                    ZaraCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ZaraTextPrimary
                            )
                            Text(
                                text = task.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = ZaraSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = ZaraTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    statusText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZaraSurface)
            .border(1.dp, ZaraBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(ZaraSurfaceVariant, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = ZaraElectricBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (statusText != null) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = ZaraWarning,
                fontWeight = FontWeight.Bold
            )
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        if (isGranted) ZaraSuccess.copy(alpha = 0.2f) else ZaraError.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (isGranted) ZaraSuccess else ZaraError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
