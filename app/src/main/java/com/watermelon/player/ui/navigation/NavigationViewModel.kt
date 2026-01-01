package com.watermelon.player.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel : ViewModel() {
    
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    private val _navigationHistory = MutableStateFlow<List<Screen>>(emptyList())
    val navigationHistory: StateFlow<List<Screen>> = _navigationHistory.asStateFlow()
    
    private val _isPlayerFullscreen = MutableStateFlow(false)
    val isPlayerFullscreen: StateFlow<Boolean> = _isPlayerFullscreen.asStateFlow()
    
    fun navigateTo(screen: Screen) {
        viewModelScope.launch {
            // Add current screen to history
            _navigationHistory.value = _navigationHistory.value + _currentScreen.value
            
            // Update current screen
            _currentScreen.value = screen
        }
    }
    
    fun navigateBack() {
        viewModelScope.launch {
            if (_navigationHistory.value.isNotEmpty()) {
                // Get last screen from history
                val lastScreen = _navigationHistory.value.last()
                
                // Remove from history
                _navigationHistory.value = _navigationHistory.value.dropLast(1)
                
                // Navigate back
                _currentScreen.value = lastScreen
            }
        }
    }
    
    fun setPlayerFullscreen(isFullscreen: Boolean) {
        _isPlayerFullscreen.value = isFullscreen
    }
    
    fun clearHistory() {
        _navigationHistory.value = emptyList()
    }
    
    companion object {
        // Default navigation items for bottom bar
        val bottomNavItems = listOf(
            BottomNavItem(
                title = "Home",
                route = Screen.Home.route,
                icon = androidx.compose.material.icons.Icons.Default.Home
            ),
            BottomNavItem(
                title = "Player",
                route = Screen.Player.route,
                icon = androidx.compose.material.icons.Icons.Default.PlayArrow
            ),
            BottomNavItem(
                title = "Settings",
                route = Screen.Settings.route,
                icon = androidx.compose.material.icons.Icons.Default.Settings
            )
        )
    }
}
