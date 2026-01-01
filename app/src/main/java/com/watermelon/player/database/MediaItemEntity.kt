package com.watermelon.player.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val title: String,
    val filePath: String,
    val fileSize: Long,
    val duration: Long,
    val mediaType: String,
    val fileFormat: String,
    val lastPlayed: Date? = null,
    val playCount: Int = 0,
    val lastPosition: Long = 0,
    val dateAdded: Date = Date()
)
