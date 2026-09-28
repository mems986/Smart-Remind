package com.smartremind.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smartremind.app.scheduler.RescheduleWorker
import java.util.concurrent.TimeUnit

class SmartRemindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Страховка: раз на 6 годин перевіряємо, що наступний будильник запланований
        // (деякі прошивки скидають AlarmManager при "очищенні" застосунку).
        val request = PeriodicWorkRequestBuilder<RescheduleWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reschedule_alarms",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
