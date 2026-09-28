package com.smartremind.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartremind.app.notification.NotificationHelper
import com.smartremind.app.service.CountdownService

class CloseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CountdownService.stop(context)
        NotificationHelper.cancel(context)
    }
}
