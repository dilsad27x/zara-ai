package com.zara.ai.features.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
import com.zara.ai.core.designsystem.components.ZaraLoadingIndicator
import com.zara.ai.core.designsystem.components.ZaraMarkdownText
import com.zara.ai.core.designsystem.components.ZaraStatusBadge
import com.zara.ai.core.designsystem.components.ZaraStatusType
import com.zara.ai.core.designsystem.components.ZaraTextField
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSecondaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSurface
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    onNavigateToVoice: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingText by viewModel.streamingMessageText.collectAsState()
    val toolStatus by viewModel.toolExecutionStatus.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val allConversations by viewModel.allConversations.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showSessionMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Detect if user has scrolled away from bottom
    val isAtBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleIndex >= totalItems - 2
            }
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size, streamingText.length) {
        val total = messages.size + if (streamingText.isNotEmpty()) 1 else 0
        if (total > 0 && isAtBottom) {
            listState.animateScrollToItem(total - 1)
        }
    }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "ZARA Conversation",
                navigationIcon = {
                    Box {
                        IconButton(onClick = { showSessionMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Sessions History",
                                tint = ZaraTextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showSessionMenu,
                            onDismissRequest = { showSessionMenu = false }
                        ) {
                            allConversations.forEach { session ->
                                DropdownMenuItem(
                                    text = { Text(session.title) },
                                    onClick = {
                                        viewModel.switchConversation(session.id)
                                        showSessionMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startNewConversation() }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New Session",
                            tint = ZaraPrimaryAccent
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isAtBottom,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        val total = messages.size + if (streamingText.isNotEmpty()) 1 else 0
                        if (total > 0) {
                            coroutineScope.launch { listState.animateScrollToItem(total - 1) }
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = ZaraSurfaceVariant,
                    contentColor = ZaraPrimaryAccent
                ) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Scroll to latest")
                }
            }
        },
        containerColor = ZaraBackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Offline Notification Banner
            if (!networkStatus.isConnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZaraError.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.WifiOff, contentDescription = null, tint = ZaraError, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device is currently offline. Local Room memory and offline tools active.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZaraError
                    )
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(msg.content))
                        },
                        onRegenerate = if (msg.role == MessageRole.ASSISTANT && msg == messages.lastOrNull { it.role == MessageRole.ASSISTANT }) {
                            { viewModel.regenerateLatestResponse() }
                        } else null
                    )
                }

                // Live Streaming Message Bubble
                if (isGenerating && streamingText.isNotEmpty()) {
                    item {
                        AssistantStreamingBubble(text = streamingText)
                    }
                }
            }

            // Active Tool Execution Status Indicator
            if (toolStatus != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(ZaraPrimaryAccent.copy(alpha = 0.15f))
                            .border(1.dp, ZaraPrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Tool Executing",
                            tint = ZaraPrimaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = toolStatus ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZaraPrimaryAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Stop Generation Bar
            if (isGenerating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ZaraSurfaceVariant)
                            .clickable { viewModel.stopGeneration() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.StopCircle,
                            contentDescription = "Stop",
                            tint = ZaraError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stop Generation",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZaraTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Input Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZaraSurface)
                    .border(width = 1.dp, color = ZaraBorder)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToVoice,
                    modifier = Modifier
                        .size(42.dp)
                        .background(ZaraSurfaceVariant, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Input",
                        tint = ZaraPrimaryAccent
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                ZaraTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = "Ask ZARA or enter command...",
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText.trim())
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !isGenerating
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) ZaraPrimaryAccent else ZaraTextSecondary.copy(alpha = 0.4f)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    onCopy: () -> Unit,
    onRegenerate: (() -> Unit)? = null
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val timestampFormatted = formatTimestamp(message.timestamp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (isUser) {
            // User Message (Vibrant Accent Gradient Bubble)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(ZaraPrimaryAccent, ZaraSecondaryAccent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timestampFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        } else {
            // ZARA AI Response Bubble (Glassmorphic Container with Markdown & Code)
            ZaraGlassmorphicSurface(
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with ZARA Avatar Tag & Timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(ZaraPrimaryAccent, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Z",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZARA AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ZaraElectricBlue
                            )
                        }

                        Text(
                            text = timestampFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ZaraTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Markdown & Code block renderer
                    ZaraMarkdownText(
                        content = message.content,
                        textColor = ZaraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Message Actions: Copy, Regenerate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Response",
                                tint = ZaraTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (onRegenerate != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = onRegenerate, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = ZaraTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantStreamingBubble(text: String) {
    ZaraGlassmorphicSurface(
        modifier = Modifier.fillMaxWidth(0.92f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ZaraLoadingIndicator(size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Streaming Response...",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZaraPrimaryAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ZaraMarkdownText(
                content = text,
                textColor = ZaraTextPrimary
            )
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
