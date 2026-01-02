package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.config.EditionManager
import com.watermelon.player.util.FitModeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsViewModel.kt
 * Hilt ViewModel for SettingsScreen.
 * Manages:
 *   - UI language selection (independent)
 *   - Subtitle language selection (independent)
 *   - Low-RAM / VHS / fit mode toggles
 *   - Manual edition override
 *   - Update guide trigger
 * All preferences persisted via DataStore (future) or SharedPreferences.
 */

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load current values (placeholder – real from DataStore)
        _uiState.value = SettingsUiState(
            uiLanguage = "فارسی", // Default Iran
            subtitleLanguage = "Auto",
            lowRamMode = true,
            vhsEnabled = false
        )
    }

    fun setUiLanguage(language: String) {
        _uiState.value = _uiState.value.copy(uiLanguage = language)
        // TODO: Trigger locale change + restart activity
    }

    fun setSubtitleLanguage(language: String) {
        _uiState.value = _uiState.value.copy(subtitleLanguage = language)
        // TODO: Apply to ExoPlayer track selection
    }

    fun toggleLowRamMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(lowRamMode = enabled)
        // TODO: Adjust Coil + buffer policies
    }

    fun toggleVhsEffect(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(vhsEnabled = enabled)
        // Global toggle – PlayerViewModel will read
    }

    fun showEditionDialog() {
        // Show dialog with Iran / Global / Auto
        // On select → EditionManager.setManualEdition()
    }

    fun openUpdateGuide() {
        // Trigger UpdateManager.checkForUpdate()
    }

    fun setGlobalFitMode(mode: Int) {
        FitModeManager.setGlobalMode(mode)
    }

    fun toggleRememberPerVideo(enabled: Boolean) {
        FitModeManager.setRememberPerVideo(enabled)
    }
}

data class SettingsUiState(
    val uiLanguage: String = "فارسی",
    val subtitleLanguage: String = "Auto",
    val lowRamMode: Boolean = true,
    val vhsEnabled: Boolean = false
)
