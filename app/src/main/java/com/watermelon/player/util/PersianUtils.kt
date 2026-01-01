package com.watermelon.player.util

import java.util.*

object PersianUtils {
    // Persian/Arabic character ranges
    private val PERSIAN_ARABIC_RANGES = listOf(
        '\u0600'..'\u06FF',  // Arabic
        '\u0750'..'\u077F',  // Arabic Supplement
        '\u08A0'..'\u08FF',  // Arabic Extended-A
        '\uFB50'..'\uFDFF',  // Arabic Presentation Forms-A
        '\uFE70'..'\uFEFF'   // Arabic Presentation Forms-B
    )
    
    // Check if text contains Persian/Arabic characters
    fun containsPersianArabic(text: String): Boolean {
        return text.any { char ->
            PERSIAN_ARABIC_RANGES.any { range -> char in range }
        }
    }
    
    // Get text direction
    fun getTextDirection(text: String): androidx.compose.ui.text.style.TextDirection {
        return if (containsPersianArabic(text)) {
            androidx.compose.ui.text.style.TextDirection.Rtl
        } else {
            androidx.compose.ui.text.style.TextDirection.Ltr
        }
    }
    
    // Format Persian numbers (convert English to Persian if needed)
    fun toPersianNumbers(text: String): String {
        val englishNumbers = "0123456789"
        val persianNumbers = "۰۱۲۳۴۵۶۷۸۹"
        
        return text.map { char ->
            val index = englishNumbers.indexOf(char)
            if (index != -1) persianNumbers[index] else char
        }.joinToString("")
    }
    
    // Get Iranian locale
    fun getIranianLocale(): Locale {
        return Locale("fa", "IR")
    }
    
    // Check if locale is Persian
    fun isPersianLocale(locale: Locale): Boolean {
        return locale.language == "fa" || locale.country == "IR"
    }
}
