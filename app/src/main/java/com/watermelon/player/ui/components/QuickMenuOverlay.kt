package com.watermelon.player.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watermelon.player.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

/**
 * QuickMenuOverlay.kt - TV Remote Quick Access Menu
 * 
 * Features:
 * - Slide-in from left (natural for TV remote focus)
 * - Semi-transparent dark overlay
 * - Auto-dismiss after 8 seconds inactivity
 * - Focusable TextButton items (TV DPAD navigation)
 * - Immediate dismiss on selection
 */

@Composable
fun QuickMenuOverlay(
    viewModel: PlayerViewModel,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)) // Better semantic color
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(320.dp)
                    .padding(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quick Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    MenuItem(
                        text = if (uiState.isPlaying) "Pause" else "Play",
                        onClick = {
                            viewModel.togglePlayPause()
                            onDismiss()
                        }
                    )

                    MenuItem(
                        text = if (uiState.subtitlesEnabled) "Subtitles: ON" else "Subtitles: OFF",
                        onClick = {
                            viewModel.toggleSubtitles()
                            onDismiss()
                        }
                    )

                    MenuItem(
                        text = if (uiState.vhsEnabled) "VHS Effect: ON" else "VHS Effect: OFF",
                        onClick = {
                            viewModel.toggleVhsEffect(!uiState.vhsEnabled)
                            onDismiss()
                        }
                    )

                    // Placeholder for future items
                    // MenuItem("Audio Track", onClick = { ... })

                    MenuItem(
                        text = "Exit to Home",
                        onClick = {
                            // viewModel.navigateHome() // Implement in ViewModel
                            onDismiss()
                        }
                    )
                }
            }

            // Auto-dismiss after 8 seconds
            LaunchedEffect(visible) {
                if (visible) {
                    delay(8000)
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
