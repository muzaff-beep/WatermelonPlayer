package com.watermelon.player.di

import android.content.Context
import com.watermelon.player.config.EditionManager
import com.watermelon.player.database.MediaDatabase
import com.watermelon.player.player.WatermelonPlayer
import com.watermelon.player.security.CrashReporter
import com.watermelon.player.security.TamperDetector
import com.watermelon.player.util.PerformanceMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { androidContext() as Context }

    // Edition Manager
    single { EditionManager }

    // Database
    single { MediaDatabase.getDatabase(get()) }

    // Player
    factory { WatermelonPlayer(get()) }

    // Security
    single { TamperDetector }
    single { CrashReporter }

    // Performance
    single { PerformanceMonitor }

    // Add other app-wide singles/factories here
}
