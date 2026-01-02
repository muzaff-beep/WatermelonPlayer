package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.player.WatermelonPlayer
import com.watermelon.player.util.FitModeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PlayerViewModel.kt
 * Hilt ViewModel for PlayerScreen.
 * Manages:
 *   - Playback state (playing, position)
 *   - UI flags (VHS, subtitles, quick menu)
 *   - Fit mode per video
 *   - Remote actions
 */

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watermelonPlayer: WatermelonPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        // Load saved fit mode for current video
        viewModelScope.launch {
            val mode = FitModeManager.getModeForVideo(/* context */, _uiState.value.currentPath)
            watermelonPlayer.exoPlayer.setResizeMode(mode)
        }
    }

    fun togglePlayPause() {
        if (watermelonPlayer.exoPlayer.isPlaying) {
            watermelonPlayer.exoPlayer.pause()
        } else {
            watermelonPlayer.exoPlayer.play()
        }
    }

    fun toggleSubtitles() {
        val new = !_uiState.value.subtitlesEnabled
        _uiState.value = _uiState.value.copy(subtitlesEnabled = new)
        // Apply to player tracks
    }

    fun toggleVhsEffect(enabled: Boolean) {
        watermelonPlayer.toggleVhsEffect(enabled)
        _uiState.value = _uiState.value.copy(vhsEnabled = enabled)
    }

    fun showQuickMenu() {
        _uiState.value = _uiState.value.copy(quickMenuVisible = true)
    }

    fun hideQuickMenu() {
        _uiState.value = _uiState.value.copy(quickMenuVisible = false)
    }

    fun setFitMode(mode: Int) {
        watermelonPlayer.exoPlayer.setResizeMode(mode)
        FitModeManager.saveModeForVideo(/* context */, _uiState.value.currentPath, mode)
    }

    override fun onCleared() {
        watermelonPlayer.release()
        super.onCleared()
    }
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val subtitlesEnabled: Boolean = true,
    val vhsEnabled: Boolean = false,
    val quickMenuVisible: Boolean = false,
    val currentPath: String = ""
)
