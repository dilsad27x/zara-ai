package com.zara.ai.core.designsystem.animation

import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext

object ZaraAnimations {

    // Easing curves
    val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
    val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

    // Standard durations (ms)
    const val DURATION_FAST = 180
    const val DURATION_MEDIUM = 300
    const val DURATION_EXPAND = 450
    const val DURATION_ORB_CYCLE = 2400

    fun screenEnter(): EnterTransition =
        fadeIn(animationSpec = tween(DURATION_MEDIUM, easing = LinearOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(DURATION_MEDIUM, easing = EaseOutQuart)
                )

    fun screenExit(): ExitTransition =
        fadeOut(animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing)) +
                slideOutVertically(
                    targetOffsetY = { -it / 12 },
                    animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing)
                )

    fun dialogEnter(): EnterTransition =
        fadeIn(animationSpec = tween(DURATION_FAST)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                )

    fun dialogExit(): ExitTransition =
        fadeOut(animationSpec = tween(DURATION_FAST)) +
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(DURATION_FAST)
                )
}

/**
 * Checks whether Android system reduced motion setting is active.
 */
@Composable
fun isReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        scale == 0f
    } catch (_: Exception) {
        false
    }
}

/**
 * GPU-accelerated tactile press effect for interactive elements.
 */
fun Modifier.zaraPressEffect(
    pressedScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isReduced = isReducedMotionEnabled()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isReduced) pressedScale else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "zara_press_scale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
