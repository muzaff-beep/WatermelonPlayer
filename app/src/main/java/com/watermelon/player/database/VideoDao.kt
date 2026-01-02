package com.watermelon.player.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * VideoDao.kt
 * DAO for VideoEntity.
 * Operations:
 *   - Insert/update
 *   - Get all videos (Flow for Compose)
 *   - Search by keyword/tags
 *   - Update tags/fitMode
 */

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(video: VideoEntity)

    @Query("SELECT * FROM videos ORDER BY title ASC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("UPDATE videos SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)

    @Query("UPDATE videos SET fitMode = :mode WHERE id = :id")
    suspend fun saveFitMode(id: String, mode: Int)

    @Query("SELECT fitMode FROM videos WHERE id = :id")
    suspend fun getFitMode(id: String): Int?
}
