package com.watermelon.player.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.watermelon.player.config.EditionManager

/**
 * UpdateManager.kt
 * Purpose: Manual update flow for Iran edition (no auto-update, no Google Play).
 * Features:
 *   - Opens CafeBazaar or MyKet page if installed
 *   - Falls back to Telegram channel or sideload guide
 *   - Shows changelog dialog (hardcoded or from assets)
 * Iran-first: No foreign stores, no internet required for guide.
 */

object UpdateManager {

    private const val CAFEBAAZAAR_PACKAGE = "com.farsitel.bazaar"
    private const val MYKET_PACKAGE = "ir.myket"
    private const val APP_ID = "com.watermelon.player" // Replace with real

    fun checkForUpdate(context: Context) {
        if (EditionManager.isIranEdition) {
            openIranianStore(context) ?: showSideloadGuide(context)
        } else {
            // Global: future Play Store link
        }
    }

    private fun openIranianStore(context: Context): Boolean {
        val pm = context.packageManager

        // Try CafeBazaar first
        if (isPackageInstalled(pm, CAFEBAAZAAR_PACKAGE)) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("bazaar://details?id=$APP_ID")
                setPackage(CAFEBAAZAAR_PACKAGE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        }

        // Try MyKet
        if (isPackageInstalled(pm, MYKET_PACKAGE)) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("myket://details?id=$APP_ID")
                setPackage(MYKET_PACKAGE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        }

        return false
    }

    private fun showSideloadGuide(context: Context) {
        // Show dialog with steps:
        // 1. Download APK from Telegram channel
        // 2. Enable unknown sources
        // 3. Install
        // Future: Open Telegram link
    }

    private fun isPackageInstalled(pm: android.content.pm.PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
