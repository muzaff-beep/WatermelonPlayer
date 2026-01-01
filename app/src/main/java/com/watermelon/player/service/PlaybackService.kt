package com.watermelon.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.watermelon.player.MainActivity
import com.watermelon.player.R
import com.watermelon.player.config.EditionManager
import com.watermelon.player.datasource.EditionAwareDataSourceFactory
import com.watermelon.player.player.WatermelonPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var watermelonPlayer: WatermelonPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var notificationManager: NotificationManagerCompat? = null
    private val notificationId = 1001
    private val notificationChannelId = "watermelon_playback_channel"
    
    companion object {
        const val ACTION_PLAY = "com.watermelon.player.ACTION_PLAY"
        const val ACTION_PAUSE = "com.watermelon.player.ACTION_PAUSE"
        const val ACTION_STOP = "com.watermelon.player.ACTION_STOP"
        const val EXTRA_MEDIA_URI = "extra_media_uri"
        const val EXTRA_MEDIA_TITLE = "extra_media_title"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        notificationManager = NotificationManagerCompat.from(this)
        initializePlayer()
        initializeMediaSession()
        startForegroundService()
    }
    
    private fun initializePlayer() {
        watermelonPlayer = WatermelonPlayer(
            context = this,
            dataSourceFactory = EditionAwareDataSourceFactory(this)
        ).apply {
            initialize()
        }
    }
    
    private fun initializeMediaSession() {
        watermelonPlayer?.exoPlayer?.let { player ->
            mediaSession = MediaSession.Builder(this, player)
                .build()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                getString(R.string.playback_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.playback_channel_description)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundService() {
        val notification = buildNotification(
            title = getString(R.string.app_name),
            text = getString(R.string.playback_service_running)
        )
        startForeground(notificationId, notification)
    }
    
    private fun buildNotification(title: String, text: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_watermelon_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
        
        return builder.build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> watermelonPlayer?.exoPlayer?.play()
            ACTION_PAUSE -> watermelonPlayer?.exoPlayer?.pause()
            ACTION_STOP -> stopSelf()
            else -> {
                intent.getStringExtra(EXTRA_MEDIA_URI)?.let { uriString ->
                    serviceScope.launch {
                        playMedia(uriString)
                    }
                }
            }
        }
    }
    
    private suspend fun playMedia(uriString: String) {
        // TODO: Implement media playback
        // For now, just update notification
        updateNotification(
            title = "Playing media",
            text = uriString.substringAfterLast('/')
        )
    }
    
    private fun updateNotification(title: String, text: String) {
        val notification = buildNotification(title, text)
        notificationManager?.notify(notificationId, notification)
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        watermelonPlayer?.release()
        watermelonPlayer = null
        mediaSession?.release()
        mediaSession = null
        stopForeground(true)
        notificationManager?.cancel(notificationId)
        super.onDestroy()
    }
}
