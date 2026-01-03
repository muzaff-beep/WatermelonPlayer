package com.watermelon.player.config

import com.watermelon.player.config.EditionManager.PaymentMethod

/**
 * FeatureGate.kt
 * Purpose: Central feature-flagging based on current edition.
 * 
 * All feature checks go through here — single source of truth.
 * Used across ViewModels, UI, billing, vault, etc.
 * Iran-first: Restricts online-only or Google-dependent features.
 */

object FeatureGate {

    private val currentEdition = EditionManager.getCurrentEdition()
    private val features = EditionManager.getFeatures()

    /**
     * Online features (streaming, cloud sync, etc.)
     * Disabled in Iran edition
     */
    val isOnlineFeaturesEnabled: Boolean
        get() = !currentEdition.isIranEdition && features.requiresInternet

    /**
     * Media vault (encrypted storage)
     */
    val isVaultEnabled: Boolean
        get() = features.supportsVault

    /**
     * Subtitle download or online sources
     */
    val isOnlineSubtitlesEnabled: Boolean
        get() = isOnlineFeaturesEnabled

    /**
     * Analytics / crash reporting
     */
    val isAnalyticsEnabled: Boolean
        get() = !currentEdition.isIranEdition

    /**
     * Google Play billing
     */
    val isGoogleBillingEnabled: Boolean
        get() = !currentEdition.isIranEdition

    /**
     * Iranian payment gateways (Cafe Bazaar, MyKet)
     */
    val isIranianBillingEnabled: Boolean
        get() = currentEdition.isIranEdition

    /**
     * Supported payment methods for current edition
     */
    val supportedPaymentMethods: List<PaymentMethod>
        get() = features.paymentMethods

    /**
     * Persian/RTL language support
     */
    val isPersianSupported: Boolean
        get() = features.supportsPersian

    /**
     * Advanced equalizer or audio effects
     */
    val isAdvancedAudioEnabled: Boolean
        get() = true  // Available in both editions

    /**
     * Casting (Chromecast, etc.) — disabled in Iran due to sanctions
     */
    val isCastingEnabled: Boolean
        get() = !currentEdition.isIranEdition
}
