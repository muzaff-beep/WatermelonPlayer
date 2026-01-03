package com.watermelon.player.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * TamperDetector.kt - Enhanced 2026 version
 * 
 * Purpose: Protect paid features and detect piracy/debugging in release builds.
 * 
 * Checks:
 * - APK signature validation (SHA-256 against known release key)
 * - Debuggable flag
 * - Root/Magisk detection (improved paths + Magisk mount check)
 * - Emulator detection (stronger heuristics)
 * - Basic hooking framework detection (Frida/Xposed)
 * 
 * Iran-first: Critical for preventing cracked APKs on Cafe Bazaar/MyKet.
 * 
 * IMPORTANT: Fill EXPECTED_SIGNATURE_SHA256 with your release signing key fingerprint.
 * Generate with: gradlew signingReport (look for SHA-256 under release variant)
 */

object TamperDetector {

    // TODO: Replace with your actual release key SHA-256 fingerprint
    // Example: byteArrayOf(0x12, 0x34, ... ) // 32 bytes
    private val EXPECTED_SIGNATURE_SHA256: ByteArray = byteArrayOf(
        // ← FILL THIS ON EVERY RELEASE BUILD
        // Use ./gradlew signingReport to get it
    )

    fun isTampered(context: Context): Boolean {
        return isDebuggable(context) ||
                isRooted() ||
                isEmulator() ||
                isHookingFrameworkDetected() ||
                !isSignatureValid(context)
    }

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isRooted(): Boolean {
        // Standard su paths
        val commonPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        if (commonPaths.any { File(it).exists() }) return true

        // Magisk detection - check mount points
        return try {
            val mounts = File("/proc/mounts").readText()
            mounts.contains("magisk") || mounts.contains("com.topjohnwu.magisk")
        } catch (e: Exception) {
            false
        }
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.HARDWARE.contains("goldfish") ||  // Emulator kernel
                Build.HARDWARE.contains("ranchu") ||
                Build.PRODUCT.contains("sdk_gphone") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
    }

    private fun isHookingFrameworkDetected(): Boolean {
        // Basic Frida/Xposed detection via known library files
        val suspiciousPaths = listOf(
            "/data/local/tmp/frida-server",
            "/system/lib/libfrida-gadget.so",
            "/system/framework/XposedBridge.jar"
        )
        return suspiciousPaths.any { File(it).exists() }
    }

    private fun isSignatureValid(context: Context): Boolean {
        if (EXPECTED_SIGNATURE_SHA256.isEmpty()) {
            // Safety: if not set, skip check during development
            return true
        }

        try {
            val pm = context.packageManager
            val packageName = context.packageName

            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val md = MessageDigest.getInstance("SHA-256")

            for (signature in signatures) {
                val digest = md.digest(signature.toByteArray())
                if (digest.contentEquals(EXPECTED_SIGNATURE_SHA256)) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Any error = treat as invalid
            return false
        }

        return false
    }

    fun handleTamper() {
        // Clean process kill - harder to hook than System.exit()
        android.os.Process.killProcess(android.os.Process.myPid())
        // Optional: Show toast or dialog before kill (in UI thread)
        // But avoid long delay - attacker can hook
    }
}
