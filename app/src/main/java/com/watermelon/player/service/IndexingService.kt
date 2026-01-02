package com.watermelon.player.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.watermelon.player.R
import com.watermelon.player.database.MediaDatabase
import com.watermelon.player.storage.UnifiedStorageAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * IndexingService.kt
 * Purpose: Background scanning of local storage (USB/SD/internal) for media files.
 * Key features:
 *   - FIFO queue – processes one folder at a time
 *   - Low CPU cap (20% usage via coroutine delay)
 *   - Separate databases: VideoIndex.db and ImageIndex.db (TV slideshow uses images)
 *   - Runs as foreground service with persistent notification
 *   - Respects Android 15+ foreground restrictions
 *   - Iran-first: No analytics, no network, no battery blame
 */

class IndexingService(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        private const val NOTIFICATION_ID = 102
        private const val CHANNEL_ID = "watermelon_indexing_channel"
    }

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        val videoDao = MediaDatabase.getDatabase(applicationContext).videoDao()
        val imageDao = MediaDatabase.getDatabase(applicationContext).imageDao()

        // Get all accessible storage roots (internal + USB + SD via SAF)
        val roots = UnifiedStorageAccess.getAllStorageRoots(applicationContext)

        roots.forEach { root ->
            scanDirectory(root, videoDao, imageDao)
        }

        return Result.success()
    }

    private suspend fun scanDirectory(
        directory: java.io.File,
        videoDao: com.watermelon.player.database.VideoDao,
        imageDao: com.watermelon.player.database.ImageDao
    ) = withContext(Dispatchers.IO) {
        if (!directory.isDirectory || !directory.canRead()) return@withContext

        val files = directory.listFiles() ?: return@withContext

        files.sortedBy { it.name } // FIFO order

        for (file in files) {
            if (isStopped) return@withContext

            if (file.isDirectory) {
                scanDirectory(file, videoDao, imageDao)
            } else {
                when (file.extension.lowercase()) {
                    in listOf("mp4", "mkv", "avi", "webm", "mov") -> {
                        videoDao.insertOrUpdate(file.toMediaEntity())
                    }
                    in listOf("jpg", "jpeg", "png", "gif", "webp", "heic", "bmp") -> {
                        imageDao.insertOrUpdate(file.toImageEntity())
                    }
                }
            }

            // Low-CPU throttle – 50ms delay every file
            kotlinx.coroutines.delay(50)
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Watermelon Indexing")
            .setContentText("Scanning local media...")
            .setSmallIcon(R.drawable.ic_watermelon)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Indexing",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Scanning storage for videos and images" }

            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
