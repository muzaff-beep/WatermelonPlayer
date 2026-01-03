package com.watermelon.player.billing

import android.content.Context
import com.watermelon.player.config.EditionManager
import com.watermelon.player.billing.IranianPaymentProcessor
import com.watermelon.player.billing.GooglePlayBilling

class EditionAwareBilling(private val context: Context) {
    private val iranianProcessor = IranianPaymentProcessor()
    private val googlePlayBilling = GooglePlayBilling(context)

    fun startPurchase(onSuccess: () -> Unit, onFail: (String) -> Unit) {
if (EditionManager.getCurrentEdition().isIranEdition) { // Fixed
    IranianPaymentProcessor() // Fixed - call constructor
}

        
    }

    fun verifyPurchase(): Boolean {
        return if (EditionManager.isIranEdition()) {
            iranianProcessor.verifyIranianPurchase()
        } else {
            googlePlayBilling.verifyGlobalPurchase()
        }
    }

    fun unlockFullVersion() {
        // Shared unlock logic
        // Set shared preference "unlocked = true"
    }
}
