package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.database.MediaDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel.kt
 * Hilt ViewModel for HomeScreen.
 * Responsibilities:
 *   - Load videos and images from Room DB
 *   - Combine into UI state
 *   - Detect TV vs mobile for layout
 *   - Trigger indexing if empty
 */

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: MediaDatabase
) : ViewModel() {

    private val videoDao = database.videoDao()
    private val imageDao = database.imageDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                videoDao.getAllVideos(),
                imageDao.getAllImages()
            ) { videos, images ->
                HomeUiState(
                    videoItems = videos.map { it.toUiModel() },
                    imageItems = images.map { it.toUiModel() },
                    isTvDevice = false // TODO: Detect via UiModeManager
                )
            }.collect { _uiState.value = it }
        }
    }

    fun refresh() {
        // Trigger re-indexing service if lists empty
        if (_uiState.value.videoItems.isEmpty() && _uiState.value.imageItems.isEmpty()) {
            // TODO: Start IndexingService
        }
    }
}

data class HomeUiState(
    val videoItems: List<MediaItemUi> = emptyList(),
    val imageItems: List<MediaItemUi> = emptyList(),
    val isTvDevice: Boolean = false
)

// Simple mapper extensions (real impl in separate file later)
private fun com.watermelon.player.database.VideoEntity.toUiModel(): MediaItemUi =
    MediaItemUi(path = path, title = title, thumbnailUri = thumbnailPath ?: "")

private fun com.watermelon.player.database.ImageEntity.toUiModel(): MediaItemUi =
    MediaItemUi(path = path, title = title, thumbnailUri = thumbnailPath ?: "")
