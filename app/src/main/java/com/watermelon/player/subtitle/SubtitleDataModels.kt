package com.watermelon.player.subtitle

data class SubtitleCue(
    val startTime: Long,
    val endTime: Long,
    val text: String,
    val language: String = "fa"
)

data class SubtitleTrack(
    val file: String,
    val language: String,
    val cues: List<SubtitleCue>
)
