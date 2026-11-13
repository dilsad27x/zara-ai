package com.zara.ai.features.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MemoryEntity
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraEmptyState
import com.zara.ai.core.designsystem.components.ZaraModalSheet
import com.zara.ai.core.designsystem.components.ZaraTextField

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val memories by viewModel.memories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var isAddingMemory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(title = "Neural Memory")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingMemory = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Memory")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("All") }
                    )
                }
                items(MemoryCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category.name) }
                    )
                }
            }

            if (memories.isEmpty()) {
                ZaraEmptyState(
                    title = "No Memories Stored",
                    description = "ZARA will store extracted facts, user preferences, and knowledge here.",
                    icon = Icons.Filled.Psychology
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memories, key = { it.id }) { memory ->
                        MemoryItemCard(
                            memory = memory,
                            onDelete = { viewModel.deleteMemory(memory.id) }
                        )
                    }
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
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = memory.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
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
                text = "Store New Memory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ZaraTextField(
                value = key,
                onValueChange = { key = it },
                label = "Key / Identifier",
                placeholder = "e.g. user_project_name"
            )

            ZaraTextField(
                value = value,
                onValueChange = { value = it },
                label = "Value / Information",
                placeholder = "e.g. ZARA AI Engine"
            )

            ZaraButton(
                onClick = { onSave(key, value, selectedCategory) },
                enabled = key.isNotBlank() && value.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save to Memory")
            }
        }
    }
}
