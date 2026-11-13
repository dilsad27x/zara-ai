package com.zara.ai.features.websiteforge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraLoadingIndicator
import com.zara.ai.core.designsystem.components.ZaraTextField

@Composable
fun WebsiteForgeScreen(
    viewModel: WebsiteForgeViewModel = hiltViewModel()
) {
    val isForging by viewModel.isForging.collectAsState()
    val latestProject by viewModel.latestProject.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(title = "Website Forge")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Autonomous Website Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Specify a concept, design language, or business model, and ZARA AI will forge modern web code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ZaraTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = "e.g. Portfolio with glassmorphism and cyberpunk colors...",
                        label = "Project Description"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ZaraButton(
                        onClick = {
                            viewModel.forgeWebsite(promptInput)
                            promptInput = ""
                        },
                        enabled = promptInput.isNotBlank() && !isForging,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isForging) {
                            ZaraLoadingIndicator(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Forge Website")
                        }
                    }
                }
            }

            latestProject?.let { project ->
                item {
                    ZaraCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = project.htmlPreview,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
