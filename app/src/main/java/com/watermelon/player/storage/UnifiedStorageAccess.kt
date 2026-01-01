package com.watermelon.player.storage

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.watermelon.player.config.EditionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Unified storage access for Watermelon Player
 * Handles both traditional storage and SAF (Storage Access Framework)
 */
class UnifiedStorageAccess(private val context: Context) {

    companion object {
        // Media directories to scan
        val MEDIA_DIRECTORIES = listOf(
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_MUSIC,
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_DCIM
        )

        // Media file extensions
        val MEDIA_EXTENSIONS = listOf(
            // Video
            ".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm", ".m4v",
            ".3gp", ".ts", ".m2ts", ".mpg", ".mpeg", ".vob", ".asf", ".rm", ".rmvb",
            // Audio
            ".mp3", ".aac", ".flac", ".wav", ".ogg", ".m4a", ".opus", ".wma",
            ".alac", ".aiff", ".ape", ".dsd", ".mka"
        )
    }

    /**
     * Get all media files from device
     */
    suspend fun getAllMediaFiles(): List<MediaFile> = withContext(Dispatchers.IO) {
        val mediaFiles = mutableListOf<MediaFile>()

        try {
            // Method 1: Use MediaStore for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaFiles.addAll(getMediaFilesFromMediaStore())
            }

            // Method 2: Traditional file scanning (for older Android and external storage)
            mediaFiles.addAll(getMediaFilesFromFileSystem())

            // Method 3: SAF granted directories
            mediaFiles.addAll(getMediaFilesFromSAF())

            // Remove duplicates and sort by name
            mediaFiles.distinctBy { it.path }.sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get media files using MediaStore API (Android 10+)
     */
    private fun getMediaFilesFromMediaStore(): List<MediaFile> {
        val mediaFiles = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DURATION
        )

        // Query for video and audio files
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?) AND (" +
                MEDIA_EXTENSIONS.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" } +
                ")"

        val selectionArgs = mutableListOf<String>().apply {
            add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
            MEDIA_EXTENSIONS.forEach { add("%$it") }
        }

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs.toTypedArray(),
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(pathColumn)
                val size = cursor.getLong(sizeColumn)
                val dateModified = cursor.getLong(dateColumn)
                val mimeType = cursor.getString(mimeColumn)
                val duration = cursor.getLong(durationColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    id
                )

                mediaFiles.add(
                    MediaFile(
                        id = id,
                        name = name,
                        path = path,
                        uri = contentUri.toString(),
                        size = size,
                        dateModified = dateModified * 1000, // Convert to milliseconds
                        mimeType = mimeType,
                        duration = duration,
                        isDirectory = false
                    )
                )
            }
        }

        return mediaFiles
    }

    /**
     * Get media files by scanning file system (traditional method)
     */
    private fun getMediaFilesFromFileSystem(): List<MediaFile> {
        val mediaFiles = mutableListOf<MediaFile>()
        val externalStorage = Environment.getExternalStorageDirectory()

        if (externalStorage != null && externalStorage.exists()) {
            scanDirectory(externalStorage, mediaFiles)
        }

        // Check for additional storage volumes
        val storageVolumes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.externalMediaDirs.toList()
        } else {
            listOf(externalStorage)
        }

        storageVolumes.forEach { volume ->
            if (volume != null && volume.exists()) {
                scanDirectory(volume, mediaFiles)
            }
        }

        return mediaFiles
    }

    /**
     * Recursively scan directory for media files
     */
    private fun scanDirectory(directory: File, mediaFiles: MutableList<MediaFile>, depth: Int = 0) {
        if (depth > 10) return // Limit recursion depth

        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // Skip hidden and system directories
                    if (!file.name.startsWith(".")) {
                        scanDirectory(file, mediaFiles, depth + 1)
                    }
                } else {
                    val extension = file.extension.lowercase()
                    if (MEDIA_EXTENSIONS.contains(".$extension")) {
                        mediaFiles.add(
                            MediaFile(
                                id = file.hashCode().toLong(),
                                name = file.name,
                                path = file.absolutePath,
                                uri = Uri.fromFile(file).toString(),
                                size = file.length(),
                                dateModified = file.lastModified(),
                                mimeType = getMimeTypeFromExtension(extension),
                                duration = 0,
                                isDirectory = false
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // No permission for this directory
        }
    }

    /**
     * Get media files from SAF granted directories
     */
    private fun getMediaFilesFromSAF(): List<MediaFile> {
        // This would be implemented when user grants access via SAF
        // For now, return empty list
        return emptyList()
    }

    /**
     * Get directory tree for browsing
     */
    suspend fun getDirectoryTree(path: String = Environment.getExternalStorageDirectory().absolutePath): List<MediaFile> =
        withContext(Dispatchers.IO) {
            val directory = File(path)
            val items = mutableListOf<MediaFile>()

            if (!directory.exists() || !directory.isDirectory) {
                return@withContext emptyList()
            }

            try {
                // Add parent directory (except for root)
                if (directory.parent != null) {
                    items.add(
                        MediaFile(
                            id = -1,
                            name = "..",
                            path = directory.parent!!,
                            uri = Uri.fromFile(File(directory.parent!!)).toString(),
                            size = 0,
                            dateModified = 0,
                            mimeType = null,
                            duration = 0,
                            isDirectory = true,
                            isParent = true
                        )
                    )
                }

                directory.listFiles()?.forEach { file ->
                    items.add(
                        MediaFile(
                            id = file.hashCode().toLong(),
                            name = file.name,
                            path = file.absolutePath,
                            uri = Uri.fromFile(file).toString(),
                            size = if (file.isFile) file.length() else 0,
                            dateModified = file.lastModified(),
                            mimeType = if (file.isFile) getMimeTypeFromExtension(file.extension) else null,
                            duration = 0,
                            isDirectory = file.isDirectory,
                            isParent = false
                        )
                    )
                }
            } catch (e: SecurityException) {
                // No permission
            }

            // Sort: directories first, then files, alphabetically
            items.sortedWith(
                compareBy(
                    { !it.isDirectory }, // Directories first (false < true)
                    { it.name.lowercase() } // Then alphabetical
                )
            )
        }

    /**
     * Check if a file is a media file by extension
     */
    fun isMediaFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return MEDIA_EXTENSIONS.contains(".$extension")
    }

    /**
     * Get MIME type from file extension
     */
    private fun getMimeTypeFromExtension(extension: String): String {
        return when (extension.lowercase()) {
            "mp4", "m4v" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            else -> "application/octet-stream"
        }
    }

    /**
     * Get human readable file size
     */
    fun getReadableFileSize(size: Long): String {
        return when {
            size >= 1_000_000_000 -> "${String.format("%.2f", size / 1_000_000_000.0)} GB"
            size >= 1_000_000 -> "${String.format("%.2f", size / 1_000_000.0)} MB"
            size >= 1_000 -> "${String.format("%.2f", size / 1_000.0)} KB"
            else -> "$size B"
        }
    }

    /**
     * Get readable duration
     */
    fun getReadableDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
}

/**
 * Data class representing a media file or directory
 */
data class MediaFile(
    val id: Long,
    val name: String,
    val path: String,
    val uri: String,
    val size: Long,
    val dateModified: Long,
    val mimeType: String?,
    val duration: Long,
    val isDirectory: Boolean,
    val isParent: Boolean = false
) {
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()

    val type: String
        get() = when {
            isDirectory -> "directory"
            extension in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "ts", "m2ts", "mpg", "mpeg") -> "video"
            extension in listOf("mp3", "aac", "flac", "wav", "ogg", "m4a", "opus", "wma", "alac", "aiff", "ape", "dsd", "mka") -> "audio"
            else -> "unknown"
        }
}
