package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcadeColorScheme = darkColorScheme(
    primary = PacYellow,
    onPrimary = ArcadeDark,
    primaryContainer = PacYellowDark,
    onPrimaryContainer = Color.White,
    secondary = NeonAccent,
    onSecondary = ArcadeDark,
    secondaryContainer = ArcadeCard,
    onSecondaryContainer = NeonAccent,
    tertiary = GhostInky,
    background = ArcadeDark,
    onBackground = Color(0xFFF0F4FF),
    surface = ArcadeSurface,
    onSurface = Color(0xFFF0F4FF),
    surfaceVariant = ArcadeCard,
    onSurfaceVariant = Color(0xFFBAC5E8),
    outline = ArcadeBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcadeColorScheme,
        typography = Typography,
        content = content
    )
}
