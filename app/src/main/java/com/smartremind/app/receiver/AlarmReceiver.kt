package com.smartremind.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartremind.app.data.SettingsRepository
import com.smartremind.app.notification.NotificationHelper
import com.smartremind.app.scheduler.AlarmScheduler
import com.smartremind.app.service.CountdownService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val action = intent.action
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(app).get()
                try {
                    CountdownService.start(app)
                } catch (e: Exception) {
                    // Foreground-сервіс заборонено (напр. неточний будильник) — показуємо статичне сповіщення
                    NotificationHelper.showStatic(app, settings)
                }
                if (action == AlarmScheduler.ACTION_ALARM) {
                    // +5 с, щоб будильник, що спрацював трохи раніше, не запланував сам себе
                    AlarmScheduler.reschedule(app, settings, ZonedDateTime.now().plusSeconds(5))
                }
            } finally {
                pending.finish()
            }
        }
    }
}
