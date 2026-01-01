package com.watermelon.player.util

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.io.File

// Context extensions
fun Context.getFileSize(uri: Uri): Long {
    return contentResolver.openFileDescriptor(uri, "r")?.use {
        it.statSize
    } ?: 0L
}

fun Context.getFileName(uri: Uri): String {
    return when (uri.scheme) {
        "content" -> {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: uri.lastPathSegment ?: "Unknown"
        }
        "file" -> File(uri.path ?: "").name
        else -> uri.lastPathSegment ?: "Unknown"
    }
}

// String extensions
fun String.toReadableFileSize(): String {
    val bytes = this.toLongOrNull() ?: return "0 B"
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun String.toReadableDuration(): String {
    val milliseconds = this.toLongOrNull() ?: return "00:00"
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

// Compose extensions
@Composable
fun String?.orDefault(default: String = ""): String {
    return if (this.isNullOrEmpty()) default else this
}
