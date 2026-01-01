package com.watermelon.player.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.watermelon.player.config.EditionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Storage Access Framework helper for Watermelon Player
 * Handles SAF permissions and file access
 */
class SAFHelper(private val context: Context) {

    companion object {
        const val REQUEST_CODE_SAF = 1001
        
        // SAF URI permissions
        fun isSAFUri(uri: Uri): Boolean {
            return DocumentsContract.isDocumentUri(context, uri)
        }
        
        // Check if we have SAF permission for a URI
        fun hasSAFPermission(context: Context, uri: Uri): Boolean {
            return context.contentResolver.persistedUriPermissions.any {
                it.uri == uri && it.isWritePermission
            }
        }
    }
    
    private var safResultLauncher: ActivityResultLauncher<Intent>? = null
    
    /**
     * Register SAF result launcher (call this from Activity onCreate)
     */
    fun registerSAFLauncher(activity: ComponentActivity, onResult: (Uri?) -> Unit) {
        safResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Take persistable URI permission
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            takeFlags
                        )
                    }
                    
                    onResult(uri)
                }
            } else {
                onResult(null)
            }
        }
    }
    
    /**
     * Open SAF file picker
     */
    fun openSAFFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            
            // Optional: Set initial directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, 
                    DocumentsContract.buildDocumentUriUsingTree(
                        Uri.parse("content://com.android.externalstorage.documents"),
                        "primary"
                    )
                )
            }
            
            // Grant read/write permissions
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
        
        safResultLauncher?.launch(intent)
    }
    
    /**
     * Open SAF for single file selection
     */
    fun openSAFFilePickerForFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            
            // Filter by media types
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "video/*",
                "audio/*",
                "application/octet-stream"
            ))
            
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        safResultLauncher?.launch(intent)
    }
    
    /**
     * List files in SAF directory
     */
    suspend fun listSAFFiles(treeUri: Uri): List<DocumentFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<DocumentFile>()
        
        try {
            val treeDocument = DocumentFile.fromTreeUri(context, treeUri)
            treeDocument?.listFiles()?.forEach { documentFile ->
                files.add(documentFile)
            }
        } catch (e: SecurityException) {
            // No permission
        }
        
        files
    }
    
    /**
     * Check if SAF is required for the given path
     * For Android 11+ (API 30+), SAF is required for accessing certain directories
     */
    fun isSAFRequiredForPath(path: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return false
        }
        
        val file = File(path)
        return when {
            // Android 11+ requires SAF for accessing Downloads without permission
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    Environment.isExternalStorageManager() -> false
            else -> !file.canRead()
        }
    }
    
    /**
     * Get display name for SAF URI
     */
    fun getDisplayName(uri: Uri): String? {
        return try {
            DocumentFile.fromSingleUri(context, uri)?.name
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get file size for SAF URI
     */
    fun getFileSize(uri: Uri): Long {
        return try {
            DocumentFile.fromSingleUri(context, uri)?.length() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Check if we can write to SAF URI
     */
    fun canWrite(uri: Uri): Boolean {
        return try {
            DocumentFile.fromSingleUri(context, uri)?.canWrite() ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create a file in SAF directory
     */
    suspend fun createFile(treeUri: Uri, mimeType: String, displayName: String): Uri? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DocumentsContract.createDocument(
                        context.contentResolver,
                        treeUri,
                        mimeType,
                        displayName
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
