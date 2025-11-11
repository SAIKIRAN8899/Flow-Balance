package com.easy.flowbalance.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DeepTeal80,
    onPrimary = DeepTeal40,
    primaryContainer = Fog40,
    onPrimaryContainer = DeepTeal80,
    secondary = Golden80,
    onSecondary = DeepTeal40,
    tertiary = Clay80,
    onTertiary = Color.Black,
    background = Color(0xFF0E1A20),
    surface = Color(0xFF13242B),
    surfaceVariant = Color(0xFF1F323A),
    onSurface = Color(0xFFE4EEF2),
    onSurfaceVariant = Fog80
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal40,
    onPrimary = Color.White,
    primaryContainer = DeepTeal80,
    onPrimaryContainer = Color(0xFF05232E),
    secondary = Golden40,
    onSecondary = Color.White,
    tertiary = Clay40,
    onTertiary = Color.White,
    background = Color(0xFFF6F7F8),
    surface = Color(0xFFFBFCFD),
    surfaceVariant = Color(0xFFE5EBEF),
    onSurface = Color(0xFF1E2A35),
    onSurfaceVariant = Color(0xFF4B5763)
)

@Composable
fun FlowBalanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}