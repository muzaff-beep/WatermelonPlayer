package com.watermelon.player.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * MediaDatabase.kt
 * Purpose: Single Room database for all local media metadata.
 * Entities:
 *   - VideoEntity (path, title, duration, tags, thumbnail, fitMode)
 *   - ImageEntity (path, title, thumbnail)
 * Separate DAOs for clean access.
 * Iran-first: Fully offline, no sync, small footprint.
 * Singleton instance – created in WatermelonApp.
 */

@Database(
    entities = [VideoEntity::class, ImageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: MediaDatabase? = null

        fun getDatabase(context: Context): MediaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaDatabase::class.java,
                    "watermelon_media_database"
                )
                    .fallbackToDestructiveMigration() // Simple for MVP
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
