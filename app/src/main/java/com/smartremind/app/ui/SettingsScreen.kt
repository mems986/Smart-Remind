@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartremind.app.R
import com.smartremind.app.data.AppSettings
import com.smartremind.app.data.LANG_AUTO
import com.smartremind.app.data.PILL_LABEL
import com.smartremind.app.data.PILL_TIMER
import com.smartremind.app.scheduler.AlarmScheduler
import com.smartremind.app.service.CountdownService
import com.smartremind.app.util.AppUtils
import com.smartremind.app.util.StealthManager
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val cs = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = cs.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.title_settings), fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.background,
                    scrolledContainerColor = cs.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val current = settings
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeroCard(current)
                PermissionsCard()
                NotificationCard(current, vm)
                ScheduleCard(current, vm)
                AppearanceCard(current, vm)
                PrivacyCard()
                TestCard()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ---------- Загальні елементи ----------

/** Макет «піли» у статус-барі: іконка + текст, як чіп Live Update поруч із годинником. */
@Composable
private fun StatusPill(text: String) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notification),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun HeroCard(settings: AppSettings) {
    val cs = MaterialTheme.colorScheme
    val next = AlarmScheduler.nextTriggerMillis(settings)
    val nextText = if (next == null) {
        stringResource(R.string.no_next_alarm)
    } else {
        stringResource(
            R.string.next_alarm,
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
                .format(Date(next))
        )
    }
    val title = settings.label.ifBlank { stringResource(R.string.default_label) }
    val pillText = if (settings.pillContent == PILL_LABEL) title.take(7) else "%d:00".format(settings.timerMinutes)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = cs.primary, contentColor = cs.onPrimary)
    ) {
        Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusPill(pillText)
            Text(
                title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )
            LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = cs.onPrimary,
                trackColor = cs.onPrimary.copy(alpha = 0.25f)
            )
            Text(nextText, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    container: Color,
    badge: Color,
    onBadge: Color,
    badgeShape: Shape,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(shape = badgeShape, color = badge, modifier = Modifier.size(54.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = onBadge)
                    }
                }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
            content()
        }
    }
}

// ---------- Дозволи ----------

@Composable
private fun PermissionsCard() {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
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

    val allOk = notifOk && exactOk
    SectionCard(
        title = stringResource(R.string.section_permissions),
        icon = if (allOk) Icons.Default.CheckCircle else Icons.Default.Warning,
        container = if (allOk) cs.secondaryContainer else cs.errorContainer,
        badge = if (allOk) cs.secondary else cs.error,
        onBadge = if (allOk) cs.onSecondary else cs.onError,
        badgeShape = MaterialShapes.Cookie12Sided.toShape()
    ) {
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
        if (Build.VERSION.SDK_INT >= 36) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.perm_live_updates), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.perm_live_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = { openLiveUpdatesSettings(context) }) {
                    Text(stringResource(R.string.open_settings))
                }
            }
        }
    }
}

private fun notificationsEnabled(context: Context) =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

private fun openLiveUpdatesSettings(context: Context) {
    val pkg = context.packageName
    val promo = Intent("android.settings.APP_NOTIFICATION_PROMOTION_SETTINGS")
        .putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
    val fallback = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
    try {
        context.startActivity(promo)
    } catch (e: Exception) {
        context.startActivity(fallback)
    }
}

@Composable
private fun PermissionRow(title: String, hint: String?, granted: Boolean, onGrant: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!granted && hint != null) {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (granted) {
            Text(
                stringResource(R.string.granted),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            FilledTonalButton(onClick = onGrant) { Text(stringResource(R.string.grant)) }
        }
    }
}

// ---------- Live-віджет ----------

@Composable
private fun NotificationCard(settings: AppSettings, vm: SettingsViewModel) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    // Локальний стан, щоб курсор у полі не стрибав через асинхронний DataStore
    var label by remember { mutableStateOf(settings.label) }
    var minutes by remember { mutableFloatStateOf(settings.timerMinutes.toFloat()) }
    var showPicker by remember { mutableStateOf(false) }

    val hasTarget = settings.targetPackage.isNotBlank()
    val appName = remember(settings.targetPackage) { AppUtils.appLabel(context, settings.targetPackage) }
    val notFound = hasTarget && appName == null

    SectionCard(
        title = stringResource(R.string.section_notification),
        icon = Icons.Default.Notifications,
        container = cs.primaryContainer,
        badge = cs.primary,
        onBadge = cs.onPrimary,
        badgeShape = MaterialShapes.Cookie9Sided.toShape()
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it; vm.setLabel(it) },
            label = { Text(stringResource(R.string.label_text)) },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        )

        // Вибір застосунку для кнопки «Відкрити»: зі списку або вручну
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(cs.surface)
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (appName != null) {
                AppIcon(settings.targetPackage, 44.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.target_app),
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant
                )
                Text(
                    text = appName
                        ?: if (hasTarget) settings.targetPackage else stringResource(R.string.target_app_none),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (notFound) cs.error else cs.onSurface
                )
                if (notFound) {
                    Text(
                        stringResource(R.string.target_not_found),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.error
                    )
                }
            }
            if (hasTarget) {
                IconButton(onClick = { vm.setTargetPackage("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_app))
                }
            }
            FilledTonalButton(onClick = { showPicker = true }) {
                Text(stringResource(if (hasTarget) R.string.change_app else R.string.choose_app))
            }
        }

        Text(stringResource(R.string.pill_title), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = settings.pillContent == PILL_TIMER,
                onClick = { vm.setPillContent(PILL_TIMER) },
                label = { Text(stringResource(R.string.pill_timer)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = cs.primary,
                    selectedLabelColor = cs.onPrimary
                )
            )
            FilterChip(
                selected = settings.pillContent == PILL_LABEL,
                onClick = { vm.setPillContent(PILL_LABEL) },
                label = { Text(stringResource(R.string.pill_label)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = cs.primary,
                    selectedLabelColor = cs.onPrimary
                )
            )
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.alert_on_start), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.alert_on_start_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            Switch(checked = settings.alertOnStart, onCheckedChange = vm::setAlertOnStart)
        }

        Text(stringResource(R.string.timer_duration, minutes.roundToInt()), style = MaterialTheme.typography.titleMedium)
        Slider(
            value = minutes,
            onValueChange = { minutes = it },
            onValueChangeFinished = { vm.setTimerMinutes(minutes.roundToInt()) },
            valueRange = 1f..15f,
            steps = 13
        )
    }

    if (showPicker) {
        AppPickerDialog(
            currentPackage = settings.targetPackage,
            onDismiss = { showPicker = false },
            onSelect = { pkg ->
                vm.setTargetPackage(pkg)
                showPicker = false
            }
        )
    }
}

