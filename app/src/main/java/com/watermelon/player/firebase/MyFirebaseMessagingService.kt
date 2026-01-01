package com.watermelon.player.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.watermelon.player.config.EditionManager

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (EditionManager.isIranEdition()) return // Disable for Iran Edition

        // Handle notification
        Log.d("FCM", "Message received: ${remoteMessage.notification?.title}")
        // Show notification or update UI
    }

    override fun onNewToken(token: String) {
        if (EditionManager.isIranEdition()) return

        Log.d("FCM", "Token: $token")
        // Send token to your server
    }
}
