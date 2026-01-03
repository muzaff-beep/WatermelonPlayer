
package com.watermelon.player

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.watermelon.player.config.EditionManager
import com.watermelon.player.util.PerformanceMonitor
import com.watermelon.player.security.TamperDetector
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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

        if (BuildConfig.DEBUG.not() && TamperDetector.isTampered(this)) { // Fixed RELEASE reference
            TamperDetector.handleTamper()
            return
        }

        EditionManager.initialize(this)
        PerformanceMonitor.startMonitoring(this)
        PerformanceMonitor.configureCoilForLowRam(this)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onLowMemory() {
        super.onLowMemory()
        PerformanceMonitor.enforceLowRamPolicy()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_BACKGROUND) {
            PerformanceMonitor.enforceLowRamPolicy()
        }
    }
}
