package com.watermelon.player.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile

/**
 * SAFHelper.kt
 * Purpose: Handles Storage Access Framework for persistent USB/SD access on Android 11+.
 * Users grant tree URI once → we persist and use DocumentFile for scanning.
 * Required because direct File access blocked on removable storage.
 */

object SAFHelper {

    private const val PREF_SAF_URI = "saf_tree_uri"

    /**
     * Launch SAF picker for user to grant access to USB/SD root
     */
    fun launchSafPicker(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        launcher.launch(intent)
    }

    /**
     * Save granted tree URI
     */
    fun saveTreeUri(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        context.getSharedPreferences("saf_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_SAF_URI, uri.toString())
            .apply()
    }

    /**
     * Get saved tree DocumentFile if exists
     */
    fun getSavedTree(context: Context): DocumentFile? {
        val uriString = context.getSharedPreferences("saf_prefs", Context.MODE_PRIVATE)
            .getString(PREF_SAF_URI, null) ?: return null
        val uri = Uri.parse(uriString)
        return DocumentFile.fromTreeUri(context, uri)
    }
}
