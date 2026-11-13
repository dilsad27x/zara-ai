package com.zara.ai.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// ZARA AI Part 02 — Master Production Color System
// ============================================================================

// Core Palette
val ZaraBackgroundPrimary = Color(0xFF080810)
val ZaraBackgroundSecondary = Color(0xFF10101C)
val ZaraSurface = Color(0xFF171727)
val ZaraSurfaceVariant = Color(0xFF1F1F35)
val ZaraBorder = Color(0xFF282846)

// Accent Hierarchy
val ZaraPrimaryAccent = Color(0xFF8B5CF6)    // Vibrant Purple
val ZaraSecondaryAccent = Color(0xFF6366F1)  // Radiant Indigo
val ZaraElectricBlue = Color(0xFF3B82F6)     // Electric Blue
val ZaraVioletGlow = Color(0xFF7C3AED)

// Semantic Colors
val ZaraTextPrimary = Color(0xFFFFFFFF)
val ZaraTextSecondary = Color(0xFFA1A1B5)
val ZaraTextTertiary = Color(0xFF6B6B80)
val ZaraSuccess = Color(0xFF22C55E)
val ZaraError = Color(0xFFEF4444)
val ZaraWarning = Color(0xFFF59E0B)

// Glassmorphic Surface Overlays
val ZaraGlassSurfaceDark = Color(0xE6171727)
val ZaraGlassSurfaceLight = Color(0xCCFFFFFF)
val ZaraGlassBorder = Color(0x338B5CF6)

// Ambient Shaders & Gradients
val ZaraOrbGradient = Brush.linearGradient(
    colors = listOf(ZaraPrimaryAccent, ZaraSecondaryAccent, ZaraElectricBlue)
)

val ZaraGlassGradient = Brush.linearGradient(
    colors = listOf(
        ZaraPrimaryAccent.copy(alpha = 0.35f),
        ZaraSecondaryAccent.copy(alpha = 0.20f),
        Color.Transparent
    )
)

val ZaraNeonGlowGradient = Brush.radialGradient(
    colors = listOf(
        ZaraPrimaryAccent.copy(alpha = 0.40f),
        ZaraElectricBlue.copy(alpha = 0.15f),
        Color.Transparent
    )
)

// Legacy compatibility aliases to prevent breaking Part 01 references
val ZaraCyan = ZaraElectricBlue
val ZaraCyanVariant = ZaraElectricBlue
val ZaraCyanGlow = Color(0x333B82F6)
val ZaraPurple = ZaraPrimaryAccent
val ZaraPurpleVariant = ZaraSecondaryAccent
val ZaraEmerald = ZaraSuccess
val ZaraAmber = ZaraWarning
val ZaraCrimson = ZaraError
val ZaraDarkBackground = ZaraBackgroundPrimary
val ZaraDarkSurface = ZaraSurface
val ZaraDarkSurfaceVariant = ZaraSurfaceVariant
val ZaraDarkCard = ZaraSurface
val ZaraDarkBorder = ZaraBorder
val ZaraDarkTextPrimary = ZaraTextPrimary
val ZaraDarkTextSecondary = ZaraTextSecondary
val ZaraLightBackground = Color(0xFFF8F9FD)
val ZaraLightSurface = Color(0xFFFFFFFF)
val ZaraLightSurfaceVariant = Color(0xFFEEF2F9)
val ZaraLightCard = Color(0xFFFFFFFF)
val ZaraLightBorder = Color(0xFFD6DFEC)
val ZaraLightTextPrimary = Color(0xFF0F172A)
val ZaraLightTextSecondary = Color(0xFF475569)
val ZaraGlassBackgroundDark = ZaraGlassSurfaceDark
val ZaraGlassBackgroundLight = ZaraGlassSurfaceLight

// Material 3 Dark ColorScheme (Primary Theme)
val ZaraDarkColorScheme = darkColorScheme(
    primary = ZaraPrimaryAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = ZaraSecondaryAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = ZaraElectricBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = Color(0xFFDBEAFE),
    background = ZaraBackgroundPrimary,
    onBackground = ZaraTextPrimary,
    surface = ZaraSurface,
    onSurface = ZaraTextPrimary,
    surfaceVariant = ZaraSurfaceVariant,
    onSurfaceVariant = ZaraTextSecondary,
    outline = ZaraBorder,
    error = ZaraError,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

// Material 3 Light ColorScheme
val ZaraLightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = Color(0xFF2563EB),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDBEAFE),
    onTertiaryContainer = Color(0xFF172554),
    background = ZaraLightBackground,
    onBackground = ZaraLightTextPrimary,
    surface = ZaraLightSurface,
    onSurface = ZaraLightTextPrimary,
    surfaceVariant = ZaraLightSurfaceVariant,
    onSurfaceVariant = ZaraLightTextSecondary,
    outline = ZaraLightBorder,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)
