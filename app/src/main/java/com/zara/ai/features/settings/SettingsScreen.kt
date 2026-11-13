package com.zara.ai.features.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraCard
import com.zara.ai.core.designsystem.components.ZaraCenterAlignedAppBar
import com.zara.ai.core.designsystem.components.ZaraTextField
import com.zara.ai.core.designsystem.theme.ThemeMode
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSecondaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSuccess
import com.zara.ai.core.designsystem.theme.ZaraSurface
import com.zara.ai.core.designsystem.theme.ZaraSurfaceVariant
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary

@Composable
fun SettingsScreen(
    onNavigateToPermissions: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeConfig by viewModel.themeConfig.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var newKeyInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(title = "Settings & Preferences")
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
            // 1. Appearance & Theme Mode
            item {
                SettingsSectionHeader(title = "1. Appearance", icon = Icons.Filled.Palette)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Theme Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZaraPrimaryAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AMOLED Pure Black", style = MaterialTheme.typography.bodyMedium, color = ZaraTextPrimary)
                            Text("Maximizes battery conservation on OLED displays", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                        }
                        Switch(
                            checked = themeConfig.useAmoledBlack,
                            onCheckedChange = { viewModel.setAmoledBlack(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ZaraPrimaryAccent)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dynamic Material You", style = MaterialTheme.typography.bodyMedium, color = ZaraTextPrimary)
                            Text("Derive accent palette from wallpaper (Android 12+)", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                        }
                        Switch(
                            checked = themeConfig.useDynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ZaraPrimaryAccent)
                        )
                    }
                }
            }

            // 2. Language Selection
            item {
                SettingsSectionHeader(title = "2. Language", icon = Icons.Filled.Language)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Interface & Reasoning Language", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    val languages = listOf("English (US)", "Spanish", "French", "German", "Japanese")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        languages.take(3).forEach { lang ->
                            FilterChip(
                                selected = uiState.selectedLanguage == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                label = { Text(lang, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZaraPrimaryAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 3. Voice Settings
            item {
                SettingsSectionHeader(title = "3. Voice Synthesis", icon = Icons.Filled.Mic)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Active Neural Voice Model", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Model: ${uiState.selectedVoiceModel}", style = MaterialTheme.typography.bodyMedium, color = ZaraElectricBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    val voiceModels = listOf("Aura Neural HD", "Cyber Radiance", "Echo Studio")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        voiceModels.forEach { model ->
                            FilterChip(
                                selected = uiState.selectedVoiceModel == model,
                                onClick = { viewModel.setVoiceModel(model) },
                                label = { Text(model, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZaraPrimaryAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 4. AI Provider & Hardware Keystore Settings
            item {
                SettingsSectionHeader(title = "4. AI Provider & API Credentials", icon = Icons.Filled.SmartToy)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Inference Endpoint", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Current: ${uiState.aiProvider}", style = MaterialTheme.typography.bodyMedium, color = ZaraPrimaryAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Master API Key Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                    Text("Protected: ${uiState.apiKeyMasked}", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    ZaraTextField(
                        value = newKeyInput,
                        onValueChange = { newKeyInput = it },
                        label = "Update Master Key",
                        placeholder = "sk-..."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ZaraButton(
                        onClick = {
                            viewModel.saveApiKey(newKeyInput)
                            newKeyInput = ""
                        },
                        enabled = newKeyInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Key in Hardware Keystore")
                    }
                }
            }

            // 5. Privacy & Biometrics
            item {
                SettingsSectionHeader(title = "5. Privacy & Security", icon = Icons.Filled.Fingerprint)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biometric Settings Lock", style = MaterialTheme.typography.bodyMedium, color = ZaraTextPrimary)
                            Text("Require fingerprint before displaying credentials", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                        }
                        Switch(
                            checked = uiState.isBiometricLockEnabled,
                            onCheckedChange = { viewModel.toggleBiometricLock() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ZaraPrimaryAccent)
                        )
                    }
                }
            }

            // 6. Floating Assistant Settings
            item {
                SettingsSectionHeader(title = "6. Floating Assistant", icon = Icons.Filled.Layers)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Floating Mini Orb", style = MaterialTheme.typography.bodyMedium, color = ZaraTextPrimary)
                            Text("Render assistant orb over Android applications", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                        }
                        Switch(
                            checked = uiState.isMiniOrbEnabled,
                            onCheckedChange = { viewModel.toggleMiniOrb() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ZaraPrimaryAccent)
                        )
                    }
                }
            }

            // 7. Notification Settings
            item {
                SettingsSectionHeader(title = "7. Notification Preferences", icon = Icons.Filled.Notifications)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Autonomous Task Notifications", style = MaterialTheme.typography.bodyMedium, color = ZaraTextPrimary)
                            Text("Alert upon task or build completion in background", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                        }
                        Switch(
                            checked = uiState.taskNotificationEnabled,
                            onCheckedChange = { viewModel.toggleTaskNotifications() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ZaraPrimaryAccent)
                        )
                    }
                }
            }

            // 8. Permission Center Shortcut
            item {
                SettingsSectionHeader(title = "8. Permissions", icon = Icons.Filled.Security)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToPermissions
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Permission Center", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                            Text("Inspect microphone, accessibility & overlay status", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ZaraPrimaryAccent)
                    }
                }
            }

            // 9. About ZARA AI & Build Information
            item {
                SettingsSectionHeader(title = "9. About ZARA AI", icon = Icons.Filled.Info)
                Spacer(modifier = Modifier.height(6.dp))
                ZaraCard(modifier = Modifier.fillMaxWidth()) {
                    Text("ZARA Autonomous Mobile Intelligence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Architecture: Clean Architecture Modular Monolith", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                    Text("UI Framework: Jetpack Compose + Material 3", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                    Text("Build Variant: ${uiState.buildVariant}", style = MaterialTheme.typography.bodySmall, color = ZaraElectricBlue)
                    Text("API Endpoint: ${uiState.apiEndpoint}", style = MaterialTheme.typography.bodySmall, color = ZaraTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = ZaraPrimaryAccent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ZaraTextPrimary)
    }
}
