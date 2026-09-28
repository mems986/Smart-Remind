package com.smartremind.app.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Яскрава вбудована палітра: фіолетовий + бірюзовий + рожевий
private val VividLight = lightColorScheme(
    primary = Color(0xFF6A4FE0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5DEFF),
    onPrimaryContainer = Color(0xFF1D0060),
    secondary = Color(0xFF00897B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFAEF0E5),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFFD81B60),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E4),
    onTertiaryContainer = Color(0xFF3E001D),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EB),
    onSurfaceVariant = Color(0xFF49454E)
)

private val VividDark = darkColorScheme(
    primary = Color(0xFFCAC0FF),
    onPrimary = Color(0xFF300F9C),
    primaryContainer = Color(0xFF4A32C0),
    onPrimaryContainer = Color(0xFFE5DEFF),
    secondary = Color(0xFF66D9C8),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Color(0xFFAEF0E5),
    tertiary = Color(0xFFFFB0CA),
    onTertiary = Color(0xFF650033),
    tertiaryContainer = Color(0xFF8E0047),
    onTertiaryContainer = Color(0xFFFFD9E4),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E6),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E6),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4CF)
)

private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

/** Яскрава Material 3 тема: насичені кольори (динамічні або власна палітра) і заокруглені форми. */
@Composable
fun SmartRemindTheme(dynamic: Boolean = true, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme: ColorScheme = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> VividDark
        else -> VividLight
    }
    MaterialTheme(
        colorScheme = scheme,
        shapes = ExpressiveShapes,
        content = content
    )
}
