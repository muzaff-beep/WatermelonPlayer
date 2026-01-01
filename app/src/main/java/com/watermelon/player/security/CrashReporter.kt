package com.watermelon.player.security

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashReporter(private val context: Context) {
    private val crashLogDir = File(context.filesDir, "crash_logs")

    init {
        if (!crashLogDir.exists()) crashLogDir.mkdirs()
    }

    fun logCrash(throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val logFile = File(crashLogDir, "crash_${System.currentTimeMillis()}.txt")
        logFile.writeText(stackTrace)

        Log.e("WatermelonCrash", "Crash logged locally: ${logFile.name}")
        // No transmission — offline only
    }
}
