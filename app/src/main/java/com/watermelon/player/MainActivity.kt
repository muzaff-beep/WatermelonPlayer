package com.watermelon.player

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.watermelon.player.config.EditionManager
import com.watermelon.player.config.LocaleManager
import com.watermelon.player.databinding.ActivityMainBinding
import com.watermelon.player.player.WatermelonPlayer
import kotlinx.coroutines.launch
import androidx.media3.common.Player

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: WatermelonPlayer
    private var isSeeking = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize edition system
        EditionManager.initialize(this)
        
        // Check if Persian locale is detected
        val isPersian = LocaleManager.isPersianLocale(this)
        val useRtl = LocaleManager.shouldUseRtlLayout(this)
        
        // Initialize media player
        player = WatermelonPlayer(this)
        
        // Setup UI
        setupUI()
        
        // Setup player listeners
        setupPlayerListeners()
        
        // Apply RTL layout if needed
        if (useRtl) {
            applyRtlLayout()
        }
        
        // Display edition info
        displayEditionInfo(isPersian, useRtl)
    }
    
    private fun setupUI() {
        // Play/Pause button
        binding.btnPlayPause.setOnClickListener {
            player.togglePlayPause()
        }
        
        // Stop button
        binding.btnStop.setOnClickListener {
            player.stop()
        }
        
        // Next button
        binding.btnNext.setOnClickListener {
            player.next()
        }
        
        // Previous button
        binding.btnPrev.setOnClickListener {
            player.previous()
        }
        
        // Seek forward
        binding.btnForward.setOnClickListener {
            player.seekForward()
        }
        
        // Seek backward
        binding.btnBackward.setOnClickListener {
            player.seekBackward()
        }
        
        // Shuffle button
        binding.btnShuffle.setOnClickListener {
            player.toggleShuffle()
        }
        
        // Repeat button
        binding.btnRepeat.setOnClickListener {
            toggleRepeatMode()
        }
        
        // Volume seekbar
        binding.seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100f
                    player.setVolume(volume)
                    binding.tvVolume.text = "${progress}%"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Player seekbar
        binding.seekbarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !isSeeking) {
                    val duration = player.getDurationMs()
                    if (duration > 0) {
                        val newPosition = (progress * duration) / 1000
                        binding.tvCurrentTime.text = WatermelonPlayer.formatDuration(newPosition)
                    }
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeeking = false
                val duration = player.getDurationMs()
                if (duration > 0) {
                    val newPosition = (seekBar?.progress ?: 0).toLong() * duration / 1000
                    player.seekTo(newPosition)
                }
            }
        })
    }
    
    private fun setupPlayerListeners() {
        // Observe playback state
        lifecycleScope.launch {
            player.playbackState.collect { state ->
                when (state) {
                    is WatermelonPlayer.PlaybackState.PLAYING -> {
                        binding.btnPlayPause.text = "⏸️"
                        binding.tvStatus.text = "Playing"
                    }
                    is WatermelonPlayer.PlaybackState.PAUSED -> {
                        binding.btnPlayPause.text = "▶️"
                        binding.tvStatus.text = "Paused"
                    }
                    is WatermelonPlayer.PlaybackState.BUFFERING -> {
                        binding.tvStatus.text = "Buffering..."
                    }
                    is WatermelonPlayer.PlaybackState.ENDED -> {
                        binding.tvStatus.text = "Ended"
                    }
                    is WatermelonPlayer.PlaybackState.ERROR -> {
                        binding.tvStatus.text = "Error: ${state.message}"
                        Toast.makeText(this@MainActivity, "Playback error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.tvStatus.text = "Ready"
                    }
                }
            }
        }
        
        // Observe current position
        lifecycleScope.launch {
            player.currentPosition.collect { position ->
                if (!isSeeking) {
                    val duration = player.getDurationMs()
                    if (duration > 0) {
                        val progress = (position * 1000 / duration).toInt()
                        binding.seekbarPlayer.progress = progress
                        binding.tvCurrentTime.text = WatermelonPlayer.formatDuration(position)
                    }
                }
            }
        }
        
        // Observe duration
        lifecycleScope.launch {
            player.duration.collect { duration ->
                binding.tvTotalTime.text = WatermelonPlayer.formatDuration(duration)
                if (duration > 0) {
                    binding.seekbarPlayer.max = 1000
                }
            }
        }
        
        // Observe volume
        lifecycleScope.launch {
            player.volume.collect { volume ->
                val progress = (volume * 100).toInt()
                binding.seekbarVolume.progress = progress
                binding.tvVolume.text = "${progress}%"
            }
        }
        
        // Observe shuffle mode
        lifecycleScope.launch {
            player.isShuffleEnabled.collect { enabled ->
                binding.btnShuffle.text = if (enabled) "🔀 ON" else "🔀 OFF"
            }
        }
        
        // Observe repeat mode
        lifecycleScope.launch {
            player.repeatMode.collect { mode ->
                val repeatText = when (mode) {
                    Player.REPEAT_MODE_OFF -> "🔁 OFF"
                    Player.REPEAT_MODE_ONE -> "🔂 ONE"
                    Player.REPEAT_MODE_ALL -> "🔁 ALL"
                    else -> "🔁"
                }
                binding.btnRepeat.text = repeatText
            }
        }
    }
    
    private fun toggleRepeatMode() {
        val currentMode = player.repeatMode.value
        val nextMode = when (currentMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.setRepeatMode(nextMode)
    }
    
    private fun applyRtlLayout() {
        // For Persian/RTL layout adjustments
        binding.root.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        binding.tvTitle.textDirection = android.view.View.TEXT_DIRECTION_RTL
        binding.tvStatus.textDirection = android.view.View.TEXT_DIRECTION_RTL
    }
    
    private fun displayEditionInfo(isPersian: Boolean, useRtl: Boolean) {
        val edition = EditionManager.getCurrentEdition()
        val features = EditionManager.getFeatures()
        
        val info = """
            📱 Watermelon Player
            
            Edition: ${edition.displayName}
            
            Features:
            • Internet Required: ${features.requiresInternet}
            • Persian Support: ${features.supportsPersian}
            • Current Locale: ${if (isPersian) "Persian (فارسی)" else "English"}
            • RTL Layout: ${if (useRtl) "Enabled ←" else "Disabled →"}
            • Payment Methods: ${features.paymentMethods.joinToString()}
            
            Player Ready: ✓
            Format Support: ${WatermelonPlayer.SUPPORTED_FORMATS.size} formats
        """.trimIndent()
        
        binding.tvTitle.text = info
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
