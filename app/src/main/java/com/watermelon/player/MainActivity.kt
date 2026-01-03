package com.watermelon.player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.watermelon.player.config.LocaleManager
import com.watermelon.player.ui.theme.WatermelonPlayerTheme

class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocaleManager // Force reference to ensure file exists

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        connectToPlaybackService()

        setContent {
            WatermelonPlayerTheme {
                val playbackState = controller?.playbackState ?: Player.STATE_IDLE
                val isPlaying = controller?.isPlaying == true
                val showIdleBackground = playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !isPlaying

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showIdleBackground) {
                        Image(
                            painter = painterResource(R.drawable.background_tv_idle),
                            contentDescription = "WatermelonPlayer idle background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)))  // Fixed reference
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("WatermelonPlayer", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
                        Text(text = if (isPlaying) "Playing..." else "Ready", color = Color.White)
                    }
                }
            }
        }
    }

    private fun connectToPlaybackService() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        controller?.let { MediaController.releaseFuture(controllerFuture as ListenableFuture<MediaController>) } // Fixed type mismatch
        controller = null
    }
}
