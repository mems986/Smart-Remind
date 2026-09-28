package com.smartremind.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.smartremind.app.R
import com.smartremind.app.util.StealthManager

/** Набір *#*#7373#*#* у «Телефоні» повертає іконку в лаунчер. */
class SecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        StealthManager.setIconHidden(context, false)
        Toast.makeText(context, R.string.icon_restored, Toast.LENGTH_LONG).show()
    }
}
