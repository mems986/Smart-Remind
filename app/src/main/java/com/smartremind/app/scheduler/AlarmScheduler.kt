package com.smartremind.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.smartremind.app.data.AppSettings
import com.smartremind.app.data.SettingsRepository
import com.smartremind.app.receiver.AlarmReceiver
import java.time.ZonedDateTime

object AlarmScheduler {
    const val ACTION_ALARM = "com.smartremind.app.ACTION_ALARM"
    const val ACTION_TEST = "com.smartremind.app.ACTION_TEST"
    private const val RC_MAIN = 1001
    private const val RC_TEST = 1002

    /** Найближчий момент спрацювання строго після [from], або null якщо розклад порожній. */
    fun nextTriggerMillis(
        settings: AppSettings,
        from: ZonedDateTime = ZonedDateTime.now()
    ): Long? {
        if (!settings.enabled || settings.times.isEmpty() || settings.daysMask == 0) return null
        val sortedTimes = settings.times.sorted()
        for (offset in 0..7) {
            val date = from.toLocalDate().plusDays(offset.toLong())
            val dayIndex = date.dayOfWeek.value - 1 // Пн = 0
            if (!settings.hasDay(dayIndex)) continue
            for (minute in sortedTimes) {
                val candidate = date.atTime(minute / 60, minute % 60).atZone(from.zone)
                if (candidate.isAfter(from)) return candidate.toInstant().toEpochMilli()
            }
        }
        return null
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    fun reschedule(
        context: Context,
        settings: AppSettings,
        from: ZonedDateTime = ZonedDateTime.now()
    ) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context, RC_MAIN, ACTION_ALARM)
        am.cancel(pi)
        val at = nextTriggerMillis(settings, from) ?: return
        setAlarm(context, at, pi)
    }

    suspend fun rescheduleFromStore(context: Context) {
        reschedule(context, SettingsRepository(context).get())
    }

    fun scheduleTest(context: Context, seconds: Int) {
        val at = System.currentTimeMillis() + seconds * 1000L
        setAlarm(context, at, pendingIntent(context, RC_TEST, ACTION_TEST))
    }

    private fun setAlarm(context: Context, at: Long, pi: PendingIntent) {
        val am = context.getSystemService(AlarmManager::class.java)
        try {
            if (canScheduleExact(context)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun pendingIntent(context: Context, requestCode: Int, action: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
