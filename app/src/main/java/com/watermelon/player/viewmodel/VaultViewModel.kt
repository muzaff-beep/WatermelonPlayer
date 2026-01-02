package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.vault.MediaVault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * VaultViewModel.kt
 * ViewModel for VaultScreen.
 * Handles:
 *   - Listing vault files
 *   - Add/remove encryption
 *   - Authentication state
 *   - Streaming play via decrypting DataSource
 */

@HiltViewModel
class VaultViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadVaultItems()
    }

    private fun loadVaultItems() {
        // Scan vault directory or DB for encrypted files
        // Placeholder
        _uiState.value = _uiState.value.copy(
            vaultItems = listOf(
                VaultItem("encrypted_movie.mkv.enc", "Top Secret Movie")
            )
        )
    }

    fun addToVault() {
        // Pick file → encrypt in background
        viewModelScope.launch {
            // MediaVault.encryptFile(...)
        }
    }

    fun removeFromVault(path: String) {
        // Delete encrypted file
    }

    fun playVaultItem(path: String) {
        if (!_uiState.value.isAuthenticated) {
            _uiState.value = _uiState.value.copy(showAuthDialog = true)
            return
        }
        // Pass decrypting DataSource to player
    }

    fun authenticateSuccess() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            showAuthDialog = false
        )
    }
}

data class VaultUiState(
    val vaultItems: List<VaultItem> = emptyList(),
    val showAuthDialog: Boolean = false,
    val isAuthenticated: Boolean = false
)

data class VaultItem(val path: String, val title: String)
