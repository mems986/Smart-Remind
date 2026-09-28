package com.smartremind.app.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartremind.app.MainActivity
import com.smartremind.app.R
import com.smartremind.app.data.AppSettings
import com.smartremind.app.receiver.CloseReceiver
import com.smartremind.app.util.AppUtils
import com.smartremind.app.util.LocaleHelper

/**
 * Live-сповіщення з таймером.
 *  - Android 16+: Notification.ProgressStyle + «promoted ongoing» (Live Update) —
 *    чіп у статус-барі поруч із годинником та шкала прогресу в шторці.
 *  - Android 8–15: звичайне heads-up сповіщення зі шкалою прогресу та зворотним відліком.
 * Шкалу щосекунди оновлює CountdownService.
 */
object NotificationHelper {
    const val CHANNEL_ID = "remind_live_v2"
    const val NOTIFICATION_ID = 42
    const val ACTION_CLOSE = "com.smartremind.app.ACTION_CLOSE"

    private const val ACCENT = 0xFF6A4FE0.toInt()

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

    /**
     * @param elapsedSec скільки секунд минуло
     * @param totalSec повна тривалість таймера
     * @param endWallMillis момент завершення (System.currentTimeMillis)
     * @param oneShot true — сповіщення без сервісу: система сама скасує його по завершенню
     */
    fun build(
        context: Context,
        settings: AppSettings,
        elapsedSec: Int,
        totalSec: Int,
        endWallMillis: Long,
        oneShot: Boolean = false
    ): Notification {
        val loc = LocaleHelper.wrap(context, settings.language)
        ensureChannel(loc)

        val title = settings.label.ifBlank { loc.getString(R.string.default_label) }
        val appName = AppUtils.appLabel(context, settings.targetPackage)
        val body = if (appName != null) {
            loc.getString(R.string.notif_body_app, appName)
        } else {
            loc.getString(R.string.notif_body_generic)
        }
        val openLabel = loc.getString(R.string.action_open)
        val closeLabel = loc.getString(R.string.action_close)
        val openPi = openIntent(context, settings)
        val closePi = closeIntent(context)
        val safeTotal = totalSec.coerceAtLeast(1)
        val safeElapsed = elapsedSec.coerceIn(0, safeTotal)

        return if (Build.VERSION.SDK_INT >= 36) {
            buildLive(
                context, title, body, safeElapsed, safeTotal, endWallMillis, oneShot,
                openLabel, closeLabel, openPi, closePi
            )
        } else {
            buildCompat(
                context, title, body, safeElapsed, safeTotal, endWallMillis, oneShot,
                openLabel, closeLabel, openPi, closePi
            )
        }
    }

    @RequiresApi(36)
    private fun buildLive(
        context: Context,
        title: String,
        body: String,
        elapsed: Int,
        total: Int,
        endWall: Long,
        oneShot: Boolean,
        openLabel: String,
        closeLabel: String,
        openPi: PendingIntent,
        closePi: PendingIntent
    ): Notification {
        val segment = Notification.ProgressStyle.Segment(total).setColor(ACCENT)
        val style = Notification.ProgressStyle()
            .setStyledByProgress(false)
            .setProgressSegments(listOf(segment))
            .setProgress(elapsed)

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(style)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            // Зворотний відлік у чіпі статус-бару та в шапці сповіщення
            .setShowWhen(true)
            .setWhen(endWall)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            // Просимо систему показати це як Live Update
            .setRequestPromotedOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_open), openLabel, openPi
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_close), closeLabel, closePi
                ).build()
            )
        if (oneShot) builder.setTimeoutAfter(total * 1000L)
        return builder.build()
    }

    private fun buildCompat(
        context: Context,
        title: String,
        body: String,
        elapsed: Int,
        total: Int,
        endWall: Long,
        oneShot: Boolean,
        openLabel: String,
        closeLabel: String,
        openPi: PendingIntent,
        closePi: PendingIntent
    ): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setProgress(total, elapsed, false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(ACCENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setWhen(endWall)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .addAction(R.drawable.ic_open, openLabel, openPi)
            .addAction(R.drawable.ic_close, closeLabel, closePi)
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
        val end = System.currentTimeMillis() + totalSec * 1000L
        notify(context, build(context, settings, 0, totalSec, end, oneShot = true))
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    /** Кнопка «Відкрити»: обраний застосунок, а якщо не обрано — екран налаштувань. */
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
