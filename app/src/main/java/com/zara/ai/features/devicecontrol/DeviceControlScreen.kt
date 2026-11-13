package com.zara.ai.features.devicecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar

@Composable
fun DeviceControlScreen(
    viewModel: DeviceControlViewModel = hiltViewModel()
) {
    val metrics by viewModel.deviceMetrics.collectAsState()
    val volume by viewModel.currentVolume.collectAsState()

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "Android Device Control",
                actions = {
                    IconButton(onClick = { viewModel.refreshStatus() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
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
                        text = "Hardware Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    metrics?.let {
                        Text("Model: ${it.deviceModel}", style = MaterialTheme.typography.bodyMedium)
                        Text("System: ${it.osVersion}", style = MaterialTheme.typography.bodyMedium)
                        Text("Battery: ${it.batteryLevel}% (Charging: ${it.isCharging})", style = MaterialTheme.typography.bodyMedium)
                        Text("RAM: ${it.availableRamMb} MB available / ${it.totalRamMb} MB total", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ZaraButton(
                        onClick = { viewModel.copyDiagnostics() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Copy System Report to Clipboard")
                    }
                }
            }

            item {
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Audio Stream Volume",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Volume Level: $volume", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = volume.toFloat(),
                        onValueChange = { viewModel.setVolume(it.toInt()) },
                        valueRange = 0f..15f,
                        steps = 14
                    )
                }
            }
        }
    }
}
