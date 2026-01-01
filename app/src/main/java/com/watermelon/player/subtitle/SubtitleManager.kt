package com.watermelon.player.subtitle

import android.content.Context
import android.net.Uri
import androidx.media3.common.text.Cue
import androidx.media3.extractor.text.SubtitleDecoderFactory
import androidx.media3.extractor.text.ass.AssSubtitle
import androidx.media3.extractor.text.srt.SrtSubtitle
import com.watermelon.player.config.EditionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Subtitle manager for Watermelon Player
 * Handles loading, parsing, and displaying subtitles
 */
class SubtitleManager(private val context: Context) {

    companion object {
        // Supported subtitle formats
        val SUPPORTED_FORMATS = listOf(".srt", ".ass", ".ssa", ".vtt", ".sub")
        
        // Persian/Arabic character ranges
        val PERSIAN_ARABIC_RANGES = arrayOf(
            '\u0600'..'\u06FF',  // Arabic
            '\u0750'..'\u077F',  // Arabic Supplement
            '\u08A0'..'\u08FF',  // Arabic Extended-A
            '\uFB50'..'\uFDFF',  // Arabic Presentation Forms-A
            '\uFE70'..'\uFEFF',  // Arabic Presentation Forms-B
            '\u0600'..'\u06FF',  // Persian (shared with Arabic)
            '\uFB50'..'\uFDFF',  // Persian Presentation Forms
            '\uFE70'..'\uFEFF'   // Persian Presentation Forms
        )
    }
    
    // Current subtitle state
    private var currentSubtitles: List<SubtitleCue> = emptyList()
    private var currentSubtitleUri: Uri? = null
    private var currentOffset: Long = 0
    
    // Subtitle styling
    data class SubtitleStyle(
        val textSize: Float = 16f,
        val textColor: Int = 0xFFFFFFFF.toInt(),
        val backgroundColor: Int = 0x80000000.toInt(),
        val edgeType: Int = Cue.EDGE_TYPE_OUTLINE,
        val edgeColor: Int = 0xFF000000.toInt(),
        val fontFamily: String? = null,
        val isRtl: Boolean = false
    )
    
    // Subtitle cue data class
    data class SubtitleCue(
        val startTime: Long,    // in milliseconds
        val endTime: Long,      // in milliseconds
        val text: String,
        val style: SubtitleStyle = SubtitleStyle()
    )
    
