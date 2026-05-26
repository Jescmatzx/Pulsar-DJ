package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = PsyPink,
    tertiary = ToxicGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCard,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextHigh,
    onSurface = TextHigh,
    onSurfaceVariant = TextHigh
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the professional DJ setup vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our intentional neon styling hierarchy
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
