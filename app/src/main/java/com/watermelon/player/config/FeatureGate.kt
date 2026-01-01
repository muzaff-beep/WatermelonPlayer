package com.watermelon.player.config

import com.watermelon.player.config.EditionManager.Edition

/**
 * FeatureGate provides runtime checks for edition-specific features.
 * Use this to conditionally enable/disable code paths based on current edition.
 */
object FeatureGate {

    /**
     * Checks if internet-required features are allowed.
     */
    fun isInternetEnabled(): Boolean {
        return EditionManager.getCurrentEdition() is Edition.Global
    }

    /**
     * Checks if smart tagging with online ML is available.
     */
    fun isSmartTaggingOnlineEnabled(): Boolean {
        return EditionManager.getFeatures().smartTaggingOnline
    }

    /**
     * Checks if subtitle auto-fetch is allowed.
     */
    fun isSubtitleAutoFetchEnabled(): Boolean {
        return EditionManager.getFeatures().subtitleAutoFetch
    }

    /**
     * Checks if network streaming (SMB/DLNA) is enabled.
     */
    fun isNetworkStreamingEnabled(): Boolean {
        return EditionManager.getFeatures().networkStreaming
    }

    /**
     * Checks if Whisper module is available.
     */
    fun isWhisperModuleEnabled(): Boolean {
        return EditionManager.getFeatures().whisperModuleAvailable
    }

    /**
     * Checks if banner ads should be shown (e.g., in trial mode).
     */
    fun showBannerAds(): Boolean {
        return EditionManager.getFeatures().showBannerAds
    }

    /**
     * Gets the list of supported payment methods.
     */
    fun getPaymentMethods(): List<EditionManager.PaymentMethod> {
        return EditionManager.getFeatures().paymentMethods
    }

    // Add more gates as needed for other features
}
