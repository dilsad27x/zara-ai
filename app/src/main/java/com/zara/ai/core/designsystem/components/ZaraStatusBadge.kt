package com.zara.ai.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zara.ai.core.designsystem.theme.ZaraAmber
import com.zara.ai.core.designsystem.theme.ZaraCrimson
import com.zara.ai.core.designsystem.theme.ZaraCyan
import com.zara.ai.core.designsystem.theme.ZaraEmerald

enum class ZaraStatusType {
    ONLINE, THINKING, BUSY, OFFLINE, ERROR
}

@Composable
fun ZaraStatusBadge(
    status: ZaraStatusType,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val (color, defaultText) = when (status) {
        ZaraStatusType.ONLINE -> ZaraEmerald to "Ready"
        ZaraStatusType.THINKING -> ZaraCyan to "Thinking"
        ZaraStatusType.BUSY -> ZaraAmber to "Executing"
        ZaraStatusType.OFFLINE -> Color.Gray to "Offline"
        ZaraStatusType.ERROR -> ZaraCrimson to "Error"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (status == ZaraStatusType.THINKING || status == ZaraStatusType.BUSY) pulseAlpha else 1.0f)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label ?: defaultText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