// ---------- Розклад ----------

@Composable
private fun ScheduleCard(settings: AppSettings, vm: SettingsViewModel) {
    val cs = MaterialTheme.colorScheme
    var showPicker by remember { mutableStateOf(false) }
    val cookie = MaterialShapes.Cookie9Sided.toShape()
    val dayNames = listOf(
        stringResource(R.string.day_mon), stringResource(R.string.day_tue),
        stringResource(R.string.day_wed), stringResource(R.string.day_thu),
        stringResource(R.string.day_fri), stringResource(R.string.day_sat),
        stringResource(R.string.day_sun)
    )

    SectionCard(
        title = stringResource(R.string.section_schedule),
        icon = Icons.Default.DateRange,
        container = cs.tertiaryContainer,
        badge = cs.tertiary,
        onBadge = cs.onTertiary,
        badgeShape = MaterialShapes.Clover4Leaf.toShape()
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.schedule_enabled),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = settings.enabled, onCheckedChange = vm::setEnabled)
        }

        Text(stringResource(R.string.days_title), style = MaterialTheme.typography.labelLarge)
        // Дні тижня: вибраний день перетворюється на «печиво» (cookie), як у Material 3 Expressive
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            dayNames.forEachIndexed { index, name ->
                val selected = settings.hasDay(index)
                Box(
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(if (selected) cookie else CircleShape)
                        .background(if (selected) cs.tertiary else cs.surface)
                        .clickable(role = Role.Checkbox) { vm.toggleDay(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        color = if (selected) cs.onTertiary else cs.onSurface
                    )
                }
            }
        }

        Text(stringResource(R.string.times_title), style = MaterialTheme.typography.labelLarge)
        settings.times.sorted().forEach { minute ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(cs.surface)
                    .padding(start = 24.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatTime(minute),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.removeTime(minute) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_time))
                }
            }
        }
        OutlinedButton(
            onClick = { showPicker = true },
            shape = CircleShape,
            modifier = Modifier.height(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.add_time))
        }
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

@Composable
private fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.add_time),
                    style = MaterialTheme.typography.titleLarge,
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

// ---------- Вигляд: мова та кольори ----------

@Composable
private fun AppearanceCard(settings: AppSettings, vm: SettingsViewModel) {
    val cs = MaterialTheme.colorScheme
    val options = listOf(
        LANG_AUTO to stringResource(R.string.lang_auto),
        "uk" to stringResource(R.string.lang_uk),
        "en" to stringResource(R.string.lang_en),
        "de" to stringResource(R.string.lang_de)
    )
    SectionCard(
        title = stringResource(R.string.section_appearance),
        icon = Icons.Default.Settings,
        container = cs.secondaryContainer,
        badge = cs.secondary,
        onBadge = cs.onSecondary,
        badgeShape = MaterialShapes.SoftBurst.toShape()
    ) {
        Text(stringResource(R.string.language_title), style = MaterialTheme.typography.labelLarge)
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
        if (Build.VERSION.SDK_INT >= 31) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.dynamic_colors), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.dynamic_colors_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
                Switch(checked = settings.dynamicColor, onCheckedChange = vm::setDynamicColor)
            }
        }
    }
}

// ---------- Приватність (Stealth) ----------

@Composable
private fun PrivacyCard() {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var hidden by remember { mutableStateOf(StealthManager.isIconHidden(context)) }
    var confirm by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hidden = StealthManager.isIconHidden(context)
    }

    SectionCard(
        title = stringResource(R.string.section_privacy),
        icon = Icons.Default.Lock,
        container = cs.surfaceContainerHigh,
        badge = cs.inverseSurface,
        onBadge = cs.inverseOnSurface,
        badgeShape = MaterialShapes.Cookie6Sided.toShape()
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.hide_icon),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
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
            color = cs.onSurfaceVariant
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
private fun TestCard() {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    SectionCard(
        title = stringResource(R.string.section_test),
        icon = Icons.Default.PlayArrow,
        container = cs.surfaceContainerHigh,
        badge = cs.primary,
        onBadge = cs.onPrimary,
        badgeShape = MaterialShapes.Cookie12Sided.toShape()
    ) {
        Button(
            onClick = { CountdownService.start(context) },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(stringResource(R.string.test_now)) }
        OutlinedButton(
            onClick = {
                AlarmScheduler.scheduleTest(context, 10)
                Toast.makeText(context, R.string.test_scheduled, Toast.LENGTH_SHORT).show()
            },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(stringResource(R.string.test_in_10s)) }
    }
}
