package com.zara.ai.features.social

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraButtonVariant
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
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
fun SocialScreen(
    viewModel: SocialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(title = "Social Agent Dashboard")
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
            // Emergency Stop Killswitch Card
            item {
                ZaraCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (uiState.emergencyStopActive) ZaraError else ZaraBorder,
                    containerColor = if (uiState.emergencyStopActive) ZaraError.copy(alpha = 0.15f) else ZaraSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Dangerous,
                                    contentDescription = null,
                                    tint = if (uiState.emergencyStopActive) ZaraError else ZaraWarning
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.emergencyStopActive) "EMERGENCY STOP ACTIVE" else "Autonomous Emergency Stop",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.emergencyStopActive) ZaraError else ZaraTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instantly aborts all automated outbound messages, auto-replies, and background broadcasts across every channel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZaraTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        ZaraButton(
                            onClick = { viewModel.triggerEmergencyStop() },
                            variant = if (uiState.emergencyStopActive) ZaraButtonVariant.PRIMARY else ZaraButtonVariant.DANGER
                        ) {
                            Text(if (uiState.emergencyStopActive) "Resume" else "KILL ALL")
                        }
                    }
                }
            }

            // Platform Integration Statuses
            item {
                Text(
                    text = "Connected Platform Integrations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
            }

            items(uiState.channels, key = { it.id }) { channel ->
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ZaraSurfaceVariant, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (channel.isConnected) Icons.Filled.Link else Icons.Filled.LinkOff,
                                    contentDescription = null,
                                    tint = if (channel.isConnected) ZaraSuccess else ZaraTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = channel.platformName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ZaraTextPrimary
                                )
                                Text(
                                    text = channel.accountHandle ?: "Not Connected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (channel.isConnected) ZaraElectricBlue else ZaraTextSecondary
                                )
                                Text(
                                    text = channel.statusDescription,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (channel.isConnected) ZaraSuccess else ZaraWarning
                                )
                            }
                        }

                        // Auto-Reply Toggle
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Auto-Reply",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZaraTextSecondary
                            )
                            Switch(
                                checked = channel.autoReplyEnabled && !uiState.emergencyStopActive,
                                onCheckedChange = { viewModel.toggleAutoReply(channel.id) },
                                enabled = channel.isConnected && !uiState.emergencyStopActive,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ZaraPrimaryAccent
                                )
                            )
                        }
                    }
                }
            }

            // Approval Requests Queue
            if (uiState.approvalRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Autonomous Approval Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZaraTextPrimary
                    )
                }

                items(uiState.approvalRequests, key = { it.id }) { req ->
                    ZaraGlassmorphicSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = req.actionTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ZaraTextPrimary
                                )
                                Text(
                                    text = "Risk: ${req.riskLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZaraWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = req.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ZaraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.approveRequest(req.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZaraSuccess)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Authorize")
                                }
                            }
                        }
                    }
                }
            }

            // Pending Message Drafts
            if (uiState.pendingDrafts.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Message Drafts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZaraTextPrimary
                    )
                }

                items(uiState.pendingDrafts, key = { it.id }) { draft ->
                    ZaraCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "To: ${draft.recipient} (${draft.platform})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ZaraElectricBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${draft.draftContent}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZaraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissDraft(draft.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZaraError)
                            ) {
                                Text("Discard")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            ZaraButton(onClick = { viewModel.approveDraft(draft.id) }) {
                                Text("Approve & Send")
                            }
                        }
                    }
                }
            }
        }
    }
}
