package com.watermelon.player.billing

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * IranianPaymentProcessor.kt
 * Purpose: Handles in-app unlock payments for Iran edition.
 * Sanctions-aware:
 *   - No Google Play Billing in Iran flavor
 *   - Placeholders for local gateways: ZarinPal, IDPay
 *   - USDT (TRC20/BEP20) fallback via wallet address
 *   - Manual verification (user sends TXID, admin verifies)
 * Iran-first: All flows offline-capable, no foreign APIs.
 */

object IranianPaymentProcessor {

    private const val ZARINPAL_URL = "https://www.zarinpal.com/pg/StartPay/"
    private const val IDPAY_URL = "https://idpay.ir/"
    private const val USDT_TRC20_ADDRESS = "TR7NHqjeKQxGTCuuP8qACi7c5LtZG7M3" // Placeholder
    private const val USDT_BEP20_ADDRESS = "0x1234...abcd" // Placeholder

    /**
     * Start payment flow – opens browser or wallet
     */
    fun startUnlockPayment(context: Context, amountRials: Long) {
        // Option 1: ZarinPal gateway (most common in Iran)
        val zarinIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("$ZARINPAL_URL$amountRials") // Simplified
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Option 2: Crypto fallback
        val cryptoMessage = "Send $amountRials equivalent USDT to:\nTRC20: $USDT_TRC20_ADDRESS\nBEP20: $USDT_BEP20_ADDRESS"

        // For MVP: Show dialog with instructions
        // Future: Deep link to Shetab-enabled banks
        showPaymentDialog(context, zarinIntent, cryptoMessage)
    }

    private fun showPaymentDialog(context: Context, webIntent: Intent, cryptoInfo: String) {
        // TODO: Use Compose dialog to show options
        // User chooses: Pay via gateway or USDT
        // After payment, user enters TXID → manual unlock via admin
    }

    /**
     * Verify manual payment (admin side or future serverless)
     */
    fun verifyManualPayment(txid: String): Boolean {
        // Placeholder – real would check blockchain
        return false
    }
}
