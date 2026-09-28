package com.smartremind.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartremind.app.ui.SettingsScreen
import com.smartremind.app.ui.SettingsViewModel
import com.smartremind.app.ui.SmartRemindTheme

// AppCompatActivity потрібна для AppCompatDelegate.setApplicationLocales() на Android < 13
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            SmartRemindTheme(dynamic = settings?.dynamicColor ?: true) {
                SettingsScreen(vm)
            }
        }
    }
}
