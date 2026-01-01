package com.watermelon.player.util

import android.os.SystemClock
import java.util.concurrent.ConcurrentHashMap

object PerformanceMonitor {
    private val startTimes = ConcurrentHashMap<String, Long>()
    private val measurements = ConcurrentHashMap<String, MutableList<Long>>()
    
    fun startMeasure(key: String) {
        startTimes[key] = SystemClock.elapsedRealtime()
    }
    
    fun endMeasure(key: String): Long {
        val startTime = startTimes[key] ?: return 0
        val duration = SystemClock.elapsedRealtime() - startTime
        measurements.getOrPut(key) { mutableListOf() }.add(duration)
        startTimes.remove(key)
        return duration
    }
    
    fun getAverage(key: String): Long {
        val times = measurements[key] ?: return 0
        return if (times.isNotEmpty()) times.average().toLong() else 0
    }
    
    fun clear() {
        startTimes.clear()
        measurements.clear()
    }
    
    fun logPerformance() {
        measurements.forEach { (key, times) ->
            val avg = if (times.isNotEmpty()) times.average().toLong() else 0
            val max = if (times.isNotEmpty()) times.max() else 0
            val min = if (times.isNotEmpty()) times.min() else 0
            println("Performance[$key]: Avg=${avg}ms, Min=${min}ms, Max=${max}ms, Samples=${times.size}")
        }
    }
}
