package com.jot.app.ui.theme

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
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
    dynamicColor: Boolean = false,
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

    val context = LocalContext.current

    Crossfade(
        targetState = darkTheme,
        animationSpec = tween(300),
        label = "themeCrossfade"
    ) { isDark ->
        val scheme = if (dynamicColor) {
            val base = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            base.copy(
                background = if (isDark) SageBackgroundDark else SageBackground,
                onBackground = if (isDark) SageOnBackgroundDark else SageOnBackground,
                surface = if (isDark) SageSurfaceDark else SageSurface,
                onSurface = if (isDark) SageOnSurfaceDark else SageOnSurface,
                surfaceVariant = if (isDark) SageSurfaceVariantDark else SageSurfaceVariant,
                onSurfaceVariant = if (isDark) SageOnSurfaceVariantDark else SageOnSurfaceVariant,
                outline = if (isDark) SageOutlineDark else SageOutline,
                outlineVariant = if (isDark) SagePlaceholderDark else SagePlaceholder
            )
        } else {
            if (isDark) SageDarkColorScheme else SageLightColorScheme
        }
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
