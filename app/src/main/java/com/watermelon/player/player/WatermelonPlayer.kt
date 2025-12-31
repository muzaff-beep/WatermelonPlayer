package com.watermelon.player.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Core media player engine for Watermelon Player
 * Supports both Iranian (offline) and Global (streaming) editions
 */
class WatermelonPlayer(context: Context) {
    
    // ExoPlayer instance
    private val exoPlayer: ExoPlayer
    
    // Data source factory
    private val dataSourceFactory: DefaultDataSource.Factory
    
    // Coroutine scope for background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    
    // State flows for UI observation
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentMedia = MutableStateFlow<MediaItem?>(null)
    val currentMedia: StateFlow<MediaItem?> = _currentMedia.asStateFlow()
    
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()
    
    // Playback states
    sealed class PlaybackState {
        object IDLE : PlaybackState()
        object BUFFERING : PlaybackState()
        object READY : PlaybackState()
        object PLAYING : PlaybackState()
        object PAUSED : PlaybackState()
        object ENDED : PlaybackState()
        data class ERROR(val message: String) : PlaybackState()
    }
    
    init {
        // Initialize ExoPlayer with optimized settings for Iranian edition (offline-first)
        exoPlayer = ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5000)  // 5 seconds back
            .setSeekForwardIncrementMs(5000) // 5 seconds forward
            .setPauseAtEndOfMediaItems(true)
            .build()
        
        // Initialize data source factory
        dataSourceFactory = DefaultDataSource.Factory(context)
        
        // Set up player listeners
        setupPlayerListeners()
    }
    
    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState(playbackState)
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _currentPosition.value = exoPlayer.currentPosition
            }
            
            override fun onEvents(player: Player, events: Player.Events) {
                // Update duration and position
                _duration.value = exoPlayer.duration
                _currentPosition.value = exoPlayer.currentPosition
                _volume.value = exoPlayer.volume
                _playbackSpeed.value = exoPlayer.playbackParameters.speed
                _isShuffleEnabled.value = exoPlayer.shuffleModeEnabled
                _repeatMode.value = exoPlayer.repeatMode
            }
            
            override fun onPlayerError(error: PlaybackException) {
                _playbackState.value = PlaybackState.ERROR(error.message ?: "Unknown error")
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMedia.value = mediaItem
            }
        })
        
        // Start position update coroutine
        startPositionUpdates()
    }
    
    private fun startPositionUpdates() {
        coroutineScope.launch {
            while (true) {
                delay(500) // Update every 500ms
                if (exoPlayer.isPlaying) {
                    _currentPosition.value = exoPlayer.currentPosition
                    _duration.value = exoPlayer.duration
                }
            }
        }
    }
    
    private fun updatePlaybackState(state: Int) {
        _playbackState.value = when (state) {
            Player.STATE_IDLE -> PlaybackState.IDLE
            Player.STATE_BUFFERING -> PlaybackState.BUFFERING
            Player.STATE_READY -> PlaybackState.READY
            Player.STATE_ENDED -> PlaybackState.ENDED
            else -> PlaybackState.IDLE
        }
    }
    
    // Media Control Methods
    
    /**
     * Play media from URI (local file or network)
     */
    fun playMedia(uri: Uri, title: String? = null, artist: String? = null) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title ?: uri.lastPathSegment ?: "Unknown")
                    .setArtist(artist ?: "Unknown Artist")
                    .build()
            )
            .build()
        
        playMedia(mediaItem)
    }
    
    /**
     * Play a MediaItem
     */
    fun playMedia(mediaItem: MediaItem) {
        _currentMedia.value = mediaItem
        
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
        
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
    }
    
    /**
     * Add media to queue and play next
     */
    fun addToQueue(uri: Uri, title: String? = null) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title ?: uri.lastPathSegment ?: "Unknown")
                    .build()
            )
            .build()
        
        exoPlayer.addMediaItem(mediaItem)
    }
    
    /**
     * Basic controls
     */
    fun play() {
        exoPlayer.play()
    }
    
    fun pause() {
        exoPlayer.pause()
    }
    
    fun stop() {
        exoPlayer.stop()
        _currentMedia.value = null
        _playbackState.value = PlaybackState.IDLE
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }
    
    fun seekForward() {
        exoPlayer.seekForward()
    }
    
    fun seekBackward() {
        exoPlayer.seekBack()
    }
    
    fun next() {
        exoPlayer.seekToNextMediaItem()
    }
    
    fun previous() {
        exoPlayer.seekToPreviousMediaItem()
    }
    
    /**
     * Playback settings
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }
    
    fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
        _volume.value = exoPlayer.volume
    }
    
    fun toggleShuffle() {
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
        _isShuffleEnabled.value = exoPlayer.shuffleModeEnabled
    }
    
    fun setRepeatMode(mode: Int) {
        exoPlayer.repeatMode = mode
        _repeatMode.value = mode
    }
    
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Utility methods
     */
    fun isPlaying(): Boolean = exoPlayer.isPlaying
    
    fun getCurrentPositionMs(): Long = exoPlayer.currentPosition
    
    fun getDurationMs(): Long = exoPlayer.duration
    
    fun getBufferedPosition(): Long = exoPlayer.bufferedPosition
    
    fun getBufferedPercentage(): Int = exoPlayer.bufferedPercentage
    
    fun getAudioSessionId(): Int = exoPlayer.audioSessionId
    
    /**
     * Cleanup
     */
    fun release() {
        exoPlayer.release()
        coroutineScope.cancel()
    }
    
    companion object {
        /**
         * Format milliseconds to MM:SS or HH:MM:SS
         */
        fun formatDuration(millis: Long): String {
            val totalSeconds = millis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
            }
        }
        
        /**
         * Check if file format is supported
         */
        fun isFormatSupported(filePath: String): Boolean {
            val extension = filePath.substringAfterLast('.', "").lowercase(Locale.getDefault())
            return SUPPORTED_FORMATS.contains(extension)
        }
        
        /**
         * Supported formats for Iranian edition (offline focus)
         */
        val SUPPORTED_FORMATS = listOf(
            // Audio
            "mp3", "m4a", "aac", "ogg", "wav", "flac", "opus", "wma",
            // Video
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp",
            // Playlists
            "m3u", "m3u8", "pls"
        )
    }
}
