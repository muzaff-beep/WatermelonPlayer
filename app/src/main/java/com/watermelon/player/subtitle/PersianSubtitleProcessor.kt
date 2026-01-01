package com.watermelon.player.subtitle

import java.nio.charset.Charset

class PersianSubtitleProcessor {
    fun processSubtitle(text: String, encoding: String): String {
        val charset = Charset.forName(encoding)
        val decoded = String(text.toByteArray(), charset)

        return decoded
            .replace(Regex("[\u0600-\u06FF]"), { match -> fixPersianLigatures(match.value) })  // Ligature fix
            .replace(Regex("[0-9]+"), { match -> convertToPersianDigits(match.value) })  // Persian numerals
    }

    private fun fixPersianLigatures(text: String): String {
        // Common Persian ligatures (e.g., لا → ل ا)
        return text.replace("لا", "ل ا").replace("الله", "ا ل ل ه")
    }

    private fun convertToPersianDigits(text: String): String {
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        return text.map { persianDigits[it.digitToInt()] }.joinToString("")
    }
}
