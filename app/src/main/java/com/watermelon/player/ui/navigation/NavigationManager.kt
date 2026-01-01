package com.watermelon.player.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
class NavigationManager {
    private val _navigationEvents = MutableStateFlow<NavigationEvent>(NavigationEvent.None)
    val navigationEvents: StateFlow<NavigationEvent> = _navigationEvents.asStateFlow()
    
    fun navigateTo(event: NavigationEvent) {
        _navigationEvents.value = event
    }
    
    fun clearNavigation() {
        _navigationEvents.value = NavigationEvent.None
    }
    
    companion object {
        @Composable
        fun rememberNavigationManager(): NavigationManager {
            val context = LocalContext.current
            return remember { NavigationManager() }
        }
    }
}

sealed class NavigationEvent {
    object None : NavigationEvent()
    data class NavigateToPlayer(val uri: Uri, val title: String) : NavigationEvent()
    data class NavigateToSettings(val section: String? = null) : NavigationEvent()
    data class NavigateToFolder(val path: String) : NavigationEvent()
    data class ShareFile(val uri: Uri, val mimeType: String) : NavigationEvent()
    data class OpenExternal(val intent: Intent) : NavigationEvent()
    object GoBack : NavigationEvent()
    object GoHome : NavigationEvent()
}

// Helper functions for common navigation tasks
fun Context.createPlayerIntent(uri: Uri, title: String): Intent {
    return Intent(this, Class.forName("com.watermelon.player.MainActivity")).apply {
        action = Intent.ACTION_VIEW
        data = uri
        putExtra("title", title)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
}

fun Context.createSettingsIntent(): Intent {
    return Intent(this, Class.forName("com.watermelon.player.MainActivity")).apply {
        action = "android.intent.action.VIEW"
        putExtra("screen", "settings")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
}