    /**
     * Load subtitles from URI
     */
    suspend fun loadSubtitles(uri: Uri, offset: Long = 0): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            currentSubtitleUri = uri
            currentOffset = offset
            
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream, detectCharset(stream)))
                val content = reader.readText()
                
                currentSubtitles = when {
                    uri.toString().endsWith(".srt", ignoreCase = true) -> parseSrt(content)
                    uri.toString().endsWith(".ass", ignoreCase = true) -> parseAss(content)
                    uri.toString().endsWith(".ssa", ignoreCase = true) -> parseSsa(content)
                    else -> parseGeneric(content)
                }
                
                // Apply offset
                currentSubtitles = currentSubtitles.map { cue ->
                    cue.copy(
                        startTime = cue.startTime + offset,
                        endTime = cue.endTime + offset
                    )
                }
                
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Parse SRT subtitle format
     */
    private fun parseSrt(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val blocks = content.trim().split("\n\n")
        
        for (block in blocks) {
            val lines = block.lines()
            if (lines.size < 3) continue
            
            // Parse time line (e.g., "00:01:23,456 --> 00:01:25,789")
            val timeLine = lines[1]
            val timeMatch = Pattern.compile(
                "(\\d{2}):(\\d{2}):(\\d{2})[,\\.](\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2})[,\\.](\\d{3})"
            ).matcher(timeLine)
            
            if (timeMatch.find()) {
                val startTime = parseTime(
                    timeMatch.group(1).toInt(),
                    timeMatch.group(2).toInt(),
                    timeMatch.group(3).toInt(),
                    timeMatch.group(4).toInt()
                )
                
                val endTime = parseTime(
                    timeMatch.group(5).toInt(),
                    timeMatch.group(6).toInt(),
                    timeMatch.group(7).toInt(),
                    timeMatch.group(8).toInt()
                )
                
                // Combine text lines (skip index and time lines)
                val text = lines.subList(2, lines.size).joinToString("\n")
                
                // Clean up text (remove HTML tags, etc.)
                val cleanText = cleanSubtitleText(text)
                
                // Detect RTL for Persian/Arabic
                val isRtl = containsPersianArabic(text)
                
                cues.add(
                    SubtitleCue(
                        startTime = startTime,
                        endTime = endTime,
                        text = cleanText,
                        style = SubtitleStyle(isRtl = isRtl)
                    )
                )
            }
        }
        
        return cues
    }
    
    /**
     * Parse ASS/SSA subtitle format (basic implementation)
     */
    private fun parseAss(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val lines = content.lines()
        var inEventsSection = false
        
        for (line in lines) {
            when {
                line.startsWith("[Events]") -> inEventsSection = true
                line.startsWith("[") && !line.startsWith("[Events]") -> inEventsSection = false
                inEventsSection && line.startsWith("Dialogue:") -> {
                    val parts = line.split(",")
                    if (parts.size >= 10) {
                        val startTime = parseAssTime(parts[1].trim())
                        val endTime = parseAssTime(parts[2].trim())
                        val text = parts.subList(9, parts.size).joinToString(",")
                        
                        // Extract text from ASS format (remove override tags)
                        val cleanText = extractAssText(text)
                        
                        // Detect RTL
                        val isRtl = containsPersianArabic(cleanText)
                        
                        cues.add(
                            SubtitleCue(
                                startTime = startTime,
                                endTime = endTime,
                                text = cleanText,
                                style = SubtitleStyle(isRtl = isRtl)
                            )
                        )
                    }
                }
            }
        }
        
        return cues
    }
    
    /**
     * Parse SSA subtitle format (similar to ASS)
     */
    private fun parseSsa(content: String): List<SubtitleCue> {
        // SSA is similar to ASS
        return parseAss(content)
    }
    
    /**
     * Generic parser for unknown formats
     */
    private fun parseGeneric(content: String): List<SubtitleCue> {
        // Try to detect format and parse accordingly
        return when {
            content.contains("-->") || content.contains("->") -> parseSrt(content)
            content.contains("[Events]") -> parseAss(content)
            else -> emptyList()
        }
    }
    
    /**
     * Get cues for current playback position
     */
    fun getCuesAtPosition(positionMs: Long): List<SubtitleCue> {
        return currentSubtitles.filter { cue ->
            positionMs >= cue.startTime && positionMs <= cue.endTime
        }
    }
    
    /**
     * Convert subtitle cues to Media3 Cues
     */
    fun convertToMedia3Cues(cues: List<SubtitleCue>): List<Cue> {
        return cues.map { cue ->
            Cue.Builder()
                .setText(cue.text)
                .setStartTimeMs(cue.startTime)
                .setEndTimeMs(cue.endTime)
                .setTextSize(cue.style.textSize, Cue.TEXT_SIZE_TYPE_FRACTIONAL)
                .setTextAlignment(if (cue.style.isRtl) Layout.Alignment.ALIGN_OPPOSITE else null)
                .setPosition(0.5f)
                .setLine(-1f, Cue.LINE_TYPE_FRACTION)
                .setVerticalType(Cue.VERTICAL_TYPE_BOTTOM)
                .build()
        }
    }
    
    /**
     * Set subtitle offset (for synchronization)
     */
    fun setOffset(offset: Long) {
        currentOffset = offset
        currentSubtitles = currentSubtitles.map { cue ->
            cue.copy(
                startTime = cue.startTime + offset,
                endTime = cue.endTime + offset
            )
        }
    }
    
    /**
     * Clear current subtitles
     */
    fun clear() {
        currentSubtitles = emptyList()
        currentSubtitleUri = null
        currentOffset = 0
    }
    
    /**
     * Check if text contains Persian/Arabic characters
     */
    private fun containsPersianArabic(text: String): Boolean {
        return text.any { char ->
            PERSIAN_ARABIC_RANGES.any { range -> char in range }
        }
    }
    
    /**
     * Clean subtitle text (remove HTML tags, fix formatting)
     */
    private fun cleanSubtitleText(text: String): String {
        var cleaned = text
            .replace("<[^>]*>".toRegex(), "") // Remove HTML tags
            .replace("\\{.*?\\}".toRegex(), "") // Remove ASS tags
            .replace("\\\\N", "\n") // Replace newline markers
            .replace("\\\\n", "\n")
            .trim()
        
        // Fix RTL text if needed
        if (containsPersianArabic(cleaned)) {
            // Add RLM (Right-to-Left Mark) for better rendering
            cleaned = "\u200F$cleaned"
        }
        
        return cleaned
    }
    
    /**
     * Extract text from ASS format (remove override tags)
     */
    private fun extractAssText(assText: String): String {
        var text = assText
        // Remove override tags like {\fnArial}
        text = text.replace("\\{.*?\\}".toRegex(), "")
        // Remove actor names
        text = text.replace("^[^:]*:".toRegex(), "")
        return text.trim()
    }
    
    /**
     * Parse time from hours, minutes, seconds, milliseconds
     */
    private fun parseTime(hours: Int, minutes: Int, seconds: Int, millis: Int): Long {
        return TimeUnit.HOURS.toMillis(hours.toLong()) +
                TimeUnit.MINUTES.toMillis(minutes.toLong()) +
                TimeUnit.SECONDS.toMillis(seconds.toLong()) +
                millis
    }
    
    /**
     * Parse ASS/SSA time format (H:MM:SS.cc)
     */
    private fun parseAssTime(timeStr: String): Long {
        val parts = timeStr.split(":", ".")
        if (parts.size == 4) {
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            val seconds = parts[2].toInt()
            val centiseconds = parts[3].toInt()
            return parseTime(hours, minutes, seconds, centiseconds * 10)
        }
        return 0
    }
    
    /**
     * Detect charset from stream (for encoding detection)
     */
    private fun detectCharset(stream: java.io.InputStream): String {
        // Simple charset detection - could be enhanced
        return try {
            // Read BOM (Byte Order Mark)
            val bom = ByteArray(3)
            stream.mark(3)
            stream.read(bom)
            stream.reset()
            
            when {
                bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte() -> "UTF-8"
                bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte() -> "UTF-16BE"
                bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() -> "UTF-16LE"
                else -> "UTF-8" // Default assumption
            }
        } catch (e: Exception) {
            "UTF-8"
        }
    }
}
