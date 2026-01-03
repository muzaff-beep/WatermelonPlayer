package com.watermelon.player.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference

/**
 * PerformanceMonitor.kt - Enhanced for 2026
 * 
 * Purpose: Aggressive memory management for low-RAM devices (Samsung A23) and Android TV.
 * - Periodic idle RAM check
 * - Force GC when RAM exceeds target
 * - Clear Coil caches
 * - Global low-RAM Coil config (no memory cache)
 * 
 * Iran-first: Essential for smooth playback on budget TVs and phones.
 */

object PerformanceMonitor {

    private var monitorJob: Job? = null
    private var contextRef: WeakReference<Context>? = null

    private const val TARGET_IDLE_RAM_MB = 150
    private const val CHECK_INTERVAL_MS = 30_000L // 30 seconds
    private const val MAX_DISK_CACHE_PERCENT = 0.02 // 2% of storage
    private const val MAX_DISK_CACHE_MB_FALLBACK = 50 // Max 50MB

    fun startMonitoring(context: Context) {
        contextRef = WeakReference(context.applicationContext)
        if (monitorJob?.isActive == true) return

        monitorJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(CHECK_INTERVAL_MS)
                enforceLowRamPolicy()
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        contextRef?.clear()
        contextRef = null
    }

    fun enforceLowRamPolicy() {
        val context = contextRef?.get() ?: return
        val currentRamMb = getCurrentRamUsageMb(context)

        if (currentRamMb > TARGET_IDLE_RAM_MB) {
            // Aggressive GC
            System.gc()
            Runtime.getRuntime().gc()
            Debug.gc(true)

            // Clear Coil caches via new ImageLoader instance
            try {
                val imageLoader = ImageLoader(context)
                imageLoader.memoryCache.clear()
                imageLoader.diskCache.clear()
                imageLoader.shutdown()
            } catch (e: Exception) {
                // Ignore - Coil may not be initialized yet
            }
        }
    }

    private fun getCurrentRamUsageMb(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val nativeHeapMb = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
        val javaHeapMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)

        return (nativeHeapMb + javaHeapMb).toInt()
    }

    /**
     * Global Coil configuration for low-RAM devices
     * Called once in Application.onCreate()
     */
    fun configureCoilForLowRam(context: Context): ImageLoader {
        return ImageLoader.Builder(context.applicationContext)
            .memoryCachePolicy(CachePolicy.DISABLED) // No L1 memory cache - saves RAM
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_image_cache"))
                    .maxSizePercent(MAX_DISK_CACHE_PERCENT)
                    .maxSizeBytes(MAX_DISK_CACHE_MB_FALLBACK * 1024 * 1024L)
                    .build()
            }
            .build()
    }
}
