package com.smartremind.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class AppInfo(val label: String, val packageName: String)

object AppUtils {
    /** Усі застосунки з іконкою в лаунчері (крім самого Smart Remind). */
    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    /** Назва застосунку за package name, або null якщо його немає на пристрої. */
    fun appLabel(context: Context, packageName: String): String? {
        if (packageName.isBlank()) return null
        val pm = context.packageManager
        return try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
