package com.watermelon.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.watermelon.player.billing.IranianPaymentProcessor
import com.watermelon.player.config.EditionManager

/**
 * BillingScreen.kt
 * Purpose: Unlock full version screen.
 * Iran edition: Shows local payment options + USDT.
 * Global: Placeholder for Google Billing.
 * Simple, no ads, no tracking.
 */

@Composable
fun BillingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Unlock Watermelon Pro",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Remove limits • Full vault • Ad-free forever",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        if (EditionManager.isIranEdition) {
            Button(
                onClick = { IranianPaymentProcessor.startUnlockPayment(LocalContext.current, 150000) }, // 150k IRR placeholder
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("پرداخت با درگاه ایرانی (۱۵۰,۰۰۰ تومان)")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* Show USDT QR + address */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("پرداخت با USDT (TRC20/BEP20)")
            }
        } else {
            Button(
                onClick = { /* Global billing */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Unlock with Google Play ($4.99)")
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "One-time payment. No subscription.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
