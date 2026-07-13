package com.jot.app.ui.theme

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SageDarkColorScheme = darkColorScheme(
    primary = SagePrimaryDark,
    onPrimary = SageBackgroundDark,
    primaryContainer = SagePrimaryContainerDark,
    onPrimaryContainer = SageOnBackgroundDark,
    secondary = SagePrimaryDark,
    background = SageBackgroundDark,
    onBackground = SageOnBackgroundDark,
    surface = SageSurfaceDark,
    onSurface = SageOnSurfaceDark,
    surfaceVariant = SageSurfaceVariantDark,
    onSurfaceVariant = SageOnSurfaceVariantDark,
    outline = SageOutlineDark,
    outlineVariant = SagePlaceholderDark
)

private val SageLightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageSurface,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnBackground,
    secondary = SagePrimary,
    background = SageBackground,
    onBackground = SageOnBackground,
    surface = SageSurface,
    onSurface = SageOnSurface,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = SageOnSurfaceVariant,
    outline = SageOutline,
    outlineVariant = SagePlaceholder
)

@Composable
fun JotTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // 根据应用实际主题强制设置状态栏图标配色
    // 避免应用主题与系统主题不一致时(如系统深色+应用浅色)状态栏图标不可见
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val colorScheme = if (darkTheme) SageDarkColorScheme else SageLightColorScheme

    Crossfade(
        targetState = colorScheme,
        animationSpec = tween(300),
        label = "themeCrossfade"
    ) { scheme ->
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
