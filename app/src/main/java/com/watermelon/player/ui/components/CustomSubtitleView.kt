// Add to existing CustomSubtitleView.kt
private fun processCues(cues: List<androidx.media3.common.text.Cue>): List<androidx.media3.common.text.Cue> {
    return cues.map { cue ->
        val rawText = cue.text.toString()
        val processed = PersianSubtitleProcessor.processLine(rawText)
        val styled = SubtitleStyler.styleLine(processed)

        cue.buildUpon()
            .setText(styled)
            .build()
    }
}
