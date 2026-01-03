package com.watermelon.player.config

import android.content.Context

object EditionManager {

    private var currentEdition: Edition = Edition.GLOBAL

    fun initialize(context: Context) {
        // In real app: detect from BuildConfig.FLAVOR
        // For now: default to IRAN for safety
        currentEdition = if (BuildConfig.FLAVOR == "iran") Edition.IRAN else Edition.GLOBAL
    }

    fun getCurrentEdition(): Edition = currentEdition

    fun getFeatures(): Features = when (currentEdition) {
        Edition.IRAN -> Features.iran
        Edition.GLOBAL -> Features.global
    }

    // Convenience
    val isIranEdition: Boolean
        get() = currentEdition == Edition.IRAN
}

enum class Edition(val isIranEdition: Boolean) {
    GLOBAL(false),
    IRAN(true)
}

data class Features(
    val requiresInternet: Boolean,
    val supportsPersian: Boolean,
    val supportsVault: Boolean,
    val paymentMethods: List<PaymentMethod>
) {
    companion object {
        val global = Features(
            requiresInternet = true,
            supportsPersian = true,
            supportsVault = true,
            paymentMethods = listOf(PaymentMethod.GOOGLE_PLAY)
        )

        val iran = Features(
            requiresInternet = false,
            supportsPersian = true,
            supportsVault = true,
            paymentMethods = listOf(PaymentMethod.CAFE_BAZAAR, PaymentMethod.MYKET)
        )
    }
}

enum class PaymentMethod {
    GOOGLE_PLAY,
    CAFE_BAZAAR,
    MYKET
}
