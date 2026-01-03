package com.watermelon.player

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.watermelon.player.config.EditionManager
import com.watermelon.player.util.PerformanceMonitor
import com.watermelon.player.security.TamperDetector
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * WatermelonApp - Application singleton
 * 
 * Responsibilities:
 * - Hilt/DI setup (@HiltAndroidApp)
 * - Early tamper detection (critical for Iran release security)
 * - Edition initialization (Iran vs Global behavior)
 * - Performance monitoring (RAM/GC watchdog for low-end devices & TV)
 * - Coil low-RAM optimization
 * - WorkManager Hilt integration (for future background indexing)
 * - Memory pressure handling
 * 
 * Iran-first design: No Crashlytics, no remote analytics, no network on startup.
 */

@HiltAndroidApp
class WatermelonApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        lateinit var instance: WatermelonApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Step 1: Critical security - detect APK tampering early
        if (BuildConfig.RELEASE && TamperDetector.isTampered(this)) {
            TamperDetector.handleTamper()
            return  // Exit immediately - do not proceed
        }

        // Step 2: Edition detection - determines entire app flow
        EditionManager.initialize(this)

        // Step 3: Performance monitoring - essential for A23 and Android TV
        PerformanceMonitor.startMonitoring(this)

        // Step 4: Optimize Coil for low-RAM environments
        PerformanceMonitor.configureCoilForLowRam(this)

        // Future hooks (ready for expansion)
        // TODO: Initialize Room database
        // TODO: Pre-warm media indexing if storage detected
        // TODO: Check for manual update flag
    }

    // Required for Hilt + WorkManager integration
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    // Aggressive cleanup on system low memory signal
    override fun onLowMemory() {
        super.onLowMemory()
        PerformanceMonitor.enforceLowRamPolicy()
    }

    // Respond to Android background trim levels
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_BACKGROUND || level >= TRIM_MEMORY_MODERATE) {
            PerformanceMonitor.enforceLowRamPolicy()
        }
    }
}
