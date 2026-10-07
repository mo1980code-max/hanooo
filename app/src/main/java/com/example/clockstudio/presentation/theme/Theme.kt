package com.example.clockstudio.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClockStudioDarkColors = darkColorScheme(
    primary = Color(0xFF00B89A),
    onPrimary = Color(0xFF001F1A),
    primaryContainer = Color(0xFF004D41),
    onPrimaryContainer = Color(0xFF9BFFEA),
    secondary = Color(0xFF37D9F2),
    onSecondary = Color(0xFF002027),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF111111),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF454545),
    error = Color(0xFFFF6B6B),
)

@Composable
fun ClockStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ClockStudioDarkColors,
        typography = Typography(),
        content = content,
    )
}
