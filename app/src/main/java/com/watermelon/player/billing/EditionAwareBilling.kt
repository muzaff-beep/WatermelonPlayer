package com.watermelon.player.billing

import android.content.Context
import com.watermelon.player.config.EditionManager

class EditionAwareBilling(private val context: Context) {

    private val iranianProcessor = IranianPaymentProcessor(context)  // Assume it needs context

    /**
     * Starts purchase flow based on current edition
     */
    fun startPurchase(
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        when {
            EditionManager.isIranEdition -> {
                iranianProcessor.startPurchaseFlow(onSuccess, onFail)
            }
            else -> {
                , onFail)
            }
        }
    }

    /**
     * Verifies purchase status
     */
    fun verifyPurchase(): Boolean {
        return if (EditionManager.isIranEdition) {
            iranianProcessor.verifyIranianPurchase()
        } else {
            googlePlayBilling.verifyGlobalPurchase()
        }
    }

    /**
     * Called after successful verification from either processor
     */
    fun unlockFullVersion() {
        // Shared unlock logic across both editions
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("full_version_unlocked", true).apply()
        
        // Optional: Trigger feature refresh
        // EventBus.post(UnlockEvent())
    }

    /**
     * Clean up billing clients (call from Activity.onDestroy)
     */
    fun dispose() {
        iranianProcessor.dispose()
    }
}
