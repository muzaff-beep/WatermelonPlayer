package com.watermelon.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.watermelon.player.ui.theme.WatermelonRedDark

/**
 * SplashScreen.kt
 * Simple 1.2s splash with centered loading text in user's language.
 * No animation to keep APK small and fast on A23.
 */

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(1200)
        navController.navigate("home") {
            popUpTo(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WatermelonRedDark),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.loading_text),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
