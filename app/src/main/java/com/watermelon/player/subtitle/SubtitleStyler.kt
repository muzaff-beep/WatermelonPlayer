package com.watermelon.player.subtitle

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.*
import androidx.compose.ui.graphics.toArgb
import com.watermelon.player.ui.theme.WatermelonRedDark

/**
 * SubtitleStyler.kt
 * Purpose: Applies user-customizable styling to subtitles.
 * Features:
 *   - Text color (default white)
 *   - Font size scale (80% to 200%)
 *   - Background box (black semi-transparent, optional)
 *   - Stroke/outline (black edge for readability)
 * Called from CustomSubtitleView before rendering.
 * Settings saved in SharedPreferences (future DataStore).
 */

object SubtitleStyler {

    // Default values – user can change in Settings
    private var textColor: Int = Color.WHITE
    private var fontSizeScale: Float = 1.0f // 1.0 = default
    private var backgroundEnabled: Boolean = true
    private var outlineEnabled: Boolean = true

    /**
     * Apply style to processed subtitle line
     */
    fun styleLine(line: CharSequence): CharSequence {
        if (line.isBlank()) return line

        val spannable = SpannableStringBuilder(line)

        // Text color
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            0, line.length,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Font size (relative to system default)
        spannable.setSpan(
            RelativeSizeSpan(fontSizeScale),
            0, line.length,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Background box
        if (backgroundEnabled) {
            spannable.setSpan(
                BackgroundColorSpan(Color.BLACK and 0xAA000000), // 67% opacity
                0, line.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Outline/stroke (black edge)
        if (outlineEnabled) {
            // Simple approximation using multiple shadows
            val shadowRadius = 4f
            val shadowColor = Color.BLACK
            spannable.setSpan(
                ShadowSpan(shadowRadius, 0f, 0f, shadowColor),
                0, line.length, 0
            )
            spannable.setSpan(
                ShadowSpan(shadowRadius, shadowRadius, 0f, shadowColor),
                0, line.length, 0
            )
            spannable.setSpan(
                ShadowSpan(shadowRadius, 0f, shadowRadius, shadowColor),
                0, line.length, 0
            )
            spannable.setSpan(
                ShadowSpan(shadowRadius, -shadowRadius, 0f, shadowColor),
                0, line.length, 0
            )
        }

        return spannable
    }

    // Setters from SettingsViewModel
    fun setTextColor(color: androidx.compose.ui.graphics.Color) {
        textColor = color.toArgb()
    }

    fun setFontSizeScale(scale: Float) {
        fontSizeScale = scale.coerceIn(0.8f, 2.0f)
    }

    fun setBackgroundEnabled(enabled: Boolean) {
        backgroundEnabled = enabled
    }

    fun setOutlineEnabled(enabled: Boolean) {
        outlineEnabled = enabled
    }
}

// Helper class for shadow outline
class ShadowSpan(
    private val radius: Float,
    private val dx: Float,
    private val dy: Float,
    private val color: Int
) : ReplacementSpan() {
    override fun getSize(paint: android.graphics.Paint, text: CharSequence, start: Int, end: Int, fm: android.graphics.Paint.FontMetricsInt?): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(
        canvas: android.graphics.Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: android.graphics.Paint
    ) {
        paint.color = color
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawText(text, start, end, x + dx, y + dy, paint)
    }
}
