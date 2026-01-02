package com.watermelon.player.util

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context

/**
 * LowMemoryHandler.kt
 * Purpose: System-level low memory callback.
 * Registers in WatermelonApp.
 * Clears Coil caches, thumbnail bitmaps, GC.
 * Critical for TVs/A23 with 2-4GB RAM.
 */

object LowMemoryHandler : ComponentCallbacks2 {

    private lateinit var context: Context

    fun register(context: Context) {
        this.context = context.applicationContext
        context.registerComponentCallbacks(this)
    }

    override fun onTrimMemory(level: Int) {
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                PerformanceMonitor.enforceLowRamPolicy()
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // More aggressive
                PerformanceMonitor.enforceLowRamPolicy()
                System.gc()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}

    override fun onLowMemory() {
        PerformanceMonitor.enforceLowRamPolicy()
        System.gc()
    }
}
