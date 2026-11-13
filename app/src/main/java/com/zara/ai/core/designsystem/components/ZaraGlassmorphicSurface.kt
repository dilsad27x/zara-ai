package com.zara.ai.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSurface

@Composable
fun ZaraGlassmorphicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    backgroundColor: Color = ZaraSurface.copy(alpha = 0.85f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientBorder = Brush.linearGradient(
        colors = listOf(
            ZaraPrimaryAccent.copy(alpha = 0.45f),
            ZaraElectricBlue.copy(alpha = 0.25f),
            ZaraBorder.copy(alpha = 0.60f)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(borderWidth, gradientBorder), shape = shape),
        content = content
    )
}
