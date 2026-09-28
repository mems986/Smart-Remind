package com.smartremind.app.notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartremind.app.MainActivity
import com.smartremind.app.R
import com.smartremind.app.data.AppSettings
import com.smartremind.app.receiver.CloseReceiver
import com.smartremind.app.util.LocaleHelper

object NotificationHelper {
    const val CHANNEL_ID = "remind_live"
    const val NOTIFICATION_ID = 42
    const val ACTION_CLOSE = "com.smartremind.app.ACTION_CLOSE"

    private fun ensureChannel(context: Context) {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.channel_name))
            .setDescription(context.getString(R.string.channel_description))
            .setShowBadge(false)
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, settings: AppSettings) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return

        // Текст сповіщення береться з мови застосунку (а не лише системної)
        val loc = LocaleHelper.wrap(context, settings.language)
        ensureChannel(loc)

        val durationMs = settings.timerMinutes.coerceAtLeast(1) * 60_000L
        val label = settings.label.ifBlank { loc.getString(R.string.default_label) }

        val views = RemoteViews(context.packageName, R.layout.notification_live).apply {
            setTextViewText(R.id.notif_label, label)
            setTextViewText(R.id.btn_open, loc.getString(R.string.action_open))
            setTextViewText(R.id.btn_close, loc.getString(R.string.action_close))
            setChronometerCountDown(R.id.notif_timer, true)
            setChronometer(R.id.notif_timer, SystemClock.elapsedRealtime() + durationMs, null, true)
            setOnClickPendingIntent(R.id.btn_open, openIntent(context, settings))
            setOnClickPendingIntent(R.id.btn_close, closeIntent(context))
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setCustomHeadsUpContentView(views)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            // Система сама скасує сповіщення, коли таймер добіг кінця
            .setTimeoutAfter(durationMs)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    /** Кнопка «Відкрити»: цільовий застосунок, а якщо його немає — екран налаштувань. */
    private fun openIntent(context: Context, settings: AppSettings): PendingIntent {
        val pkg = settings.targetPackage.trim()
        val launch = if (pkg.isNotEmpty()) {
            context.packageManager.getLaunchIntentForPackage(pkg)
        } else null
        val intent = (launch ?: Intent(context, MainActivity::class.java))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun closeIntent(context: Context): PendingIntent {
        val intent = Intent(context, CloseReceiver::class.java).setAction(ACTION_CLOSE)
        return PendingIntent.getBroadcast(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
