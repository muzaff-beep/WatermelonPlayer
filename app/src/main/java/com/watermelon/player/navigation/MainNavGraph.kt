package com.watermelon.player.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.watermelon.player.ui.screens.HomeScreen
import com.watermelon.player.ui.screens.PlayerScreen
import com.watermelon.player.ui.screens.SettingsScreen
import com.watermelon.player.ui.screens.GalleryScreen

/**
 * MainNavGraph.kt
 * Purpose: Central navigation graph.
 * Routes:
 *   - home
 *   - player/{mediaPath}
 *   - settings
 *   - gallery (TV-only)
 * TV guard: GalleryScreen hidden on mobile.
 */

@Composable
fun MainNavGraph(
    navController: NavHostController,
    isTv: Boolean
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onMediaClick = { path ->
                    navController.navigate("player/$path")
                }
            )
        }

        composable("player/{mediaPath}") { backStackEntry ->
            val path = backStackEntry.arguments?.getString("mediaPath") ?: return@composable
            PlayerScreen(mediaPath = path)
        }

        composable("settings") {
            SettingsScreen()
        }

        if (isTv) {
            composable("gallery") {
                GalleryScreen()
            }
        }
    }
}
