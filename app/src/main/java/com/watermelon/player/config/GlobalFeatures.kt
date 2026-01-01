package com.watermelon.player.config

object GlobalFeatures {
    fun enableAll() {
        // Enable online features for Global Edition
        if (FeatureGate.isOnlineFeaturesEnabled()) {
            // Setup network clients, API keys, etc.
            // Placeholder for future: OpenSubtitles.init(), GoogleDrive.init()
        }
    }
}
