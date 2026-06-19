package com.hautt.playintegrity.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// --- Nexus Trust System palette (from Stitch design system) ---
val SecurityBlue = Color(0xFF1A73E8)      // primary actions / branding
val SecurityBlueDeep = Color(0xFF005BBF)
val IntegrityGreen = Color(0xFF006D3A)    // verified / secure states
val WarningYellow = Color(0xFF7A5800)     // pending / caution
val DangerRed = Color(0xFFBA1A1A)         // failed / untrusted

val Background = Color(0xFFF8F9FA)
val SurfaceWhite = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF191C1D)
val OnSurfaceVariant = Color(0xFF414754)
val Outline = Color(0xFF727785)
val OutlineVariant = Color(0xFFC1C6D6)
val InverseSurface = Color(0xFF2E3132)
val InverseOnSurface = Color(0xFFF0F1F2)

val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF3F4F5)
val SurfaceContainer = Color(0xFFEDEEEF)
val SurfaceContainerHigh = Color(0xFFE7E8E9)
val SurfaceContainerHighest = Color(0xFFE1E3E4)

val SecondaryContainer = Color(0xFF7FF8A8)
val OnSecondaryContainer = Color(0xFF00723D)
val TertiaryContainer = Color(0xFFFFDEA3)
val OnTertiaryContainer = Color(0xFF5D4200)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

private val NexusColorScheme = lightColorScheme(
    primary = SecurityBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = IntegrityGreen,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = WarningYellow,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = DangerRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnSurface,
    surface = SurfaceWhite,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
)

// JetBrains Mono is the design's "technical data" font; Monospace is the
// closest bundled equivalent and carries the same raw-immutable-data signal.
private val Mono = FontFamily.Monospace

private val NexusTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Mono, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Mono, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Mono, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Mono, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp
    ),
)

@Composable
fun PlayIntegrityTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = NexusColorScheme,
        typography = NexusTypography,
        content = content
    )
}
