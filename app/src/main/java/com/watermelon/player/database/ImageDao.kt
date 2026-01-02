package com.watermelon.player.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * ImageDao.kt
 * DAO for ImageEntity.
 * Operations:
 *   - Insert/update
 *   - Get all images sorted by name
 *   - Basic search by title
 */

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(image: ImageEntity)

    @Query("SELECT * FROM images ORDER BY title ASC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE title LIKE '%' || :query || '%'")
    fun searchImages(query: String): Flow<List<ImageEntity>>
}
