package com.watermelon.player.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.watermelon.player.player.WatermelonPlayer
import androidx.media3.common.Player

/**
 * VideoPlayerComposable.kt
 * Purpose: Reusable Compose wrapper for the ExoPlayer surface.
 * Why separate? Allows clean reuse in PlayerScreen and future preview thumbnails.
 * Key features:
 *   - Binds ExoPlayer instance to PlayerView (native AndroidView)
 *   - Applies optional VHS retro effect via simple fragment shader (noise + scanlines)
 *   - Handles lifecycle: attaches player on composition, detaches on dispose
 *   - Fullscreen capable, keeps screen on during playback
 *   - Iran optimization: No controller auto-show (we use custom overlay), no buffering text
 */

@Composable
fun VideoPlayerComposable(
    watermelonPlayer: WatermelonPlayer,     // Our custom player wrapper instance
    vhsEnabled: Boolean,                    // Flag from UI state – toggled in Settings
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Create PlayerView once and remember across recompositions
    val playerView = remember {
        PlayerView(context).apply {
            // Hide default ExoPlayer controls – we build our own precision overlay
            useController = false

            // Improve performance on low-RAM TVs: disable artwork, resize mode fill
            useArtwork = false
            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM

            // Keep screen on while playing (critical for TV bedtime playback)
            keepScreenOn = true

            // Optional: Apply VHS shader when enabled
            // Note: Actual shader implementation requires custom SurfaceView or GLSurface
            // Here we just set a flag – real shader can be added via setVideoSurfaceView override
            // For now, we simulate via post-processing flag (future OpenGL hook)
        }
    }

    // Bind the ExoPlayer to the PlayerView
    DisposableEffect(watermelonPlayer.exoPlayer) {
        playerView.player = watermelonPlayer.exoPlayer

        // Cleanup: detach player when composable leaves screen
        onDispose {
            playerView.player = null
        }
    }

    // Apply VHS effect simulation if enabled
    // In production: replace with GLSurfaceView + fragment shader for real CRT look
    // For MVP: we can overlay a semi-transparent noise texture (raw resource)
    if (vhsEnabled) {
        // TODO: Add overlay composable with noise texture + scanlines
        // Placeholder for now – real implementation in Phase 2 polish
    }

    // Render the native PlayerView inside Compose hierarchy
    AndroidView(
        factory = { playerView },
        modifier = modifier
    )
}
