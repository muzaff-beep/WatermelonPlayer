package com.watermelon.player.util

import android.view.KeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.Modifier
import com.watermelon.player.viewmodel.PlayerViewModel

/**
 * RemoteHandler.kt
 * Purpose: Central handler for Android TV / Samsung A23 remote control keys.
 * Design philosophy: Keep it simple, predictable, no surprises.
 * Only reliable keys used — no voice, no channel buttons (not present on low-end TVs).
 * Subtitle key: Short press ONLY toggles subtitles ON/OFF (auto-sync, auto-fetch behind the scenes).
 * No language cycle, no sync slider on remote — user goes to Settings for that.
 * Long-press DPAD_CENTER opens Quick Menu overlay.
 *
 * Usage: Wrap PlayerScreen or HomeScreen with .modifier = remoteHandlerModifier(viewModel)
 */

object RemoteHandler {

    /**
     * Returns a Modifier that captures hardware key events for TV remotes
     */
    fun remoteHandlerModifier(viewModel: PlayerViewModel): Modifier {
        return Modifier.onKeyEvent { keyEvent ->
            when (keyEvent.key.nativeKeyCode) {
                // DPAD navigation — handled automatically by Compose focus system
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> false // Let system handle focus

                // CENTER / OK button
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                        // Short press: Play/Pause toggle
                        if (!keyEvent.isLongPress) {
                            viewModel.togglePlayPause()
                            true
                        } else {
                            // Long press: Show Quick Menu overlay
                            viewModel.showQuickMenu()
                            true
                        }
                    } else false
                }

                // Dedicated Subtitle / CC button — most TV remotes have it
                KeyEvent.KEYCODE_CAPTIONS,
                KeyEvent.KEYCODE_SUBTITLE -> {
                    if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                        viewModel.toggleSubtitles() // Simple ON/OFF — auto-sync handled internally
                        true
                    } else false
                }

                // BACK button — exit player or go up
                KeyEvent.KEYCODE_BACK -> {
                    if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                        viewModel.navigateBack()
                        true
                    } else false
                }

                // INFO / GUIDE button — show media info overlay (future)
                KeyEvent.KEYCODE_INFO,
                KeyEvent.KEYCODE_GUIDE -> {
                    if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                        viewModel.showMediaInfo()
                        true
                    } else false
                }

                // MUTE
                KeyEvent.KEYCODE_MUTE -> {
                    if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                        viewModel.toggleMute()
                        true
                    } else false
                }

                else -> false // Ignore all other keys
            }
        }
    }
}
