package com.watermelon.player

import android.app.Application
import android.os.Build
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.watermelon.player.config.EditionManager
import com.watermelon.player.database.MediaDatabase
import com.watermelon.player.di.appModule
import com.watermelon.player.security.CrashReporter
import com.watermelon.player.security.TamperDetector
import com.watermelon.player.util.PerformanceMonitor
import io.insert-koin.android.ext.koin.androidContext
import io.insert-koin.core.context.startKoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@UnstableApi class WatermelonApp : Application(), ImageLoaderFactory {

    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize DI
        startKoin {
            androidContext(this@WatermelonApp)
            modules(appModule)
        }

        // Initialize edition
        EditionManager.initialize(this)

        // Security checks
        TamperDetector.checkIntegrity(this)

        // Performance monitoring
        PerformanceMonitor.initialize()

        // Initialize database
        MediaDatabase.initialize(this)

        // Start background tasks
        appScope.launch {
            // Pre-warm frequently used components
            preWarmComponents()

            // Check for updates
            checkForUpdates()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

    private fun preWarmComponents() {
        // Pre-warm ExoPlayer
        androidx.media3.exoplayer.ExoPlayer.Builder(this).build().release()

        // Pre-warm encryption
        javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
    }

    private fun checkForUpdates() {
        // Edition-specific update check
        when (EditionManager.getCurrentEdition()) {
            is EditionManager.Edition.Iran -> {
                // Check Iranian app stores
                com.watermelon.player.update.CafeBazaarUpdater.checkForUpdates(this)
            }
            is EditionManager.Edition.Global -> {
                // Check Google Play
                com.watermelon.player.update.GooglePlayUpdater.checkForUpdates(this)
            }
        }
    }

    override fun onTerminate() {
        // Clean up resources
        PerformanceMonitor.shutdown()
        super.onTerminate()
    }
}
