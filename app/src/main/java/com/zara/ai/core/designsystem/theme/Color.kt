package com.zara.ai.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand Neon & Cyber Accent Colors
val ZaraCyan = Color(0xFF00E5FF)
val ZaraCyanVariant = Color(0xFF00B0FF)
val ZaraCyanGlow = Color(0x3300E5FF)
val ZaraPurple = Color(0xFF7C4DFF)
val ZaraPurpleVariant = Color(0xFF651FFF)
val ZaraEmerald = Color(0xFF00E676)
val ZaraAmber = Color(0xFFFFD600)
val ZaraCrimson = Color(0xFFFF1744)

// Dark Theme Surfaces
val ZaraDarkBackground = Color(0xFF080B10)
val ZaraDarkSurface = Color(0xFF10141E)
val ZaraDarkSurfaceVariant = Color(0xFF192030)
val ZaraDarkCard = Color(0xFF141A26)
val ZaraDarkBorder = Color(0xFF222B3E)
val ZaraDarkTextPrimary = Color(0xFFF0F4FA)
val ZaraDarkTextSecondary = Color(0xFF90A0B8)

// Light Theme Surfaces
val ZaraLightBackground = Color(0xFFF4F7FB)
val ZaraLightSurface = Color(0xFFFFFFFF)
val ZaraLightSurfaceVariant = Color(0xFFE8EEF6)
val ZaraLightCard = Color(0xFFFFFFFF)
val ZaraLightBorder = Color(0xFFD6DFEC)
val ZaraLightTextPrimary = Color(0xFF0D141F)
val ZaraLightTextSecondary = Color(0xFF5B697A)

// Glassmorphic Overlays
val ZaraGlassBackgroundDark = Color(0xD910141E)
val ZaraGlassBackgroundLight = Color(0xCCFFFFFF)

val ZaraDarkColorScheme = darkColorScheme(
    primary = ZaraCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF80F2FF),
    secondary = ZaraPurple,
    onSecondary = Color(0xFF24005B),
    secondaryContainer = Color(0xFF3B0086),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = ZaraEmerald,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005327),
    onTertiaryContainer = Color(0xFF6CF89B),
    error = ZaraCrimson,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = ZaraDarkBackground,
    onBackground = ZaraDarkTextPrimary,
    surface = ZaraDarkSurface,
    onSurface = ZaraDarkTextPrimary,
    surfaceVariant = ZaraDarkSurfaceVariant,
    onSurfaceVariant = ZaraDarkTextSecondary,
    outline = ZaraDarkBorder
)

val ZaraLightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADDFF),
    onSecondaryContainer = Color(0xFF21005D),
    tertiary = Color(0xFF006D34),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF95F8AE),
    onTertiaryContainer = Color(0xFF00210B),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = ZaraLightBackground,
    onBackground = ZaraLightTextPrimary,
    surface = ZaraLightSurface,
    onSurface = ZaraLightTextPrimary,
    surfaceVariant = ZaraLightSurfaceVariant,
    onSurfaceVariant = ZaraLightTextSecondary,
    outline = ZaraLightBorder
)
