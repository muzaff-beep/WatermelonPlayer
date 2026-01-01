package com.watermelon.player.smart

import com.watermelon.player.model.VideoItem
import com.watermelon.player.smart.HeuristicAnalyzer
import com.watermelon.player.config.EditionManager
import com.watermelon.player.smart.WhisperModule

class SmartTaggingEngine {
    private val heuristicAnalyzer = HeuristicAnalyzer()
    private val whisperModule = WhisperModule()

    fun tagVideo(videoItem: VideoItem): VideoItem {
        val tags = mutableListOf<String>()

        // Always run heuristics (offline)
        tags.addAll(heuristicAnalyzer.analyze(videoItem))

        // Whisper only in Global Edition
        if (!EditionManager.isIranEdition()) {
            whisperModule.loadModelIfNeeded()
            tags.addAll(whisperModule.extractTags(videoItem.file))
        }

        return videoItem.copy(tags = tags)
    }

    fun suggestRename(videoItem: VideoItem): String? {
        val prefs = WatermelonApp.instance.getSharedPreferences("smart_prefs", Context.MODE_PRIVATE)
        val isAutoRename = prefs.getBoolean("auto_rename", false)

        if (isAutoRename) {
            val suggestedTitle = videoItem.tags.joinToString(" ") { it.capitalize() }
            return "${suggestedTitle}.mp4"
        }
        return null
    }
}
