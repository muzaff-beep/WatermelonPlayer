package com.watermelon.player.storage

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * UnifiedStorageAccess.kt
 * Purpose: Provides list of all readable storage roots (internal + removable).
 * Uses direct File access where possible (Android 10+ scoped storage limits).
 * Fallback to SAF tree URIs for USB/SD on Android 11+.
 * Critical for indexing without constant permission prompts.
 */

object UnifiedStorageAccess {

    /**
     * Returns list of root directories we can scan
     */
    fun getAllStorageRoots(context: Context): List<File> {
        val roots = mutableListOf<File>()

        // Internal storage
        context.getExternalFilesDirs(null).forEach { dir ->
            dir?.parentFile?.parentFile?.parentFile?.parentFile?.let { roots.add(it) }
        }

        // Primary external (emulated)
        Environment.getExternalStorageDirectory().takeIf { it.canRead() }?.let { roots.add(it) }

        // Removable volumes (USB/SD)
        val externalDirs = context.getExternalFilesDirs(null)
        externalDirs.forEach { dir ->
            if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                val root = dir.parentFile?.parentFile?.parentFile?.parentFile
                root?.let { roots.add(it) }
            }
        }

        return roots.distinct()
    }

    /**
     * Check if path is removable storage
     */
    fun isRemovable(path: String): Boolean {
        return Environment.isExternalStorageRemovable(File(path))
    }
}
