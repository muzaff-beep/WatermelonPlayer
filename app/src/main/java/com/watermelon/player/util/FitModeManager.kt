package com.watermelon.player.util

import android.content.Context
import androidx.media3.ui.AspectRatioFrameLayout

object FitModeManager {

    const val MODE_FIT = AspectRatioFrameLayout.RESIZE_MODE_FIT
    const val MODE_ZOOM = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    const val MODE_STRETCH = AspectRatioFrameLayout.RESIZE_MODE_FILL

    private var globalMode = MODE_ZOOM
    private var rememberPerVideo = true

    fun getModeForVideo(context: Context, videoPath: String): Int {
        if (!rememberPerVideo) return globalMode
        // TODO: Load from preferences or Room
        return globalMode
    }

    fun saveModeForVideo(context: Context, videoPath: String, mode: Int) {
        if (!rememberPerVideo) return
        // TODO: Save to preferences or Room
    }

    fun setGlobalMode(mode: Int) { globalMode = mode }
    fun setRememberPerVideo(enabled: Boolean) { rememberPerVideo = enabled }
}
