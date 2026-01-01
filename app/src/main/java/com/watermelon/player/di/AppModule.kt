package com.watermelon.player.di

import android.content.Context
import com.watermelon.player.config.EditionManager
import com.watermelon.player.database.MediaDatabase
import com.watermelon.player.datasource.EditionAwareDataSourceFactory
import com.watermelon.player.player.WatermelonPlayer
import com.watermelon.player.storage.UnifiedStorageAccess
import com.watermelon.player.subtitle.SubtitleManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Context
    single<Context> { androidContext() }
    
    // Edition manager
    single { EditionManager }
    
    // Storage
    single { UnifiedStorageAccess(get()) }
    
    // Data source factory
    single { EditionAwareDataSourceFactory(get()) }
    
    // Subtitle manager
    single { SubtitleManager(get()) }
    
    // Database
    single { MediaDatabase.getInstance(get()) }
    single { get<MediaDatabase>().mediaDao() }
    
    // Player (factory - creates new instance each time)
    factory { (context: Context) ->
        WatermelonPlayer(
            context = context,
            dataSourceFactory = get()
        )
    }
}
