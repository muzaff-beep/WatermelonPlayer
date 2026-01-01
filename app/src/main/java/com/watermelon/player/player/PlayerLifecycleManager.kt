package com.watermelon.player.player

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.watermelon.player.service.PlaybackService

class PlayerLifecycleManager : Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var activeActivities = 0
    private var isAppInForeground = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        activeActivities++
        if (activeActivities == 1) {
            isAppInForeground = true
            // Resume playback
            PlaybackService.startPlayback(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        activeActivities--
        if (activeActivities == 0) {
            isAppInForeground = false
            // Pause or move to background service
            PlaybackService.pausePlayback()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppStop() {
        // Save playback state
    }
}
