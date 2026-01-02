package com.watermelon.player.database

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * VideoEntity.kt
 * Room entity for indexed videos.
 * Fields:
 *   - id: hash of absolute path (unique)
 *   - path: full file path
 *   - title: display name (cleaned from filename)
 *   - durationMs: from metadata
 *   - tags: comma-separated string
 *   - thumbnailPath: local cached thumbnail
 *   - fitMode: user-saved display fit (zoom/fit/stretch)
 */

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,          // SHA-256 or simple hash of path
    val path: String,
    val title: String,
    val durationMs: Long = 0,
    val tags: String = "",
    val thumbnailPath: String? = null,
    val fitMode: Int = 0                 // From FitModeManager constants
)
// Add to existing VideoEntity.kt
fun VideoEntity.toMediaItemUi(): MediaItemUi {
    return MediaItemUi(
        path = path,
        title = title,
        thumbnailUri = thumbnailPath ?: "",
        duration = durationMs
    )
}

fun File.toMediaEntity(): VideoEntity {
    return VideoEntity(
        id = absolutePath.hashCode().toString(),
        path = absolutePath,
        title = nameWithoutExtension,
        durationMs = 0 // Filled later by SmartTagging
    )
}
