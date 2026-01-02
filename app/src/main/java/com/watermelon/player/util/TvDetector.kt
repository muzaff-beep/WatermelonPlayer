package com.watermelon.player.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.getSystemService
import android.app.UiModeManager

/**
 * TvDetector.kt
 * Purpose: Detect if running on Android TV (Samsung A23 TV box, etc.).
 * Used to:
 *   - Show/hide GalleryScreen
 *   - Adjust grid columns
 *   - Enable remote handler
 *   - Larger fonts
 */

object TvDetector {

    fun isTvDevice(context: Context): Boolean {
        val uiModeManager = context.getSystemService<UiModeManager>()
        return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    fun hasTouchscreen(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.touchscreen")
    }

    fun isLeanback(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("android.software.leanback")
    }

    fun isTv(context: Context): Boolean {
        return isTvDevice(context) || isLeanback(context) && !hasTouchscreen(context)
    }
}
