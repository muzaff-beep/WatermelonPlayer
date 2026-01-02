package com.watermelon.player.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ImageEntity.kt
 * Room entity for indexed images (used only in TV slideshow).
 * Fields:
 *   - id: hash of absolute path
 *   - path: full file path
 *   - title: display name (from filename)
 *   - thumbnailPath: cached small preview
 * Separate table for performance – videos and images never mixed in queries.
 */

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: String,          // Hash of path
    val path: String,
    val title: String,
    val thumbnailPath: String? = null
)
