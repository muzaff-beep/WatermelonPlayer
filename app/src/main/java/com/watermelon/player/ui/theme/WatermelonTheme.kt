package com.watermelon.player.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * WatermelonTheme.kt
 * Purpose: Central theme definition for entire app.
 * Features:
 *   - Material3 compliant
 *   - Dynamic color support (Android 12+) for modern look
 *   - Full RTL (Right-to-Left) mirroring for Persian/Arabic
 *   - Custom typography with Persian/Arabic/Kurdish font support
 *   - Dark/Light mode auto + manual
 *   - Status bar / navigation bar color sync
 *
 * Iran priority: Fonts loaded from assets/res/font (Yekan, Vazir for Persian clarity)
 */

private val DarkColorScheme = darkColorScheme(
    primary = WatermelonRedDark,
    secondary = WatermelonGreenDark,
    tertiary = WatermelonAccentDark,
    background = PureBlack,
    surface = SurfaceDark,
    onPrimary = White,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = WatermelonRedLight,
    secondary = WatermelonGreenLight,
    tertiary = WatermelonAccentLight,
    background = White,
    surface = SurfaceLight,
    onPrimary = Black,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun WatermelonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color enabled on Android 12+ (S) for wallpaper-based palette
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Force RTL layout direction for Persian/Arabic regardless of system setting
    // Critical for correct UI flow in Iran edition
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            activity.window?.decorView?.layoutDirection = android.util.LayoutDirection.RTL
        }
    }

    // Sync status bar color with theme
    val view2 = LocalView.current
    if (!view2.isInEditMode) {
        SideEffect {
            val window = (view2.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view2).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PersianTypography, // Custom Typeface with Yekan/Vazir/Noto
        shapes = Shapes,
        content = content
    )
}
