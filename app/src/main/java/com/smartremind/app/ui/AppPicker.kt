@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartremind.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.smartremind.app.R
import com.smartremind.app.util.AppInfo
import com.smartremind.app.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Іконка застосунку за package name (вантажиться у фоновому потоці). */
@Composable
fun AppIcon(packageName: String, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(null, packageName) {
        value = withContext(Dispatchers.Default) {
            runCatching {
                context.packageManager.getApplicationIcon(packageName)
                    .toBitmap(128, 128)
                    .asImageBitmap()
            }.getOrNull()
        }
    }
    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier.size(size))
    } else {
        Spacer(modifier.size(size))
    }
}

/**
 * Діалог вибору застосунку: список усіх встановлених програм із пошуком
 * і режим ручного введення package name (якщо потрібного застосунку в списку немає).
 */
@Composable
fun AppPickerDialog(
    currentPackage: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var manual by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf(currentPackage) }
    val apps by produceState<List<AppInfo>?>(null) {
        value = withContext(Dispatchers.Default) { AppUtils.loadLaunchableApps(context) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(16.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    stringResource(if (manual) R.string.package_name else R.string.pick_app_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp)
                )

                if (manual) {
                    val label = AppUtils.appLabel(context, manualText.trim())
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = manualText,
                            onValueChange = { manualText = it },
                            label = { Text(stringResource(R.string.package_name)) },
                            placeholder = { Text(stringResource(R.string.package_hint)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                            isError = manualText.isNotBlank() && label == null,
                            supportingText = {
                                if (manualText.isNotBlank()) {
                                    Text(
                                        if (label != null) stringResource(R.string.package_found, label)
                                        else stringResource(R.string.package_not_found_saveable)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(R.string.search_apps)) },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )

                    val list = apps
                    if (list == null) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val filtered = list.filter {
                            query.isBlank() ||
                                it.label.contains(query, ignoreCase = true) ||
                                it.packageName.contains(query, ignoreCase = true)
                        }
                        LazyColumn(Modifier.weight(1f)) {
                            items(filtered, key = { it.packageName }) { app ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(app.packageName) }
                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AppIcon(app.packageName, 40.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            app.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            app.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    RadioButton(selected = app.packageName == currentPackage, onClick = null)
                                }
                            }
                        }
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { manual = !manual }) {
                        Text(stringResource(if (manual) R.string.pick_back else R.string.pick_manual))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    if (manual) {
                        Button(
                            onClick = { onSelect(manualText.trim()) },
                            enabled = manualText.isNotBlank()
                        ) { Text(stringResource(R.string.save)) }
                    }
                }
            }
        }
    }
}
