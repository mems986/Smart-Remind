package com.smartremind.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartremind.app.R
import com.smartremind.app.data.AppSettings
import com.smartremind.app.data.LANG_AUTO
import com.smartremind.app.notification.NotificationHelper
import com.smartremind.app.scheduler.AlarmScheduler
import com.smartremind.app.util.StealthManager
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_settings)) }) }
    ) { padding ->
        val current = settings
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionsCard()
                NotificationCard(current, vm)
                ScheduleCard(current, vm)
                LanguageCard(current, vm)
                PrivacyCard()
                TestCard(current)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

// ---------- Дозволи ----------

@Composable
private fun PermissionsCard() {
    val context = LocalContext.current
    var notifOk by remember { mutableStateOf(notificationsEnabled(context)) }
    var exactOk by remember { mutableStateOf(AlarmScheduler.canScheduleExact(context)) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notifOk = notificationsEnabled(context)
        exactOk = AlarmScheduler.canScheduleExact(context)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifOk = notificationsEnabled(context)
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !notifOk) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    SectionCard(stringResource(R.string.section_permissions)) {
        PermissionRow(
            title = stringResource(R.string.perm_notifications),
            hint = null,
            granted = notifOk,
            onGrant = {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
            }
        )
        if (Build.VERSION.SDK_INT >= 31) {
            PermissionRow(
                title = stringResource(R.string.perm_exact_alarms),
                hint = stringResource(R.string.perm_exact_hint),
                granted = exactOk,
                onGrant = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            )
        }
    }
}

private fun notificationsEnabled(context: Context) =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

@Composable
private fun PermissionRow(title: String, hint: String?, granted: Boolean, onGrant: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!granted && hint != null) {
                Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (granted) {
            Text(stringResource(R.string.granted), color = MaterialTheme.colorScheme.primary)
        } else {
            FilledTonalButton(onClick = onGrant) { Text(stringResource(R.string.grant)) }
        }
    }
}

// ---------- Сповіщення ----------

@Composable
private fun NotificationCard(settings: AppSettings, vm: SettingsViewModel) {
    val context = LocalContext.current
    // Локальний стан для полів вводу — щоб курсор не стрибав через асинхронний DataStore
    var label by remember { mutableStateOf(settings.label) }
    var pkg by remember { mutableStateOf(settings.targetPackage) }
    var minutes by remember { mutableFloatStateOf(settings.timerMinutes.toFloat()) }
    val notFound = pkg.isNotBlank() && context.packageManager.getLaunchIntentForPackage(pkg.trim()) == null

    SectionCard(stringResource(R.string.section_notification)) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it; vm.setLabel(it) },
            label = { Text(stringResource(R.string.label_text)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pkg,
            onValueChange = { pkg = it; vm.setTargetPackage(it.trim()) },
            label = { Text(stringResource(R.string.target_package)) },
            placeholder = { Text(stringResource(R.string.target_package_hint)) },
            singleLine = true,
            isError = notFound,
            supportingText = { if (notFound) Text(stringResource(R.string.target_not_found)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.timer_duration, minutes.roundToInt()))
        Slider(
            value = minutes,
            onValueChange = { minutes = it },
            onValueChangeFinished = { vm.setTimerMinutes(minutes.roundToInt()) },
            valueRange = 1f..15f,
            steps = 13
        )
    }
}

// ---------- Розклад ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleCard(settings: AppSettings, vm: SettingsViewModel) {
    var showPicker by remember { mutableStateOf(false) }
    val dayNames = listOf(
        stringResource(R.string.day_mon), stringResource(R.string.day_tue),
        stringResource(R.string.day_wed), stringResource(R.string.day_thu),
        stringResource(R.string.day_fri), stringResource(R.string.day_sat),
        stringResource(R.string.day_sun)
    )

    SectionCard(stringResource(R.string.section_schedule)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.schedule_enabled), Modifier.weight(1f))
            Switch(checked = settings.enabled, onCheckedChange = vm::setEnabled)
        }

        Text(stringResource(R.string.days_title), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dayNames.forEachIndexed { index, name ->
                FilterChip(
                    selected = settings.hasDay(index),
                    onClick = { vm.toggleDay(index) },
                    label = { Text(name) }
                )
            }
        }

        Text(stringResource(R.string.times_title), style = MaterialTheme.typography.labelLarge)
        settings.times.sorted().forEach { minute ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(minute), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.removeTime(minute) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_time))
                }
            }
        }
        OutlinedButton(onClick = { showPicker = true }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text(stringResource(R.string.add_time))
        }

        val next = AlarmScheduler.nextTriggerMillis(settings)
        Text(
            text = if (next == null) stringResource(R.string.no_next_alarm)
            else stringResource(
                R.string.next_alarm,
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
                    .format(Date(next))
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showPicker) {
        TimePickerDialog(
            onDismiss = { showPicker = false },
            onConfirm = { minute ->
                vm.addTime(minute)
                showPicker = false
            }
        )
    }
}

private fun formatTime(minuteOfDay: Int) = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.add_time),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(state = state)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

// ---------- Мова ----------

@Composable
private fun LanguageCard(settings: AppSettings, vm: SettingsViewModel) {
    val options = listOf(
        LANG_AUTO to stringResource(R.string.lang_auto),
        "uk" to stringResource(R.string.lang_uk),
        "en" to stringResource(R.string.lang_en),
        "de" to stringResource(R.string.lang_de)
    )
    SectionCard(stringResource(R.string.section_language)) {
        options.forEach { (code, name) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = settings.language == code,
                        onClick = { vm.setLanguage(code) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = settings.language == code, onClick = null)
                Text(name, Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp))
            }
        }
    }
}

// ---------- Приватність (Stealth) ----------

@Composable
private fun PrivacyCard() {
    val context = LocalContext.current
    var hidden by remember { mutableStateOf(StealthManager.isIconHidden(context)) }
    var confirm by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hidden = StealthManager.isIconHidden(context)
    }

    SectionCard(stringResource(R.string.section_privacy)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.hide_icon), Modifier.weight(1f))
            Switch(
                checked = hidden,
                onCheckedChange = { wantHidden ->
                    if (wantHidden) {
                        confirm = true
                    } else {
                        StealthManager.setIconHidden(context, false)
                        hidden = false
                    }
                }
            )
        }
        Text(
            stringResource(R.string.hide_icon_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.hide_dialog_title)) },
            text = { Text(stringResource(R.string.hide_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    StealthManager.setIconHidden(context, true)
                    hidden = true
                    confirm = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

// ---------- Тестування ----------

@Composable
private fun TestCard(settings: AppSettings) {
    val context = LocalContext.current
    SectionCard(stringResource(R.string.section_test)) {
        Button(
            onClick = { NotificationHelper.show(context, settings) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.test_now)) }
        OutlinedButton(
            onClick = {
                AlarmScheduler.scheduleTest(context, 10)
                Toast.makeText(context, R.string.test_scheduled, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.test_in_10s)) }
    }
}
