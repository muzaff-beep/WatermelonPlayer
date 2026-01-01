package com.watermelon.player.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Fts4
import java.util.Date

@Entity(tableName = "videos")
@Fts4
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val lastPlayed: Date? = null,
    val tags: String = ""
)
