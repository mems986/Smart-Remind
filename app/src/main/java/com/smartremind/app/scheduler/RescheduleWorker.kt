package com.smartremind.app.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RescheduleWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AlarmScheduler.rescheduleFromStore(applicationContext)
        return Result.success()
    }
}
