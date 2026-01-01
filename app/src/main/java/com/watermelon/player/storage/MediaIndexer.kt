package com.watermelon.player.storage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.watermelon.player.config.EditionManager
import com.watermelon.player.database.MediaDatabase
import com.watermelon.player.database.MediaItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

/**
 * Background media indexer for Watermelon Player
 * Scans and indexes media files for faster access
 */
class MediaIndexer(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "MediaIndexer"
        const val WORK_NAME = "media_indexing_work"
        
        fun scheduleIndexing(context: Context) {
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<MediaIndexer>()
                .addTag(TAG)
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .build()
                )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.LINEAR,
                    androidx.work.OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get database instance
            val database = MediaDatabase.getInstance(applicationContext)
            val mediaDao = database.mediaDao()
            
            // Clear existing media items (optional, could be incremental)
            // mediaDao.deleteAll()
            
            // Get storage access
            val storageAccess = UnifiedStorageAccess(applicationContext)
            
            // Get all media files
            val mediaFiles = storageAccess.getAllMediaFiles()
            
            // Convert to entities and insert into database
            val entities = mediaFiles.filter { !it.isDirectory }.map { mediaFile ->
                MediaItemEntity(
                    uri = mediaFile.uri,
                    title = mediaFile.name.substringBeforeLast('.'),
                    filePath = mediaFile.path,
                    fileSize = mediaFile.size,
                    duration = mediaFile.duration,
                    mediaType = mediaFile.type,
                    fileFormat = mediaFile.extension,
                    resolution = null, // Would need to extract from file
                    audioCodec = null, // Would need to extract
                    videoCodec = null, // Would need to extract
                    bitrate = 0, // Would need to calculate
                    lastPlayed = null,
                    playCount = 0,
                    lastPosition = 0,
                    dateAdded = Date(),
                    isEncrypted = false,
                    vaultId = null
                )
            }
            
            // Insert in batches
            entities.chunked(50).forEach { chunk ->
                mediaDao.insertAll(*chunk.toTypedArray())
            }
            
            // Return success with count
            Result.success(
                Data.Builder()
                    .putInt("media_files_count", entities.size)
                    .build()
            )
        } catch (e: Exception) {
            // Return failure with retry
            Result.retry()
        }
    }
}
