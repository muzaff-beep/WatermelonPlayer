package com.watermelon.player.smart

import com.watermelon.player.model.VideoItem
import android.media.MediaMetadataRetriever

class HeuristicAnalyzer {
    fun analyze(videoItem: VideoItem): List<String> {
        val tags = mutableListOf<String>()

        // Filename heuristics
        val filename = videoItem.file.name.lowercase()
        if (filename.contains("fa") || filename.contains("persian")) tags.add("farsi")
        if (filename.contains("ar") || filename.contains("arabic")) tags.add("arabic")
        if (filename.contains("s01") || filename.contains("episode")) tags.add("tv_series")

        // Directory heuristics
        val dir = videoItem.file.parent ?: ""
        if (dir.contains("movies")) tags.add("movie")
        if (dir.contains("series")) tags.add("tv")
        if (dir.contains("kids")) tags.add("animation")

        // Media metadata
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoItem.file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            if (duration > 5400000) tags.add("movie") // >90min
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            if (width >= 3840) tags.add("4k")
        } catch (e: Exception) {
            // Ignore
        } finally {
            retriever.release()
        }

        return tags.distinct()
    }
}
