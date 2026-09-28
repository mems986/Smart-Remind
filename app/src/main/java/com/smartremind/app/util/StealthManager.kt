package com.smartremind.app.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthManager {
    private const val ALIAS_CLASS = "com.smartremind.app.LauncherAlias"

    private fun alias(context: Context) = ComponentName(context, ALIAS_CLASS)

    fun setIconHidden(context: Context, hidden: Boolean) {
        val state = if (hidden) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        context.packageManager.setComponentEnabledSetting(
            alias(context),
            state,
            PackageManager.DONT_KILL_APP
        )
    }

    fun isIconHidden(context: Context): Boolean =
        context.packageManager.getComponentEnabledSetting(alias(context)) ==
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}
