package com.smartremind.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartremind.app.notification.NotificationHelper

class CloseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.cancel(context)
    }
}
