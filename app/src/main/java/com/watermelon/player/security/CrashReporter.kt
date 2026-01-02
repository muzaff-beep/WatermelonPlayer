package com.watermelon.player.security

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CrashReporter.kt
 * Purpose: Local crash logging (no remote analytics).
 * On uncaught exception: write stack trace to internal file.
 * User can view/report via Settings (future).
 * Iran-first: 100% offline, no network.
 */

object CrashReporter : Thread.UncaughtExceptionHandler {

    private lateinit var context: Context
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    fun initialize(context: Context) {
        this.context = context.applicationContext
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crashDir = File(context.filesDir, "crashes")
            crashDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val crashFile = File(crashDir, "crash_$timestamp.txt")

            val writer = StringWriter()
            throwable.printStackTrace(PrintWriter(writer))

            crashFile.writeText(
                """
                Time: ${Date()}
                Thread: ${thread.name}
                Device: ${android.os.Build.MODEL} (API ${android.os.Build.VERSION.SDK_INT})
                
                Stack trace:
                $writer
                """.trimIndent()
            )

            Log.e("WatermelonCrash", "Crash logged to ${crashFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("CrashReporter", "Failed to log crash", e)
        }

        // Pass to system
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
