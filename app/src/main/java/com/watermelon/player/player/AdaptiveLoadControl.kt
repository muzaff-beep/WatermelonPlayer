package com.watermelon.player.player

import androidx.media3.common.C
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.TrackSelectionArray
import androidx.media3.exoplayer.upstream.DefaultAllocator

class AdaptiveLoadControl : LoadControl {
    private val allocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
    private val minBufferMs = 15000L
    private val maxBufferMs = 60000L
    private val bufferForPlaybackMs = 2500L
    private val bufferForPlaybackAfterRebufferMs = 5000L
    private var targetBufferBytes = 0
    private var isBuffering = false
    
    override fun onPrepared() {
        allocator.reset()
        resetBufferState()
    }
    
    override fun onTracksSelected(
        renderers: Array<Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        targetBufferBytes = 5000000 // 5MB default
        allocator.setTargetBufferSize(targetBufferBytes)
    }
    
    override fun onStopped() {
        allocator.reset()
        resetBufferState()
    }
    
    override fun onReleased() {
        allocator.reset()
    }
    
    override fun getAllocator() = allocator
    
    override fun getBackBufferDurationUs() = 0L
    
    override fun retainBackBufferFromKeyframe() = false
    
    override fun shouldContinueLoading(
        bufferedDurationUs: Long,
        playbackSpeed: Float
    ): Boolean {
        val bufferedDurationMs = bufferedDurationUs / 1000
        return if (isBuffering) {
            bufferedDurationMs < maxBufferMs
        } else {
            bufferedDurationMs < minBufferMs
        }
    }
    
    override fun shouldStartPlayback(
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean
    ): Boolean {
        val bufferedDurationMs = bufferedDurationUs / 1000
        val requiredBufferMs = if (rebuffering) {
            bufferForPlaybackAfterRebufferMs
        } else {
            bufferForPlaybackMs
        }
        return bufferedDurationMs >= requiredBufferMs
    }
    
    private fun resetBufferState() {
        targetBufferBytes = 0
        isBuffering = false
    }
}
