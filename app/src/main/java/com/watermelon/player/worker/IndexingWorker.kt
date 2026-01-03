package com.watermelon.player.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class IndexingWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // TODO: Implement media indexing logic
        // For now, succeed immediately
        return Result.success()
    }
}
