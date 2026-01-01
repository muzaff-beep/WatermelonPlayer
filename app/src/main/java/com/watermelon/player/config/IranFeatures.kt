package com.watermelon.player.config

import com.watermelon.player.billing.IranianPaymentProcessor

object IranFeatures {
    fun enableAll() {
        // Enable Iran-specific features
        if (FeatureGate.isIranPaymentEnabled()) {
            IranianPaymentProcessor.init()
        }
    }
}
