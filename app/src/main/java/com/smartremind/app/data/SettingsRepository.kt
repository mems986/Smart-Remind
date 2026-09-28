package com.smartremind.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "settings")

private val K_LANGUAGE = stringPreferencesKey("language")
private val K_TARGET = stringPreferencesKey("target_package")
private val K_TIMER = intPreferencesKey("timer_minutes")
private val K_LABEL = stringPreferencesKey("label")
private val K_DAYS = intPreferencesKey("days_mask")
private val K_TIMES = stringPreferencesKey("times")
private val K_ENABLED = booleanPreferencesKey("enabled")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val flow: Flow<AppSettings> = store.data.map { it.toSettings() }

    suspend fun get(): AppSettings = flow.first()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { prefs ->
            val s = transform(prefs.toSettings())
            prefs[K_LANGUAGE] = s.language
            prefs[K_TARGET] = s.targetPackage
            prefs[K_TIMER] = s.timerMinutes
            prefs[K_LABEL] = s.label
            prefs[K_DAYS] = s.daysMask
            prefs[K_TIMES] = s.times.distinct().sorted().joinToString(",")
            prefs[K_ENABLED] = s.enabled
        }
    }

    private fun Preferences.toSettings(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            language = this[K_LANGUAGE] ?: d.language,
            targetPackage = this[K_TARGET] ?: d.targetPackage,
            timerMinutes = this[K_TIMER] ?: d.timerMinutes,
            label = this[K_LABEL] ?: d.label,
            daysMask = this[K_DAYS] ?: d.daysMask,
            times = this[K_TIMES]
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.sorted()
                ?: d.times,
            enabled = this[K_ENABLED] ?: d.enabled
        )
    }
}
