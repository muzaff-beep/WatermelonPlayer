package com.watermelon.player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.watermelon.player.ui.theme.WatermelonPlayerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var isSeeking by mutableStateOf(false)

    private val notificationPermissionCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize edition and locale
        EditionManager.initialize(this)
        val isPersian = LocaleManager.isPersianLocale(this)
        val useRtl = LocaleManager.shouldUseRtlLayout(this)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Connect to PlaybackService
        connectToPlaybackService()

        setContent {
            WatermelonPlayerTheme {
                val isPlaying by remember {
                    derivedStateOf {
                        controller?.isPlaying == true
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Full-screen idle background when nothing is playing
                    if (!isPlaying && (controller?.playbackState == Player.STATE_IDLE || controller?.playbackState == Player.STATE_ENDED)) {
                        Image(
                            painter = painterResource(id = R.drawable.background_tv_idle),
                            contentDescription = "WatermelonPlayer idle background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Optional dark overlay for better text visibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    }

                    // Your main player UI goes here
                    // Replace this with your actual Compose UI (buttons, seekbars, etc.)
                    PlayerUI(
                        controller = controller,
                        isSeeking = isSeeking,
                        onSeekingChange = { isSeeking = it },
                        isPersian = isPersian,
                        useRtl = useRtl
                    )
                }
            }
        }
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
                // Force UI update
                (this as ComponentActivity).setContent { } // Trigger recomposition
            }
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        controller?.let { MediaController.releaseFuture(controllerFuture) }
        controllerFuture = null
        controller = null
    }
}

// Placeholder for your actual player UI
@Composable
fun PlayerUI(
    controller: MediaController?,
    isSeeking: Boolean,
    onSeekingChange: (Boolean) -> Unit,
    isPersian: Boolean,
    useRtl: Boolean
) {
    // Implement your buttons, seekbars, title display here
    // Use controller?.play(), controller?.pause(), etc.
    // This is where your current binding-based UI logic should be migrated to Compose
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Temporary placeholder
        androidx.compose.material3.Text(
            text = if (controller?.isPlaying == true) "Now Playing..." else "Select media to play",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
