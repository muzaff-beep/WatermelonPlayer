package com.watermelon.player.di

import com.watermelon.player.database.MediaDatabase
import com.watermelon.player.storage.UnifiedStorageAccess
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Database
    single { MediaDatabase.getDatabase(androidContext()) }
    single { get<MediaDatabase>().mediaDao() }
    
    // Storage
    single { UnifiedStorageAccess(androidContext()) }
    
    // Player
    single { com.watermelon.player.player.WatermelonPlayer(androidContext()) }
}
