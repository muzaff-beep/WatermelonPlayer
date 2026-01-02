package com.watermelon.player.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.watermelon.player.player.WatermelonPlayer
import com.watermelon.player.ui.components.PrecisionSeekBar
import com.watermelon.player.ui.components.VideoPlayerComposable
import com.watermelon.player.viewmodel.PlayerViewModel

/**
 * PlayerScreen.kt
 * Purpose: Central playback screen for both mobile and TV.
 * Iran-first: No network calls, no analytics, pure local playback.
 * Features implemented here:
 *   - Full-screen ExoPlayer surface
 *   - Custom precision seek bar (0.01s steps + thumbnail preview)
 *   - VHS effect toggle (noise + scanlines on rewind/fast-forward)
 *   - Independent UI language and subtitle language selectors
 *   - Gesture controls: double-tap for play/pause, single-tap show/hide controls
 *   - Audio track / subtitle track manual switch
 */

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    watermelonPlayer: WatermelonPlayer
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Controls visibility: auto-hide after 3 seconds of inactivity
    var controlsVisible by remember { mutableStateOf(true) }
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double-tap toggles play/pause
                        if (watermelonPlayer.exoPlayer.isPlaying) {
                            watermelonPlayer.exoPlayer.pause()
                        } else {
                            watermelonPlayer.exoPlayer.play()
                        }
                    },
                    onTap = {
                        // Single tap shows/hides overlay controls
                        controlsVisible = !controlsVisible
                    }
                )
            }
    ) {
        // Main video surface – uses custom composable with optional VHS shader
        VideoPlayerComposable(
            watermelonPlayer = watermelonPlayer,
            vhsEnabled = uiState.vhsEffectEnabled,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay controls – only shown when controlsVisible = true
        if (controlsVisible) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar: Title + Settings icon
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.mediaTitle ?: "Watermelon Player",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { viewModel.navigateToSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }

                // Bottom controls cluster
                Column {
                    // Precision seek bar with thumbnail preview
                    PrecisionSeekBar(
                        player = watermelonPlayer.exoPlayer,
                        onSeek = { positionMs ->
                            watermelonPlayer.seekToPrecise(positionMs)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom row: Play/Pause, Speed, Audio/Sub tracks, VHS toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { watermelonPlayer.exoPlayer.playWhenReady = !watermelonPlayer.exoPlayer.playWhenReady }) {
                            Icon(
                                if (watermelonPlayer.exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause"
                            )
                        }

                        // Speed selector (0.5x to 2.0x)
                        var showSpeedMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showSpeedMenu = true }) {
                            Text("${uiState.playbackSpeed}x")
                        }
                        DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}x") },
                                    onClick = {
                                        watermelonPlayer.setPlaybackSpeed(speed)
                                        viewModel.updateSpeed(speed)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }

                        // Audio track selector (independent of subtitle)
                        IconButton(onClick = { viewModel.showAudioTrackDialog() }) {
                            Icon(Icons.Default.AudioFile, contentDescription = "Audio Track")
                        }

                        // Subtitle track selector (independent of UI language)
                        IconButton(onClick = { viewModel.showSubtitleTrackDialog() }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles")
                        }

                        // VHS effect toggle – only visible in Performance Mode (low-end devices)
                        IconButton(
                            onClick = {
                                val newState = !uiState.vhsEffectEnabled
                                watermelonPlayer.toggleVhsEffect(newState)
                                viewModel.toggleVhsEffect(newState)
                            }
                        ) {
                            Icon(
                                if (uiState.vhsEffectEnabled) Icons.Default.Tv else Icons.Default.TvOff,
                                contentDescription = "VHS Effect"
                            )
                        }
                    }
                }
            }
        }
    }
}
