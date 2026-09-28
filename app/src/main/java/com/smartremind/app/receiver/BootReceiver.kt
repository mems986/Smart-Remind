package com.smartremind.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartremind.app.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Перепланує наступний слот після перезавантаження, оновлення застосунку, зміни часу/пояса. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                AlarmScheduler.rescheduleFromStore(app)
            } finally {
                pending.finish()
            }
        }
    }
}
