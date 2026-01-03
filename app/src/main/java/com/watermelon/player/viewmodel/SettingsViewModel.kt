
package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import com.watermelon.player.util.FitModeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _globalFitMode = MutableStateFlow(FitModeManager.MODE_ZOOM)
    val globalFitMode: StateFlow<Int> = _globalFitMode.asStateFlow()

    private val _rememberPerVideo = MutableStateFlow(true)
    val rememberPerVideo: StateFlow<Boolean> = _rememberPerVideo.asStateFlow()

    fun setGlobalFitMode(mode: Int) {
        FitModeManager.setGlobalMode(mode)
        _globalFitMode.value = mode
    }

    fun setRememberPerVideo(enabled: Boolean) {
        FitModeManager.setRememberPerVideo(enabled)
        _rememberPerVideo.value = enabled
    }
}
