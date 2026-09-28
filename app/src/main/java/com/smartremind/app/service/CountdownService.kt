package com.smartremind.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.smartremind.app.data.SettingsRepository
import com.smartremind.app.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Короткоживучий foreground-сервіс: показує Live-сповіщення і щосекунди рухає шкалу таймера.
 * Сам зупиняється, коли таймер добіг кінця.
 */
class CountdownService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job?.cancel()
        job = scope.launch {
            val app = applicationContext
            val settings = SettingsRepository(app).get()
            val totalSec = settings.timerMinutes.coerceAtLeast(1) * 60
            val startElapsed = SystemClock.elapsedRealtime()
            val endWall = System.currentTimeMillis() + totalSec * 1000L
            var inForeground = false

            while (true) {
                val elapsed = ((SystemClock.elapsedRealtime() - startElapsed) / 1000)
                    .toInt()
                    .coerceAtMost(totalSec)
                val notification = NotificationHelper.build(app, settings, elapsed, totalSec, endWall)

                if (!inForeground) {
                    try {
                        if (Build.VERSION.SDK_INT >= 34) {
                            startForeground(
                                NotificationHelper.NOTIFICATION_ID,
                                notification,
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                            )
                        } else {
                            startForeground(NotificationHelper.NOTIFICATION_ID, notification)
                        }
                        inForeground = true
                    } catch (e: Exception) {
                        // Система не дозволила foreground-сервіс — показуємо просте сповіщення
                        NotificationHelper.showStatic(app, settings)
                        stopSelf()
                        return@launch
                    }
                } else {
                    NotificationHelper.notify(app, notification)
                }

                if (elapsed >= totalSec) break
                delay(1000)
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, CountdownService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CountdownService::class.java))
        }
    }
}
