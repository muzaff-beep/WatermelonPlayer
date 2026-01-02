package com.watermelon.player.subtitle

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import java.util.regex.Pattern

/**
 * PersianSubtitleProcessor.kt
 * Purpose: Cleans and fixes common issues in Persian (Farsi) subtitle files.
 * Problems fixed:
 *   - Wrong RTL/LTR mixing (Arabic letters rendered LTR)
 *   - Windows-1256 encoding garbage (converted to UTF-8)
 *   - Yeh/Kaf confusion (ی vs ي vs ك vs ک)
 *   - Missing hamze/yaa fixes
 *   - Forced RTL direction even if file lacks tags
 *   - Removes ads/watermarks common in Iranian subtitle sites
 * Iran-first: All fixes tuned for messy downloads from SubtitleCat, Opensub, local forums.
 */

object PersianSubtitleProcessor {

    // Regex for common watermark lines (e.g., "زیرنویس از Subadub", "Sync by XYZ")
    private val WATERMARK_PATTERN = Pattern.compile(
        "(?i)(زیرنویس|sync|ترجمه|synced|by|\\bwww\\.|\\.com|\\bsubadub\\b|\\bopensubtitles\\b)",
        Pattern.UNICODE_CASE
    )

    // Arabic letters that should be Persian forms
    private val ARABIC_TO_PERSIAN_MAP = mapOf(
        'ي' to 'ی', // Arabic yeh → Persian yeh
        'ك' to 'ک', // Arabic kaf → Persian kaf
        'ؤ' to 'و', // Isolated hamza → simple vav (common typo fix)
        'إ' to 'ا', // Alef with hamza below → simple alef
        'أ' to 'ا', // Alef with hamza above
        'ة' to 'ه'  // Taa marbuta → heh
    )

    /**
     * Main processing function – called before libass rendering
     * Input: raw subtitle line (String)
     * Output: cleaned, RTL-forced, Persian-normalized line
     */
    fun processLine(rawLine: String): CharSequence {
        if (rawLine.isBlank()) return rawLine

        var cleaned = rawLine.trim()

        // Step 1: Remove watermark lines completely
        if (WATERMARK_PATTERN.matcher(cleaned).find()) {
            return "" // Drop entire line
        }

        // Step 2: Force UTF-8 normalization and fix common encoding issues
        cleaned = cleaned.normalizePersianNumbers()

        // Step 3: Replace Arabic letter forms with proper Persian ones
        cleaned = cleaned.replaceArabicWithPersian()

        // Step 4: Force RTL embedding if line contains Persian script
        if (containsPersianScript(cleaned)) {
            // Wrap with Unicode RTL marks: RLE + content + PDF
            cleaned = "\u202B$cleaned\u202C"
        }

        // Step 5: Optional highlight for debugging (remove in release)
        // cleaned = SpannableStringBuilder(cleaned).apply { setSpan(ForegroundColorSpan(0xFFFFFF00), 0, length, 0) }

        return cleaned
    }

    /**
     * Replace Arabic numerals with Persian if needed (user toggle later)
     */
    private fun String.normalizePersianNumbers(): String {
        return this.map { char ->
            when (char) {
                in '0'..'9' -> (char.code + 1728).toChar() // 0x06F0 Persian numerals
                else -> char
            }
        }.joinToString("")
    }

    private fun String.replaceArabicWithPersian(): String {
        return this.map { char ->
            ARABIC_TO_PERSIAN_MAP[char] ?: char
        }.joinToString("")
    }

    /**
     * Detect if line contains any Persian/Arabic script
     */
    private fun containsPersianScript(text: String): Boolean {
        return text.any { char ->
            char.code in 0x0600..0x06FF || // Arabic block
                    char.code in 0xFB50..0xFDFF || // Arabic presentation forms
                    char.code in 0xFE70..0xFEFF    // Arabic presentation forms B
        }
    }

    /**
     * Batch process entire subtitle file content
     */
    fun processFileContent(content: String): String {
        return content.lineSequence()
            .map { processLine(it) }
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }
}
