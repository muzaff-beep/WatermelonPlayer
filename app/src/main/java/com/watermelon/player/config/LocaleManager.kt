package com.watermelon.player.config

import android.content.Context
import java.util.Locale

object LocaleManager {
    fun isPersianLocale(context: Context): Boolean {
        return Locale.getDefault().language == "fa"
    }

    fun shouldUseRtlLayout(context: Context): Boolean {
        return isPersianLocale(context)
    }
}
