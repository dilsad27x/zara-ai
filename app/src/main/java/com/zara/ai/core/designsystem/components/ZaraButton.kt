package com.zara.ai.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.zara.ai.core.designsystem.theme.ZaraCyan

enum class ZaraButtonVariant {
    PRIMARY, SECONDARY, OUTLINED, DANGER
}

@Composable
fun ZaraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ZaraButtonVariant = ZaraButtonVariant.PRIMARY,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1.0f, label = "button_scale")

    val buttonColors = when (variant) {
        ZaraButtonVariant.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        ZaraButtonVariant.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        ZaraButtonVariant.OUTLINED -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        ZaraButtonVariant.DANGER -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }

    if (variant == ZaraButtonVariant.OUTLINED) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .height(48.dp),
            enabled = enabled,
            shape = shape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
            colors = buttonColors,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            content = content
        )
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .height(48.dp),
            enabled = enabled,
            shape = shape,
            colors = buttonColors,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            content = content
        )
    }
}
