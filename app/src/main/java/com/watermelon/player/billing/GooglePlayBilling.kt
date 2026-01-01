package com.watermelon.player.billing

import android.content.Context

class GooglePlayBilling(private val context: Context) {
    fun startGlobalPurchase(onSuccess: () -> Unit, onFail: (String) -> Unit) {
        // Google Play Billing Library integration
        // Launch billing flow for SKU "watermelon_full_unlock"
        onSuccess()
    }

    fun verifyGlobalPurchase(): Boolean {
        // Query purchases
        return true // Placeholder
    }
}
