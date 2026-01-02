package com.watermelon.player.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import com.watermelon.player.player.WatermelonPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object PerformanceMonitor {

    private var monitorJob: Job? = null
    private lateinit var contextRef: WeakReference<Context>
    private const val TARGET_IDLE_RAM_MB = 150
    private const val GC_CHECK_INTERVAL_MS = 30_000L // Every 30s

    fun startMonitoring(context: Context) {
        contextRef = WeakReference(context)
        if (monitorJob?.isActive == true) return

        monitorJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(GC_CHECK_INTERVAL_MS)
                enforceLowRamPolicy()
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun enforceLowRamPolicy() {
        val context = contextRef.get() ?: return
        val currentRamMb = getCurrentRamUsageMb(context)

        if (currentRamMb > TARGET_IDLE_RAM_MB) {
            // Force GC + clear Coil caches
            System.gc()
            Debug.gc(true)

            // Clear Coil memory and disk cache aggressively
            ImageLoader(context).apply {
                memoryCache?.clear()
                diskCache?.clear()
            }

            // Optional: Clear any thumbnail caches here
        }
    }

    private fun getCurrentRamUsageMb(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val nativeHeap = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
        val dalvikHeap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)

        return (nativeHeap + dalvikHeap).toInt()
    }

    // Helper for lazy Coil config (call once in Application)
    fun configureCoilForLowRam(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.DISABLED) // L1 off – critical for TVs
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 2% of available
                    .build()
            }
            .build()
    }
}
