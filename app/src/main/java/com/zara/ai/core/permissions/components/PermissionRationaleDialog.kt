package com.zara.ai.core.permissions.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.zara.ai.core.designsystem.components.ZaraButton
import com.zara.ai.core.designsystem.components.ZaraButtonVariant
import com.zara.ai.core.permissions.model.ZaraPermission

@Composable
fun PermissionRationaleDialog(
    permission: ZaraPermission,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = permission.title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = permission.rationale, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            ZaraButton(
                onClick = onConfirm,
                variant = ZaraButtonVariant.PRIMARY
            ) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
