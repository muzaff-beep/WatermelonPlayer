package com.watermelon.player.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY date_added DESC")
    fun getAllMedia(): Flow<List<MediaItemEntity>>
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaItemEntity?
    
    @Insert
    suspend fun insert(mediaItem: MediaItemEntity): Long
    
    @Update
    suspend fun update(mediaItem: MediaItemEntity)
    
    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM media_items")
    suspend fun deleteAll()
}
