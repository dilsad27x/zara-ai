package com.zara.ai.features.permissions

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.zara.ai.core.permissions.model.ZaraPermission

@Composable
fun PermissionCenterScreen(
    viewModel: PermissionCenterViewModel = hiltViewModel()
) {
    val permissionStates by viewModel.permissionStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh when returning from Android Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            ZaraCenterAlignedAppBar(
                title = "Permission Center",
                actions = {
                    IconButton(onClick = { viewModel.refreshPermissions() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Permissions",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Android System & Privacy Privileges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZaraTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ZARA adheres to strict privacy isolation. Permissions are only utilized for explicit autonomous features authorized by you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZaraTextSecondary
                )
            }

            items(permissionStates, key = { it.permission.name }) { itemState ->
                PermissionCard(
                    permission = itemState.permission,
                    isGranted = itemState.isGranted,
                    onConfigure = { viewModel.openPermissionSettings(itemState.permission) }
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    permission: ZaraPermission,
    isGranted: Boolean,
    onConfigure: () -> Unit
) {
    ZaraCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ZaraSurfaceVariant, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = if (isGranted) ZaraSuccess else ZaraPrimaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ZaraTextPrimary
                    )
                    Text(
                        text = permission.category.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = ZaraElectricBlue
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isGranted) ZaraSuccess.copy(alpha = 0.15f) else ZaraError.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGranted) "GRANTED" else "NOT GRANTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGranted) ZaraSuccess else ZaraError,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = permission.description,
            style = MaterialTheme.typography.bodySmall,
            color = ZaraTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onConfigure,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isGranted) ZaraTextSecondary else ZaraPrimaryAccent
                )
            ) {
                Text(if (isGranted) "Manage in Settings" else "Grant in Settings")
            }
        }
    }
}
