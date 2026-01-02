package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.database.MediaDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GalleryViewModel.kt
 * ViewModel for GalleryScreen (TV-only image slideshow).
 * Manages:
 *   - List of images from DB
 *   - Current index + auto-advance
 *   - Play/pause state
 *   - Slide duration setting
 */

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val database: MediaDatabase
) : ViewModel() {

    private val imageDao = database.imageDao()

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            imageDao.getAllImages().collect { images ->
                _uiState.value = _uiState.value.copy(
                    images = images.map { it.toGalleryItem() },
                    currentImage = images.firstOrNull()?.toGalleryItem()
                        ?: GalleryItem("", "No images")
                )
            }
        }
    }

    fun nextImage() {
        val currentIndex = _uiState.value.currentIndex
        val list = _uiState.value.images
        if (list.isEmpty()) return
        val next = (currentIndex + 1) % list.size
        _uiState.value = _uiState.value.copy(
            currentIndex = next,
            currentImage = list[next]
        )
    }

    fun previousImage() {
        val currentIndex = _uiState.value.currentIndex
        val list = _uiState.value.images
        if (list.isEmpty()) return
        val prev = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
        _uiState.value = _uiState.value.copy(
            currentIndex = prev,
            currentImage = list[prev]
        )
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun setSlideDuration(seconds: Long) {
        _uiState.value = _uiState.value.copy(slideDurationSeconds = seconds.coerceIn(2, 60))
    }
}

data class GalleryUiState(
    val images: List<GalleryItem> = emptyList(),
    val currentImage: GalleryItem = GalleryItem("", ""),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true,
    val slideDurationSeconds: Long = 10,
    val showInfo: Boolean = true
)

data class GalleryItem(
    val path: String,
    val title: String
)

private fun com.watermelon.player.database.ImageEntity.toGalleryItem(): GalleryItem =
    GalleryItem(path = path, title = title)
