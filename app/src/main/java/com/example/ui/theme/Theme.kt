package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioGold,
    onPrimary = Color.Black,
    primaryContainer = StudioSurfaceVariant,
    onPrimaryContainer = StudioGold,
    secondary = StudioViolet,
    onSecondary = Color.White,
    secondaryContainer = StudioSurfaceVariant,
    onSecondaryContainer = StudioCyan,
    tertiary = StudioCyan,
    onTertiary = Color.Black,
    background = StudioBackground,
    onBackground = StudioTextPrimary,
    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioCardBorder,
    error = StudioHorrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
