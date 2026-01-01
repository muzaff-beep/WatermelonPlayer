package com.watermelon.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.watermelon.player.ui.screens.TVHomeScreen
import com.watermelon.player.ui.screens.TVPlayerScreen
import com.watermelon.player.ui.screens.TVSettingsScreen

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TVScreen.Home.route
    ) {
        composable(TVScreen.Home.route) {
            TVHomeScreen(
                onNavigateToPlayer = { uri, title ->
                    navController.navigate("${TVScreen.Player.route}/$uri/$title")
                },
                onNavigateToSettings = {
                    navController.navigate(TVScreen.Settings.route)
                }
            )
        }
        
        composable(
            route = "${TVScreen.Player.route}/{mediaUri}/{mediaTitle}",
            arguments = listOf(
                androidx.navigation.navArgument("mediaUri") {
                    type = androidx.navigation.NavType.StringType
                },
                androidx.navigation.navArgument("mediaTitle") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val mediaUri = backStackEntry.arguments?.getString("mediaUri") ?: ""
            val mediaTitle = backStackEntry.arguments?.getString("mediaTitle") ?: ""
            
            TVPlayerScreen(
                mediaUri = if (mediaUri.isNotEmpty()) android.net.Uri.parse(mediaUri) else null,
                mediaTitle = mediaTitle,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(TVScreen.Settings.route) {
            TVSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class TVScreen(val route: String) {
    object Home : TVScreen("tv_home")
    object Player : TVScreen("tv_player")
    object Settings : TVScreen("tv_settings")
}
