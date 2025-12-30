package com.watermelon.player

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.watermelon.player.di.appModule
import io.insert-koin.android.ext.koin.androidContext
import io.insert-koin.core.context.startKoin
import timber.log.Timber

class WatermelonApp : Application(), ImageLoaderFactory {
    
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
        
        // Initialize edition detection (to be implemented in Batch 2)
        EditionManager.initialize(this)
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
