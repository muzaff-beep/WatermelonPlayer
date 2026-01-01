package com.watermelon.player.smart

import android.content.Context
import com.watermelon.player.WatermelonApp
import com.watermelon.player.config.EditionManager
import java.io.File

class ModelLoading {
    fun loadWhisperModel(context: Context): File? {
        if (EditionManager.isIranEdition()) return null // Offline heuristic only

        val modelFile = File(context.cacheDir, "whisper_tiny.tflite")
        if (!modelFile.exists()) {
            // Download or copy from assets (global edition)
            copyFromAssets(context, "whisper_tiny.tflite", modelFile)
        }
        return modelFile
    }

    private fun copyFromAssets(context: Context, assetName: String, destFile: File) {
        context.assets.open(assetName).use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
