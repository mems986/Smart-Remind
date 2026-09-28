package com.smartremind.app.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartremind.app.MainActivity
import com.smartremind.app.R
import com.smartremind.app.data.AppSettings
import com.smartremind.app.data.PILL_LABEL
import com.smartremind.app.receiver.CloseReceiver
import com.smartremind.app.util.AppUtils
import com.smartremind.app.util.LocaleHelper

/**
 * Live Update (Android 16+): promoted ongoing notification.
 *  - «піла» у статус-барі поруч із годинником = маленька іконка + setShortCriticalText (зворотний відлік або напис);
 *  - у шторці — заголовок, текст, шкала прогресу і дві кнопки (Відкрити / Закрити).
 * На Android 8–15 це звичайне ongoing-сповіщення з тією ж шкалою та кнопками.
 * Щосекунди оновлює CountdownService.
 */
object NotificationHelper {
    const val CHANNEL_ALERT = "remind_live_alert"
    const val CHANNEL_SILENT = "remind_live_silent"
    const val NOTIFICATION_ID = 42
    const val ACTION_CLOSE = "com.smartremind.app.ACTION_CLOSE"

    private const val ACCENT = 0xFF6A4FE0.toInt()

    fun formatRemaining(seconds: Int): String {
        val s = seconds.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }

    private fun ensureChannels(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        // Старі канали з попередніх версій
        nm.deleteNotificationChannel("remind_live")
        nm.deleteNotificationChannel("remind_live_v2")

        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ALERT, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.channel_alert_name))
                .setDescription(context.getString(R.string.channel_alert_desc))
                .setShowBadge(false)
                .build()
        )
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_SILENT, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.channel_silent_name))
                .setDescription(context.getString(R.string.channel_silent_desc))
                .setShowBadge(false)
                .build()
        )
    }

    /**
     * @param elapsedSec скільки секунд минуло
     * @param totalSec повна тривалість таймера
     * @param oneShot true — сповіщення без сервісу: система сама скасує його по завершенню
     */
    fun build(
        context: Context,
        settings: AppSettings,
        elapsedSec: Int,
        totalSec: Int,
        oneShot: Boolean = false
    ): Notification {
        val loc = LocaleHelper.wrap(context, settings.language)
        ensureChannels(loc)

        val total = totalSec.coerceAtLeast(1)
        val elapsed = elapsedSec.coerceIn(0, total)
        val remainingText = formatRemaining(total - elapsed)

        val title = settings.label.ifBlank { loc.getString(R.string.default_label) }
        val appName = AppUtils.appLabel(context, settings.targetPackage)
            ?: settings.targetPackage.takeIf { it.isNotBlank() }
        val body = if (appName != null) {
            loc.getString(R.string.notif_body_app, appName)
        } else {
            loc.getString(R.string.notif_body_generic)
        }
        // Текст у піле: зворотний відлік або напис (піла вміщує ~7 символів)
        val pillText = if (settings.pillContent == PILL_LABEL) title.take(7) else remainingText

        val openPi = openIntent(context, settings)
        val closePi = closeIntent(context)

        val builder = NotificationCompat.Builder(
            context,
            if (settings.alertOnStart) CHANNEL_ALERT else CHANNEL_SILENT
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(loc.getString(R.string.notif_remaining, remainingText))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShortCriticalText(pillText)
            .setRequestPromotedOngoing(true)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setColor(ACCENT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setProgress(total, elapsed, false)
            .setContentIntent(openPi)
            // Дві стандартні кнопки: система сама підбирає їм контрастні кольори (видно на Android 16/17)
            .addAction(NotificationCompat.Action(R.drawable.ic_open, loc.getString(R.string.action_open), openPi))
            .addAction(NotificationCompat.Action(R.drawable.ic_close, loc.getString(R.string.action_close), closePi))

        if (oneShot) builder.setTimeoutAfter(total * 1000L)
        return builder.build()
    }

    @SuppressLint("MissingPermission")
    fun notify(context: Context, notification: Notification) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        nm.notify(NOTIFICATION_ID, notification)
    }

    /** Запасний варіант без сервісу (якщо систему не вдалося змусити запустити foreground-сервіс). */
    fun showStatic(context: Context, settings: AppSettings) {
        val totalSec = settings.timerMinutes.coerceAtLeast(1) * 60
        notify(context, build(context, settings, 0, totalSec, oneShot = true))
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    /** «Відкрити» та тап по сповіщенню: обраний застосунок, а якщо не обрано — екран налаштувань. */
    private fun openIntent(context: Context, settings: AppSettings): PendingIntent {
        val pkg = settings.targetPackage.trim()
        val launch = if (pkg.isNotEmpty()) {
            context.packageManager.getLaunchIntentForPackage(pkg)
        } else null
        val intent = (launch ?: Intent(context, MainActivity::class.java))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
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
