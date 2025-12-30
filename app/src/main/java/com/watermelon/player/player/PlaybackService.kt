package com.watermelon.player.player

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media3.exoplayer.ExoPlayer
import timber.log.Timber

class PlaybackService : Service() {
    
    private var exoPlayer: ExoPlayer? = null
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("PlaybackService created")
        
        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("PlaybackService started")
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
        Timber.d("PlaybackService destroyed")
    }
}
