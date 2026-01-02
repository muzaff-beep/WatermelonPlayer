package com.watermelon.player.smart

import android.content.Context
import android.media.MediaMetadataRetriever
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import com.watermelon.player.database.MediaDatabase
import java.io.File

/**
 * SmartTaggingEngine.kt
 * Purpose: Offline automatic tagging of messy local files (no internet, no heavy ML).
 * Uses lightweight heuristics:
 *   - File name keywords (e.g., "4K", "HDR", "Trailer", "1080p")
 *   - Metadata extraction (duration, bitrate, resolution)
 *   - OpenCV-lite: motion detection, face count, average brightness
 *   - Audio ratio (speech vs music vs silence)
 * Tags stored in Room DB for smart search (e.g., "Short clips", "Cinema", "Action").
 * Iran-first: Works on low-end A23, <300KB OpenCV, no TensorFlow.
 */

object SmartTaggingEngine {

    private val KEYWORD_TAGS = mapOf(
        "trailer" to "Trailer",
        "4k" to "4K",
        "hdr" to "HDR",
        "1080" to "Full HD",
        "720" to "HD",
        "dubbed" to "Dubbed",
        "farsi" to "Persian Audio",
        "short" to "Short"
    )

    /**
     * Main entry – called by IndexingService after file discovery
     */
    suspend fun tagFile(context: Context, file: File) {
        val tags = mutableSetOf<String>()

        // 1. Filename keyword scan
        val name = file.nameWithoutExtension.lowercase()
        KEYWORD_TAGS.forEach { (keyword, tag) ->
            if (keyword in name) tags.add(tag)
        }

        // 2. Metadata extraction
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLong() ?: 0
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0

            if (durationMs < 300000) tags.add("Short") // <5 min
            if (bitrate > 10_000_000) tags.add("High Quality")
            if (width >= 3840) tags.add("4K")
        }

        // 3. OpenCV motion/face (lite – single frame sample)
        // TODO: Sample middle frame, detect faces > 2 → "Group"

        // Save tags to DB
        val db = MediaDatabase.getDatabase(context)
        db.videoDao().updateTags(file.absolutePath.hashCode().toString(), tags.joinToString(","))
    }
}
