package com.watermelon.player.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.player.WatermelonPlayer
import com.watermelon.player.util.FitModeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PlayerViewModel - Hilt ViewModel for PlayerScreen
 * 
 * Responsibilities:
 * - Expose UI state via StateFlow
 * - Control playback actions
 * - Manage per-video fit mode (with persistence)
 * - Toggle subtitles, VHS effect, quick menu
 * 
 * Note: Does NOT release player — player is app-wide singleton managed by PlaybackService
 */

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watermelonPlayer: WatermelonPlayer,
    @ApplicationContext private val context: Context  // ← Injected context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        // Observe player events to update UI state
        observePlayerState()

        // Load saved fit mode for current media (if any)
        viewModelScope.launch {
            watermelonPlayer.currentMediaPath?.let { path ->
                val savedMode = FitModeManager.getModeForVideo(context, path)
                watermelonPlayer.exoPlayer.setResizeMode(savedMode)
                updateFitModeInState(savedMode)
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            watermelonPlayer.isPlaying.collect { playing ->
                _uiState.value = _uiState.value.copy(isPlaying = playing)
            }
        }

        viewModelScope.launch {
            watermelonPlayer.currentMediaPath?.let { path ->
                _uiState.value = _uiState.value.copy(currentPath = path)
            }
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
        // TODO: Apply to ExoPlayer track selection
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
        updateFitModeInState(mode)

        // Save to persistence
        watermelonPlayer.currentMediaPath?.let { path ->
            FitModeManager.saveModeForVideo(context, path, mode)
        }
    }

    private fun updateFitModeInState(mode: Int) {
        // If you want to expose current fit mode in UI state
        // _uiState.value = _uiState.value.copy(currentFitMode = mode)
    }

    // DO NOT release player here — it's managed by PlaybackService (app-wide)
    // override fun onCleared() { ... } removed intentionally
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val subtitlesEnabled: Boolean = true,
    val vhsEnabled: Boolean = false,
    val quickMenuVisible: Boolean = false,
    val currentPath: String = ""
    // val currentFitMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT // optional
)
