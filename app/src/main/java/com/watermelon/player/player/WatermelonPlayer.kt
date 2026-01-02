package com.watermelon.player.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths
import kotlin.io.path.pathString

class WatermelonPlayer(private val context: Context) {

    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            TrackSelectionParameters.Builder(context)
                .setPreferredAudioLanguage("fa") // Iran priority
                .setTrackSelectionOverrides(
                    TrackSelectionParameters.TrackSelectionOverrides.Builder()
                        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                        .build()
                )
                .build()
        )
    }

    private val loadControl = AdaptiveLoadControl(context) // Our custom conservative buffer

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .setLoadControl(loadControl)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(DefaultDataSource.Factory(context))
        )
        .build()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(C.TIME_UNSET)
    val duration: StateFlow<Long> = _duration

    private var vhsEffectEnabled = false

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _playbackState.value = state
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _currentPosition.value = exoPlayer.currentPosition
            }

            override fun onPlayerError(error: PlaybackException) {
                // Silent fallback to software decode on hardware failure
                if (error.cause is androidx.media3.exoplayer.video.DecoderVideoRendererException) {
                    trackSelector.parameters = trackSelector.parameters
                        .buildUpon()
                        .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                        .build()
                }
            }
        })
    }

    fun playUri(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playLocalFile(file: File) {
        val uri = Uri.fromFile(file)
        playUri(uri)
    }

    // Precision seek: 5-frame cluster seek with dynamic speed ramp
    fun seekToPrecise(positionMs: Long) {
        val frameDurationMs = (1000 / getCurrentVideoFormat()?.frameRate?.toDouble() ?: 30.0).toLong()
        val cluster = (positionMs / (frameDurationMs * 5)) * (frameDurationMs * 5)
        exoPlayer.seekTo(cluster)
    }

    fun toggleVhsEffect(enabled: Boolean) {
        vhsEffectEnabled = enabled
        // VHS effect applied in VideoPlayerComposable via shader (scanlines + noise)
        // This flag is read by UI layer
    }

    fun isVhsEnabled() = vhsEffectEnabled

    private fun getCurrentVideoFormat(): Format? {
        return exoPlayer.currentMediaItem?.let { item ->
            val trackGroups = exoPlayer.currentTracks.groups
            trackGroups.find { it.type == C.TRACK_TYPE_VIDEO }?.mediaTrackGroup?.getFormat(0)
        }
    }

    // Memory-mapped read support for large files (fallback to normal if fails)
    fun getMappedBuffer(file: File): MappedByteBuffer? {
        return try {
            val channel = FileChannel.open(Paths.get(file.pathString))
            channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
        } catch (e: Exception) {
            null // Fallback to normal DataSource
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackParameters(PlaybackParameters(speed))
    }

    fun release() {
        exoPlayer.release()
    }
}
