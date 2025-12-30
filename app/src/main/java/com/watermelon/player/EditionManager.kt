package com.watermelon.player

import android.content.Context

object EditionManager {
    
    private var edition: Edition = Edition.GLOBAL
    
    fun initialize(context: Context) {
        edition = when (BuildConfig.DEFAULT_EDITION) {
            "iran" -> Edition.IRAN
            else -> Edition.GLOBAL
        }
    }
    
    fun getEdition(): Edition = edition
    
    fun isIranEdition(): Boolean = edition == Edition.IRAN
    
    fun isGlobalEdition(): Boolean = edition == Edition.GLOBAL
    
    enum class Edition {
        IRAN, GLOBAL
    }
}
