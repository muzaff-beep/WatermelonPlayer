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

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watermelonPlayer: WatermelonPlayer,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
        loadInitialFitMode()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            watermelonPlayer.isPlaying.collect { playing ->
                _uiState.value = _uiState.value.copy(isPlaying = playing)
            }
        }

        viewModelScope.launch {
            watermelonPlayer.currentMediaPath.collect { path ->
                _uiState.value = _uiState.value.copy(currentPath = path ?: "")
            }
        }
    }

    private fun loadInitialFitMode() {
        viewModelScope.launch {
            watermelonPlayer.currentMediaPath.value?.let { path ->
                val mode = FitModeManager.getModeForVideo(context, path)
                watermelonPlayer.exoPlayer.setResizeMode(mode)
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
        watermelonPlayer.currentMediaPath.value?.let { path ->
            FitModeManager.saveModeForVideo(context, path, mode)
        }
    }
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val subtitlesEnabled: Boolean = true,
    val vhsEnabled: Boolean = false,
    val quickMenuVisible: Boolean = false,
    val currentPath: String = ""
)
