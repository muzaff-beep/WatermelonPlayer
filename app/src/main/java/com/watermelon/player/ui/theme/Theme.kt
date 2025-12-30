package com.watermelon.player.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WatermelonPrimary,
    secondary = WatermelonSecondary,
    tertiary = WatermelonTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = WatermelonPrimary,
    secondary = WatermelonSecondary,
    tertiary = WatermelonTertiary
)

@Composable
fun WatermelonTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
