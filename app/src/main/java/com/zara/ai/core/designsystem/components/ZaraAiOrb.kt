package com.zara.ai.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zara.ai.core.designsystem.animation.isReducedMotionEnabled
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraError
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSecondaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSuccess
import com.zara.ai.core.designsystem.theme.ZaraWarning
import kotlin.math.cos
import kotlin.math.sin

/**
 * All 8 mandatory operational states for ZARA's AI Orb.
 */
enum class OrbState {
    IDLE,
    LISTENING,
    PROCESSING,
    THINKING,
    SPEAKING,
    EXECUTING,
    PAUSED,
    ERROR
}

/**
 * Premium multi-layered GPU-accelerated animated central AI orb.
 *
 * @param state Current operational state driven by real application activity.
 * @param modifier Composable layout modifier.
 * @param size Diameter of the orb container.
 * @param audioLevel Real audio amplitude (0.0f to 1.0f) during LISTENING or SPEAKING.
 */
@Composable
fun ZaraAiOrb(
    state: OrbState,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    audioLevel: Float = 0.0f
) {
    val isReducedMotion = isReducedMotionEnabled()

    // Primary state colors
    val (targetCoreColor, targetAuraColor, targetRingColor) = when (state) {
        OrbState.IDLE -> Triple(ZaraPrimaryAccent, ZaraSecondaryAccent, ZaraElectricBlue)
        OrbState.LISTENING -> Triple(ZaraElectricBlue, ZaraPrimaryAccent, Color(0xFF60A5FA))
        OrbState.PROCESSING -> Triple(ZaraSecondaryAccent, ZaraElectricBlue, Color(0xFFA78BFA))
        OrbState.THINKING -> Triple(ZaraPrimaryAccent, Color(0xFFC084FC), ZaraElectricBlue)
        OrbState.SPEAKING -> Triple(Color(0xFF818CF8), ZaraPrimaryAccent, Color(0xFF38BDF8))
        OrbState.EXECUTING -> Triple(ZaraSuccess, ZaraElectricBlue, Color(0xFF4ADE80))
        OrbState.PAUSED -> Triple(ZaraWarning, Color(0xFFB45309), Color(0xFFFBBF24))
        OrbState.ERROR -> Triple(ZaraError, Color(0xFF991B1B), Color(0xFFF87171))
    }

    val coreColor by animateColorAsState(
        targetValue = targetCoreColor,
        animationSpec = tween(400),
        label = "orb_core_color"
    )
    val auraColor by animateColorAsState(
        targetValue = targetAuraColor,
        animationSpec = tween(400),
        label = "orb_aura_color"
    )
    val ringColor by animateColorAsState(
        targetValue = targetRingColor,
        animationSpec = tween(400),
        label = "orb_ring_color"
    )

    // Base speed and scaling based on state
    val cycleDuration = when (state) {
        OrbState.IDLE -> 3200
        OrbState.LISTENING -> 1600
        OrbState.PROCESSING -> 1100
        OrbState.THINKING -> 1400
        OrbState.SPEAKING -> 1200
        OrbState.EXECUTING -> 900
        OrbState.PAUSED -> 4500
        OrbState.ERROR -> 800
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orb_transition")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDuration / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDuration * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    // React to live audio activity when in listening/speaking states
    val audioBoost = if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
        (audioLevel.coerceIn(0f, 1f) * 0.25f)
    } else {
        0f
    }

    val effectivePulse = if (isReducedMotion) 1.0f else (pulse + audioBoost)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f

            // 1. Outer Ambient Glow Field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.40f * effectivePulse),
                        coreColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 2. Orbital Particle Rings
            val ringRadius = baseRadius * 0.72f * effectivePulse
            drawCircle(
                color = ringColor.copy(alpha = 0.35f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic orbit nodes
            if (!isReducedMotion) {
                val nodeCount = if (state == OrbState.EXECUTING || state == OrbState.THINKING) 6 else 3
                for (i in 0 until nodeCount) {
                    val angleRad = Math.toRadians((rotationAngle + (i * 360.0 / nodeCount)).toDouble())
                    val nodeX = center.x + (ringRadius * cos(angleRad)).toFloat()
                    val nodeY = center.y + (ringRadius * sin(angleRad)).toFloat()
                    drawCircle(
                        color = ringColor.copy(alpha = 0.85f),
                        radius = 3.5.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 3. Inner Kinetic Sphere Core
            val coreRadius = baseRadius * 0.48f * effectivePulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        coreColor,
                        auraColor,
                        Color.Transparent
                    ),
                    center = Offset(center.x - coreRadius * 0.25f, center.y - coreRadius * 0.25f),
                    radius = coreRadius * 1.2f
                ),
                radius = coreRadius,
                center = center
            )

            // 4. Dimensional Specular Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - coreRadius * 0.35f, center.y - coreRadius * 0.35f),
                    radius = coreRadius * 0.45f
                ),
                radius = coreRadius * 0.35f,
                center = Offset(center.x - coreRadius * 0.3f, center.y - coreRadius * 0.3f)
            )
        }
    }
}
