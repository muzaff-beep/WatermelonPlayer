package com.watermelon.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BasicVideoCard(title: String, duration: String) {
    Card(
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(title)
            Text(duration, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun LoadingSpinner() {
    // Placeholder for loading animation
    Text("Loading...")
}
