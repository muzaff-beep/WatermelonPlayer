package com.watermelon.player.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * TamperDetector.kt
 * Purpose: Prevent piracy and debugging on release builds.
 * Checks:
 *   - APK signature match (expected SHA-256)
 *   - Root detection (su binary, test-keys)
 *   - Debuggable flag
 *   - Emulator detection
 * On breach: Show warning, limit features, or crash.
 * Iran-first: Critical for paid unlock protection.
 */

object TamperDetector {

    private val EXPECTED_SIGNATURE_SHA256 = byteArrayOf( /* Fill on release */ )

    fun isTampered(context: Context): Boolean {
        return isDebuggable(context) ||
                isRooted() ||
                isEmulator() ||
                !isSignatureValid(context)
    }

    private fun isDebuggable(context: Context): Boolean {
        return context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    private fun isRooted(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion")
    }

    private fun isSignatureValid(context: Context): Boolean {
        val pm = context.packageManager
        val packageName = context.packageName
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        }

        return signatures.any { signature ->
            val digest = MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
            digest.contentEquals(EXPECTED_SIGNATURE_SHA256)
        }
    }

    fun handleTamper() {
        // Show dialog: "Tampered installation detected"
        // Disable vault, billing, etc.
        System.exit(0)
    }
}
