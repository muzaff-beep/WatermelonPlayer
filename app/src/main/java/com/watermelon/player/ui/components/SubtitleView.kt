package com.watermelon.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.SubtitleView
import com.watermelon.player.subtitle.PersianSubtitleProcessor

/**
 * SubtitleView.kt
 * Purpose: Custom subtitle renderer that hooks into our Persian processor.
 * Uses native Media3 SubtitleView for libass rendering (RTL, styling).
 * Processes every cue through PersianSubtitleProcessor for fixes.
 * Overlay at bottom — visible only when subtitles enabled.
 */

@Composable
fun CustomSubtitleView(
    cues: List<androidx.media3.common.text.Cue>,
    subtitlesEnabled: Boolean
) {
    if (!subtitlesEnabled || cues.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(
            factory = { context ->
                SubtitleView(context).apply {
                    setApplyEmbeddedStyles(true)
                    setCues(processCues(cues))
                }
            },
            update = { view ->
                view.setCues(processCues(cues))
            }
        )
    }
}

private fun processCues(cues: List<androidx.media3.common.text.Cue>): List<androidx.media3.common.text.Cue> {
    return cues.map { cue ->
        val processedText = PersianSubtitleProcessor.processLine(cue.text.toString())
        cue.buildUpon()
            .setText(processedText)
            .build()
    }
}
