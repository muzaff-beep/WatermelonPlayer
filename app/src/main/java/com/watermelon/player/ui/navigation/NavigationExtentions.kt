package com.watermelon.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

// Extension functions for cleaner navigation setup
fun NavGraphBuilder.homeScreen(
    onNavigateToPlayer: (String, String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(Screen.Home.route) {
        HomeScreen(
            onNavigateToPlayer = onNavigateToPlayer,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

fun NavGraphBuilder.playerScreen(
    onBack: () -> Unit
) {
    composable(
        route = "${Screen.Player.route}/{mediaUri}/{mediaTitle}",
        arguments = listOf(
            navArgument("mediaUri") {
                type = androidx.navigation.NavType.StringType
            },
            navArgument("mediaTitle") {
                type = androidx.navigation.NavType.StringType
            }
        )
    ) { backStackEntry ->
        val mediaUri = backStackEntry.arguments?.getString("mediaUri") ?: ""
        val mediaTitle = backStackEntry.arguments?.getString("mediaTitle") ?: ""
        
        PlayerScreen(
            mediaUri = if (mediaUri.isNotEmpty()) android.net.Uri.parse(mediaUri) else null,
            mediaTitle = mediaTitle,
            onBack = onBack
        )
    }
}

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit
) {
    composable(Screen.Settings.route) {
        SettingsScreen(onBack = onBack)
    }
}

// Extension for NavController
fun NavController.navigateToPlayer(mediaUri: String, title: String) {
    this.navigate("${Screen.Player.route}/$mediaUri/$title") {
        // Optional animation
        launchSingleTop = true
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Screen.Settings.route) {
        launchSingleTop = true
    }
}

// Composable function to get current screen
@Composable
fun currentScreen(): Screen {
    val navController = androidx.navigation.compose.rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    return when {
        currentRoute == Screen.Home.route -> Screen.Home
        currentRoute?.startsWith(Screen.Player.route) == true -> Screen.Player
        currentRoute == Screen.Settings.route -> Screen.Settings
        else -> Screen.Home
    }
}

// Helper for testing
fun createTestNavigation(): NavController {
    return androidx.navigation.compose.rememberNavController()
}
