package com.example.productivitylauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Placeholder palette (see docs/DESIGN.md). The look is expected to change,
 * so always use these tokens and never hard-code colors in screens.
 */
object AppColors {
    val Background = Color(0xFF0E0E0E)
    val Text = Color(0xFFD6D6D6)
    val Muted = Color(0xFF7D7D7D)
    val Line = Color(0xFF2A2A2A)

    /** Reserved for gated (distracting) apps. Do not use it for anything else. */
    val Danger = Color(0xFFFF6B5E)
}

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppColors.Text,
            background = AppColors.Background,
            surface = AppColors.Background,
            onBackground = AppColors.Text,
            onSurface = AppColors.Text,
        ),
        content = content,
    )
}
