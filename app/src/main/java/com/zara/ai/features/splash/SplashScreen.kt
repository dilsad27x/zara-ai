package com.zara.ai.features.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSecondaryAccent
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary

@Composable
fun SplashScreen(
    onInitialized: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val initState by viewModel.initState.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(initState) {
        if (initState is InitializationState.Ready) {
            onInitialized()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZaraBackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        // Ambient backdrop light
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ZaraPrimaryAccent.copy(alpha = 0.25f),
                            ZaraElectricBlue.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            // Stylized Geometric ZARA Monogram Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(ZaraPrimaryAccent, ZaraSecondaryAccent, ZaraElectricBlue)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Z",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ZARA AI",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = ZaraTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Next-Generation Autonomous Intelligence",
                style = MaterialTheme.typography.bodyMedium,
                color = ZaraTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            when (val state = initState) {
                is InitializationState.Initializing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ZaraPrimaryAccent,
                        strokeWidth = 2.5.dp
                    )
                }
                is InitializationState.Error -> {
                    Text(
                        text = "Initialization Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is InitializationState.Ready -> {
                    // Ready state triggers navigation effect
                }
            }
        }
    }
}
