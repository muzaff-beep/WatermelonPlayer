package com.watermelon.player

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.watermelon.player.config.EditionManager
import com.watermelon.player.util.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.watermelon.player.security.TamperDetector

// Add to onCreate()
CrashReporter.initialize(this)
LowMemoryHandler.register(this)

// Add WorkManager config if needed

@HiltAndroidApp
class WatermelonApp : Application(), Configuration.Provider {

    // ... existing code ...

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Tamper detection on startup
        if (BuildConfig.RELEASE && TamperDetector.isTampered(this)) {
            TamperDetector.handleTamper()
            return // Exit early
        }

        EditionManager.initialize(this)
        PerformanceMonitor.startMonitoring(this)
        PerformanceMonitor.configureCoilForLowRam(this)

        // ... rest unchanged ...
    }
}


/**
 * WatermelonApp.kt
 * Purpose: Application singleton – entry point for app-wide initialization.
 * Responsibilities:
 *   - Hilt/DI setup (annotated with @HiltAndroidApp)
 *   - Edition detection (Iran vs Global) via EditionManager
 *   - Start performance monitoring (RAM/GC watchdog)
 *   - Configure Coil for low-RAM (critical for TVs and A23)
 *   - Future: WorkManager config, crash reporting stub (no remote)
 *
 * Why here? Runs once on process start – perfect for one-time heavy init.
 * Iran-first: No Crashlytics, no Analytics, no network on startup.
 */

@HiltAndroidApp
class WatermelonApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory   // For future IndexingService workers

    companion object {
        // Global access to app context if needed (use sparingly – prefer DI)
        lateinit var instance: WatermelonApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Step 1: Initialize EditionManager – determines entire app behavior
        // Must run first – many modules depend on EditionManager.isIranEdition
        EditionManager.initialize(this)

        // Step 2: Start performance monitoring – critical for low-RAM devices
        // Enforces GC, clears Coil caches when RAM > 150MB idle
        PerformanceMonitor.startMonitoring(this)

        // Step 3: Configure Coil globally for low-RAM policy
        // Disables memory cache (L1), uses minimal disk cache
        // Called here so all ImageLoaders inherit the config
        PerformanceMonitor.configureCoilForLowRam(this)

        // Step 4: Future hooks
        // TODO: Initialize Room database migration
        // TODO: Pre-warm indexing queue if USB detected
        // TODO: Check for manual APK update flag
    }

    // WorkManager configuration – required when using Hilt workers
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

    // Optional: Low-memory handler – aggressive cleanup
    override fun onLowMemory() {
        super.onLowMemory()
        // Force immediate GC and cache clear on system low-memory signal
        PerformanceMonitor.enforceLowRamPolicy()
    }

    // Trim memory callback – used by Android when app in background
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_BACKGROUND) {
            // Aggressive cleanup when app not visible
            PerformanceMonitor.enforceLowRamPolicy()
        }
    }
}
