package com.watermelon.player.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watermelon.player.viewmodel.PlayerViewModel

/**
 * QuickMenuOverlay.kt
 * Purpose: Semi-transparent slide-in menu for TV remote quick access.
 * Trigger: Long-press DPAD_CENTER in PlayerScreen.
 * Navigation: DPAD_UP/DOWN to move focus, CENTER to select, BACK to dismiss.
 * Items:
 *   - Play / Pause
 *   - Subtitles On/Off
 *   - VHS Effect Toggle
 *   - Audio Track (cycle)
 *   - Exit to Home
 * Keeps video visible underneath — dark overlay 70% opacity.
 * Auto-dismiss after 8 seconds of inactivity.
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
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(300.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Quick Menu", style = MaterialTheme.typography.headlineSmall)

                    Divider()

                    MenuItem(
                        text = if (uiState.isPlaying) "Pause" else "Play",
                        onClick = { viewModel.togglePlayPause(); onDismiss() }
                    )

                    MenuItem(
                        text = if (uiState.subtitlesEnabled) "Subtitles: ON" else "Subtitles: OFF",
                        onClick = { viewModel.toggleSubtitles(); onDismiss() }
                    )

                    MenuItem(
                        text = if (uiState.vhsEnabled) "VHS Effect: ON" else "VHS Effect: OFF",
                        onClick = { viewModel.toggleVhsEffect(!uiState.vhsEnabled) }
                    )

                    MenuItem(
                        text = "Exit to Home",
                        onClick = { viewModel.navigateHome(); onDismiss() }
                    )
                }
            }

            // Auto-dismiss timer
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
private fun MenuItem(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
```

**EXTRA BONUS FILE**  
**Location:** app/src/main/java/com/watermelon/player/util/FitModeManager.kt  
**Name:** FitModeManager.kt (new file – display fit mode toggle + per-video persistence)

```kotlin
package com.watermelon.player.util

import android.content.Context
import androidx.media3.ui.AspectRatioFrameLayout
import com.watermelon.player.database.MediaDatabase

/**
 * FitModeManager.kt
 * Purpose: Controls how video fills the screen (Zoom Fill vs Original Fit vs Stretch).
 * SettingsScreen toggle + checkbox: "Remember per video".
 * Persistence: Room DB (media hash → fit mode int).
 * Default: RESIZE_MODE_ZOOM (fill screen, crop edges — no black bars).
 * Iran/TV users prefer no bars — so default is aggressive fill.
 */

object FitModeManager {

    const val MODE_ZOOM = AspectRatioFrameLayout.RESIZE_MODE_ZOOM     // Fill, crop edges
    const val MODE_FIT = AspectRatioFrameLayout.RESIZE_MODE_FIT       // Letterbox
    const val MODE_STRETCH = AspectRatioFrameLayout.RESIZE_MODE_FILL  // Stretch (rare)

    private var globalMode = MODE_ZOOM
    private var rememberPerVideo = true

    /**
     * Get fit mode for specific video (by path hash)
     */
    fun getModeForVideo(context: Context, videoPath: String): Int {
        if (!rememberPerVideo) return globalMode

        val db = MediaDatabase.getDatabase(context)
        val hash = videoPath.hashCode().toString()
        val saved = db.mediaDao().getFitMode(hash)
        return saved ?: globalMode
    }

    /**
     * Save mode for video
     */
    fun saveModeForVideo(context: Context, videoPath: String, mode: Int) {
        if (!rememberPerVideo) return

        val db = MediaDatabase.getDatabase(context)
        val hash = videoPath.hashCode().toString()
        db.mediaDao().saveFitMode(hash, mode)
    }

    /**
     * Global setters from Settings
     */
    fun setGlobalMode(mode: Int) { globalMode = mode }
    fun setRememberPerVideo(enabled: Boolean) { rememberPerVideo = enabled }
}
