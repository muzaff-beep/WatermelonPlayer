package com.watermelon.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.watermelon.player.ui.screens.HomeScreen
import com.watermelon.player.ui.screens.PlayerScreen
import com.watermelon.player.ui.screens.SettingsScreen

@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val actions = remember(navController) { MainActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPlayer = { uri, title ->
                    actions.navigateToPlayer(uri, title)
                },
                onNavigateToSettings = { actions.navigateToSettings() }
            )
        }
        
        composable(
            route = "${Screen.Player.route}/{mediaUri}/{mediaTitle}",
            arguments = listOf(
                navArgument("mediaUri") {
                    type = NavType.StringType
                },
                navArgument("mediaTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val mediaUri = backStackEntry.arguments?.getString("mediaUri") ?: ""
            val mediaTitle = backStackEntry.arguments?.getString("mediaTitle") ?: ""
            
            PlayerScreen(
                mediaUri = if (mediaUri.isNotEmpty()) android.net.Uri.parse(mediaUri) else null,
                mediaTitle = mediaTitle,
                onBack = { actions.navigateBack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { actions.navigateBack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Player : Screen("player")
    object Settings : Screen("settings")
}

class MainActions(private val navController: NavHostController) {
    fun navigateToPlayer(mediaUri: String, mediaTitle: String) {
        navController.navigate("${Screen.Player.route}/$mediaUri/$mediaTitle")
    }
    
    fun navigateToSettings() {
        navController.navigate(Screen.Settings.route)
    }
    
    fun navigateBack() {
        navController.popBackStack()
    }
}
