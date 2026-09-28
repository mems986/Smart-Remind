package com.smartremind.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.smartremind.app.ui.SettingsScreen
import com.smartremind.app.ui.SmartRemindTheme

// AppCompatActivity потрібна для AppCompatDelegate.setApplicationLocales() на Android < 13
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartRemindTheme {
                SettingsScreen()
            }
        }
    }
}
