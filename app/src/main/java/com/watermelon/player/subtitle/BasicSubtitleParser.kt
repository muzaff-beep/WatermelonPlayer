package com.watermelon.player.subtitle

import java.io.File

class BasicSubtitleParser {
    fun parseSrt(file: File): SubtitleTrack {
        val cues = mutableListOf<SubtitleCue>()
        val language = extractLanguage(file.name)

        file.forEachLine { line ->
            if (line.matches(Regex("\\d+"))) {
                val (start, end) = parseTimecodes(file.readLines().take(3).drop(1))
                val text = file.readLines().drop(2).takeWhile { it.isNotBlank() }.joinToString("\n")
                cues.add(SubtitleCue(start, end, text, language))
            }
        }

        return SubtitleTrack(file.absolutePath, language, cues)
    }

    private fun parseTimecodes(lines: List<String>): Pair<Long, Long> {
        val timeLine = lines.firstOrNull { it.contains("-->") } ?: return Pair(0, 0)
        val (start, end) = timeLine.split("-->")
        return Pair(parseTime(start.trim()), parseTime(end.trim()))
    }

    private fun parseTime(timeStr: String): Long {
        val parts = timeStr.split(":")
        val hours = parts[0].toIntOrNull() ?: 0
        val mins = parts[1].toIntOrNull() ?: 0
        val secs = parts[2].split(",").let { it[0].toIntOrNull() ?: 0 + it[1].toIntOrNull() ?: 0 } / 1000
        return (hours * 3600 + mins * 60 + secs) * 1000L
    }

    private fun extractLanguage(filename: String): String {
        return if (filename.contains("fa") || filename.contains("persian")) "fa" else "en"
    }
}
