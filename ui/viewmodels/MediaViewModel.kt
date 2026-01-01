package com.watermelon.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.database.MediaDao
import com.watermelon.player.storage.UnifiedStorageAccess
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediaViewModel(
    private val storageAccess: UnifiedStorageAccess,
    private val mediaDao: MediaDao
) : ViewModel() {
    
    private val _mediaFiles = MutableStateFlow<List<UnifiedStorageAccess.MediaFile>>(emptyList())
    val mediaFiles: StateFlow<List<UnifiedStorageAccess.MediaFile>> = _mediaFiles
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadMediaFiles()
    }
    
    fun loadMediaFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                storageAccess.listVideoFiles()
                    .collect { files ->
                        _mediaFiles.value = files
                        
                        // Store in database for history
                        files.forEach { mediaFile ->
                            mediaDao.insertMedia(
                                com.watermelon.player.database.MediaItemEntity(
                                    path = mediaFile.path ?: "",
                                    name = mediaFile.name,
                                    duration = mediaFile.duration,
                                    size = mediaFile.size,
                                    lastPlayed = System.currentTimeMillis()
                                )
                            )
                        }
                    }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadMediaFiles()
    }
}
