package com.zara.ai.features.memory

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MemoryEntity
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraEmptyState
import com.zara.ai.core.designsystem.components.ZaraGlassmorphicSurface
import com.zara.ai.core.designsystem.components.ZaraModalSheet
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

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val memories by viewModel.memories.collectAsState()
    val stats by viewModel.memoryStats.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val autoExtract by viewModel.autoExtractEnabled.collectAsState()

    var isAddingMemory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "Neural Memory",
                actions = {
                    IconButton(onClick = { viewModel.clearShortTermConversationMemory() }) {
                        Icon(
                            imageVector = Icons.Filled.CleaningServices,
                            contentDescription = "Clear Short-Term Context",
                            tint = ZaraTextSecondary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingMemory = true },
                containerColor = ZaraPrimaryAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Memory")
            }
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
            // Memory Architecture Vitals Overview
            item {
                ZaraGlassmorphicSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Memory Architecture Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZaraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MemoryStatColumn("Short-Term", "${stats.shortTermContextCount} tokens", ZaraElectricBlue)
                            MemoryStatColumn("Long-Term", "${stats.longTermMemoryCount} facts", ZaraPrimaryAccent)
                            MemoryStatColumn("User Prefs", "${stats.preferenceCount} rules", ZaraSecondaryAccent)
                        }
                    }
                }
            }

            // Auto-Extraction Setting Row
            item {
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Autonomous Memory Extraction",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ZaraTextPrimary
                            )
                            Text(
                                text = "Automatically identify and save user facts, preferences, and names from conversation context.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZaraTextSecondary
                            )
                        }
                        Switch(
                            checked = autoExtract,
                            onCheckedChange = { viewModel.toggleAutoExtract() }
                        )
                    }
                }
            }

            // Search Bar
            item {
                ZaraTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = "Search neural memories...",
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = ZaraTextSecondary)
                    }
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("All (${stats.longTermMemoryCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZaraPrimaryAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(MemoryCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setCategory(category) },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZaraPrimaryAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (memories.isEmpty()) {
                item {
                    ZaraEmptyState(
                        title = "No Matching Memories",
                        description = "Try searching for a different keyword or create a new memory fact.",
                        icon = Icons.Filled.Psychology
                    )
                }
            } else {
                items(memories, key = { it.id }) { memory ->
                    MemoryItemCard(
                        memory = memory,
                        onDelete = { viewModel.deleteMemory(memory.id) }
                    )
                }
            }
        }

        if (isAddingMemory) {
            AddMemorySheet(
                onDismiss = { isAddingMemory = false },
                onSave = { key, value, cat ->
                    viewModel.addMemory(key, value, cat)
                    isAddingMemory = false
                }
            )
        }
    }
}

@Composable
private fun MemoryStatColumn(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ZaraTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MemoryItemCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    ZaraCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memory.category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = ZaraPrimaryAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = memory.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZaraTextSecondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete Memory",
                    tint = ZaraError
                )
            }
        }
    }
}

@Composable
private fun AddMemorySheet(
    onDismiss: () -> Unit,
    onSave: (String, String, MemoryCategory) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MemoryCategory.FACT) }

    ZaraModalSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Store Custom Neural Memory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ZaraTextPrimary
            )

            ZaraTextField(
                value = key,
                onValueChange = { key = it },
                label = "Memory Key / Identifier",
                placeholder = "e.g. favorite_ide_theme"
            )

            ZaraTextField(
                value = value,
                onValueChange = { value = it },
                label = "Memory Information Content",
                placeholder = "e.g. Cyberpunk Purple with Jetpack Compose"
            )

            ZaraButton(
                onClick = { onSave(key, value, selectedCategory) },
                enabled = key.isNotBlank() && value.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save to Long-Term Memory")
            }
        }
    }
}
