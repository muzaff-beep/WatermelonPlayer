package com.watermelon.player.update

import android.content.Context
import com.watermelon.player.config.EditionManager
import com.watermelon.player.update.CafeBazaarUpdater
import com.watermelon.player.update.ModuleDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateManager(private val context: Context) {
    suspend fun checkForUpdates(onUpdateAvailable: (String) -> Unit) = withContext(Dispatchers.IO) {
        if (EditionManager.isIranEdition()) {
            CafeBazaarUpdater.checkUpdates(context, onUpdateAvailable)
        } else {
            // Global: Check Play Store or server
            ModuleDownloader.checkModules(context, onUpdateAvailable)
        }
    }

    fun applyUpdate(updateUrl: String): Boolean {
        // Download and install update
        return true // Placeholder
    }
}
