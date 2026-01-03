package com.watermelon.player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.watermelon.player.config.EditionManager
import com.watermelon.player.config.LocaleManager
import com.watermelon.player.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var isSeeking = false

    private val notificationPermissionCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize edition and locale
        EditionManager.initialize(this)
        val isPersian = LocaleManager.isPersianLocale(this)
        val useRtl = LocaleManager.shouldUseRtlLayout(this)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Connect to PlaybackService
        connectToPlaybackService()

        // Setup UI (buttons will be enabled once controller is ready)
        setupUI()

        // Apply RTL if needed
        if (useRtl) {
            applyRtlLayout()
        }

        // Display edition info
        displayEditionInfo(isPersian, useRtl)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    notificationPermissionCode
                )
            }
        }
    }

    private fun connectToPlaybackService() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            if (controller != null) {
                setupControllerListeners()
                updateUIFromController()
            } else {
                Toast.makeText(this, "Failed to connect to playback service", Toast.LENGTH_LONG).show()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupUI() {
        binding.btnPlayPause.setOnClickListener { controller?.playWhenReady = !(controller?.playWhenReady ?: false) }
        binding.btnStop.setOnClickListener { controller?.stop() }
        binding.btnNext.setOnClickListener { controller?.seekToNext() }
        binding.btnPrev.setOnClickListener { controller?.seekToPrevious() }
        binding.btnForward.setOnClickListener { controller?.seekForward() }
        binding.btnBackward.setOnClickListener { controller?.seekBackward() }
        binding.btnShuffle.setOnClickListener { controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false) }

        binding.btnRepeat.setOnClickListener {
            val current = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
            val next = when (current) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            controller?.repeatMode = next
        }

        binding.seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    controller?.volume = progress / 100f
                    binding.tvVolume.text = "${progress}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && controller?.duration ?: 0 > 0) {
                    val position = (progress * (controller?.duration ?: 0)) / 1000
                    binding.tvCurrentTime.text = WatermelonPlayer.formatDuration(position)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { isSeeking = true }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeeking = false
                controller?.let { ctrl ->
                    val duration = ctrl.duration
                    if (duration > 0) {
                        val newPosition = (seekBar?.progress ?: 0).toLong() * duration / 1000
                        ctrl.seekTo(newPosition)
                    }
                }
            }
        })
    }

    private fun setupControllerListeners() {
        controller?.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                binding.btnPlayPause.text = if (playWhenReady) "⏸️" else "▶️"
                binding.tvStatus.text = if (playWhenReady) "Playing" else "Paused"
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> binding.tvStatus.text = "Buffering..."
                    Player.STATE_ENDED -> binding.tvStatus.text = "Ended"
                    Player.STATE_READY -> binding.tvStatus.text = if (controller?.playWhenReady == true) "Playing" else "Ready"
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                binding.tvStatus.text = "Error"
                Toast.makeText(this@MainActivity, "Playback error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updateProgress()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateProgress()
            }
        })

        // Periodic position updates
        lifecycleScope.launch {
            while (true) {
                updateProgress()
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun updateProgress() {
        controller?.let { ctrl ->
            val duration = ctrl.duration
            val position = ctrl.currentPosition

            if (duration > 0 && !isSeeking) {
                val progress = (position * 1000 / duration).coerceIn(0, 1000).toInt()
                binding.seekbarPlayer.progress = progress
                binding.tvCurrentTime.text = WatermelonPlayer.formatDuration(position)
            }

            binding.tvTotalTime.text = WatermelonPlayer.formatDuration(duration)
        }
    }

    private fun updateUIFromController() {
        controller?.let { ctrl ->
            // Volume
            val volumeProgress = (ctrl.volume * 100).toInt()
            binding.seekbarVolume.progress = volumeProgress
            binding.tvVolume.text = "${volumeProgress}%"

            // Shuffle
            binding.btnShuffle.text = if (ctrl.shuffleModeEnabled) "🔀 ON" else "🔀 OFF"

            // Repeat
            val repeatText = when (ctrl.repeatMode) {
                Player.REPEAT_MODE_OFF -> "🔁 OFF"
                Player.REPEAT_MODE_ONE -> "🔂 ONE"
                Player.REPEAT_MODE_ALL -> "🔁 ALL"
                else -> "🔁"
            }
            binding.btnRepeat.text = repeatText

            // Initial play/pause state
            binding.btnPlayPause.text = if (ctrl.playWhenReady) "⏸️" else "▶️"
        }
    }

    private fun applyRtlLayout() {
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
            
            MediaSessionService: Active ✓
            Notification: Auto-managed
        """.trimIndent()

        binding.tvTitle.text = info
    }

    override fun onStop() {
        super.onStop()
        controller?.let { MediaController.releaseFuture(controllerFuture) }
        controllerFuture = null
        controller = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Player is owned by PlaybackService — no release needed here
    }
}
