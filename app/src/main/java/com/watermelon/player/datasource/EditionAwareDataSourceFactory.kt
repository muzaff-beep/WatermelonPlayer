package com.watermelon.player.datasource

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import com.watermelon.player.config.EditionManager

class EditionAwareDataSourceFactory(private val context: Context) {
    fun create(): DataSource.Factory {
        return when (EditionManager.getCurrentEdition()) {
            is EditionManager.Edition.Iran -> createIranDataSource()
            is EditionManager.Edition.Global -> createGlobalDataSource()
        }
    }
    
    private fun createIranDataSource(): DataSource.Factory {
        // Iran edition: Local files only
        return DefaultDataSource.Factory(
            context,
            FileDataSource.Factory()
        )
    }
    
    private fun createGlobalDataSource(): DataSource.Factory {
        // Global edition: Supports local and network
        return DefaultDataSource.Factory(context)
    }
}
