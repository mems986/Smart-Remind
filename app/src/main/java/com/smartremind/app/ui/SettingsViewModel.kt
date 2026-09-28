package com.smartremind.app.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartremind.app.data.AppSettings
import com.smartremind.app.data.LANG_AUTO
import com.smartremind.app.data.SettingsRepository
import com.smartremind.app.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    /** null — поки налаштування ще завантажуються з DataStore. */
    val settings: StateFlow<AppSettings?> = repo.flow
        .map<AppSettings, AppSettings?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun update(reschedule: Boolean = false, transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            repo.update(transform)
            if (reschedule) AlarmScheduler.rescheduleFromStore(getApplication())
        }
    }

    fun setLabel(value: String) = update { it.copy(label = value) }
    fun setTargetPackage(value: String) = update { it.copy(targetPackage = value) }
    fun setTimerMinutes(value: Int) = update { it.copy(timerMinutes = value) }

    fun setEnabled(value: Boolean) = update(reschedule = true) { it.copy(enabled = value) }
    fun toggleDay(index: Int) = update(reschedule = true) { it.copy(daysMask = it.daysMask xor (1 shl index)) }
    fun addTime(minuteOfDay: Int) = update(reschedule = true) { it.copy(times = it.times + minuteOfDay) }
    fun removeTime(minuteOfDay: Int) = update(reschedule = true) { it.copy(times = it.times - minuteOfDay) }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            repo.update { it.copy(language = language) }
            AppCompatDelegate.setApplicationLocales(
                if (language == LANG_AUTO) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(language)
            )
        }
    }
}
