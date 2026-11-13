package com.zara.ai.features.tasks

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.AgentTaskStatus
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraEmptyState
import com.zara.ai.core.designsystem.components.ZaraLoadingIndicator
import com.zara.ai.core.designsystem.components.ZaraModalSheet
import com.zara.ai.core.designsystem.components.ZaraTextField
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSuccess
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary
import com.zara.ai.core.designsystem.theme.ZaraWarning

@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasks by viewModel.allTasks.collectAsState()
    val isCreating by viewModel.isCreatingTask.collectAsState()
    var isNewTaskSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(title = "Autonomous Tasks")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isNewTaskSheetOpen = true },
                containerColor = ZaraPrimaryAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Task")
            }
        },
        containerColor = ZaraBackgroundPrimary
    ) { padding ->
        if (tasks.isEmpty()) {
            ZaraEmptyState(
                title = "No Active Agent Tasks",
                description = "Launch an autonomous routine, schedule an audit, or generate web applications.",
                icon = Icons.Filled.Assignment,
                actionButtonText = "Launch Autonomous Routine",
                onActionClick = { isNewTaskSheetOpen = true },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onPause = { viewModel.pauseTask(task) },
                        onResume = { viewModel.resumeTask(task) },
                        onCancel = { viewModel.cancelTask(task) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }

        if (isNewTaskSheetOpen) {
            NewTaskSheet(
                isLoading = isCreating,
                onDismiss = { isNewTaskSheetOpen = false },
                onLaunch = { title, prompt ->
                    viewModel.launchNewAutonomousTask(title, prompt)
                    isNewTaskSheetOpen = false
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: AgentTaskEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (task.status) {
        AgentTaskStatus.RUNNING -> ZaraPrimaryAccent
        AgentTaskStatus.COMPLETED -> ZaraSuccess
        AgentTaskStatus.PENDING -> ZaraWarning
        AgentTaskStatus.FAILED -> ZaraError
        AgentTaskStatus.CANCELLED -> Color.Gray
    }

    ZaraCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ZaraTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = task.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyMedium,
            color = ZaraTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar & percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = ZaraSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${(task.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = ZaraTextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        // Subtask / Result summary
        if (!task.resultJson.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Result: ${task.resultJson}",
                style = MaterialTheme.typography.bodySmall,
                color = ZaraElectricBlue
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (task.status == AgentTaskStatus.RUNNING) {
                IconButton(onClick = onPause) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = ZaraWarning)
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = ZaraError)
                }
            } else if (task.status == AgentTaskStatus.PENDING) {
                IconButton(onClick = onResume) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Resume", tint = ZaraSuccess)
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = ZaraError)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = ZaraTextSecondary)
            }
        }
    }
}

@Composable
private fun NewTaskSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onLaunch: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }

    ZaraModalSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Launch Autonomous Agent Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ZaraTextPrimary
            )

            ZaraTextField(
                value = title,
                onValueChange = { title = it },
                label = "Task Name / Goal",
                placeholder = "e.g. Android Telemetry Audit"
            )

            ZaraTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = "Autonomous Instructions",
                placeholder = "e.g. Inspect all memory channels, verify security and log results"
            )

            ZaraButton(
                onClick = { onLaunch(title, prompt) },
                enabled = title.isNotBlank() && prompt.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    ZaraLoadingIndicator(size = 20.dp, color = Color.White)
                } else {
                    Text("Launch Task")
                }
            }
        }
    }
}
