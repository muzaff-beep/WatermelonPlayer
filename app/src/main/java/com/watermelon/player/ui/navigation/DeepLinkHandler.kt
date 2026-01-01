package com.watermelon.player.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

/**
 * Handles deep links and external intents
 */
class DeepLinkHandler(
    private val context: Context,
    private val navController: NavController
) {
    
    fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                val title = intent.getStringExtra("title") ?: 
                           intent.getStringExtra(Intent.EXTRA_TITLE) ?:
                           uri?.lastPathSegment ?: "Media File"
                
                if (uri != null) {
                    navigateToPlayer(uri.toString(), title)
                }
            }
            
            "android.intent.action.SEARCH" -> {
                val query = intent.getStringExtra("query")
                // Handle search if needed
            }
            
            else -> {
                // Check for custom actions
                when (intent.getStringExtra("screen")) {
                    "settings" -> navigateToSettings()
                    "home" -> navigateToHome()
                }
            }
        }
    }
    
    fun handleDeepLink(uri: Uri) {
        when (uri.path) {
            "/player" -> {
                val mediaUri = uri.getQueryParameter("uri")
                val title = uri.getQueryParameter("title") ?: "Media"
                if (mediaUri != null) {
                    navigateToPlayer(mediaUri, title)
                }
            }
            "/settings" -> navigateToSettings()
            else -> navigateToHome()
        }
    }
    
    private fun navigateToPlayer(mediaUri: String, title: String) {
        navController.navigate("${Screen.Player.route}/$mediaUri/$title") {
            popUpTo(Screen.Home.route) { inclusive = false }
        }
    }
    
    private fun navigateToSettings() {
        navController.navigate(Screen.Settings.route) {
            popUpTo(Screen.Home.route) { inclusive = false }
        }
    }
    
    private fun navigateToHome() {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
        }
    }
}

@Composable
fun HandleDeepLinks(
    navController: NavController,
    navigationManager: NavigationManager
) {
    val context = LocalContext.current
    val deepLinkHandler = remember { DeepLinkHandler(context, navController) }
    
    // Handle navigation events from NavigationManager
    LaunchedEffect(navigationManager) {
        navigationManager.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToPlayer -> {
                    navController.navigate(
                        "${Screen.Player.route}/${event.uri}/${event.title}"
                    ) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
                
                is NavigationEvent.NavigateToSettings -> {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
                
                NavigationEvent.GoBack -> {
                    navController.popBackStack()
                }
                
                NavigationEvent.GoHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
                
                is NavigationEvent.OpenExternal -> {
                    context.startActivity(event.intent)
                }
                
                else -> {
                    // Other events
                }
            }
            navigationManager.clearNavigation()
        }
    }
}
