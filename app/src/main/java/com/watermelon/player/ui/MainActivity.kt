package com.watermelon.player.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.watermelon.player.navigation.MainNavGraph
import com.watermelon.player.util.RemoteHandler
import com.watermelon.player.util.TvDetector
import com.watermelon.player.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WatermelonTheme {
                val navController = rememberNavController()
                val isTv = TvDetector.isTv(this)

                if (isTv) {
                    // Wrap with remote handler on TV
                    Box(modifier = RemoteHandler.remoteHandlerModifier(PlayerViewModel())) {
                        AppContent(navController, isTv)
                    }
                } else {
                    AppContent(navController, isTv)
                }
            }
        }
    }
}

@Composable
private fun AppContent(navController: NavHostController, isTv: Boolean) {
    // Splash → Main graph
    val startDestination = "splash"
    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(navController)
        }
        // MainNavGraph handles the rest
        MainNavGraph(navController = navController, isTv = isTv)
    }
}
