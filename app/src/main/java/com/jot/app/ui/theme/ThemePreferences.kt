package com.jot.app.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.jot.app.R

enum class ThemeMode(@StringRes val labelRes: Int, val iconRes: Int) {
    SYSTEM(R.string.theme_system, R.drawable.ic_brightness_auto),
    LIGHT(R.string.theme_light, R.drawable.ic_brightness_light),
    DARK(R.string.theme_dark, R.drawable.ic_brightness_dark)
}

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_MODE = "theme_mode"
    private const val KEY_DYNAMIC_COLOR = "dynamic_color"

    private lateinit var modeState: MutableState<ThemeMode>
    private lateinit var dynamicColorState: MutableState<Boolean>

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        modeState = mutableStateOf(ThemeMode.valueOf(savedName))
        dynamicColorState = mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_COLOR, false))
    }

    fun setMode(context: Context, mode: ThemeMode) {
        modeState.value = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }

    fun setDynamicColorEnabled(context: Context, enabled: Boolean) {
        dynamicColorState.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DYNAMIC_COLOR, enabled)
            .apply()
    }

    /**
     * 在 Composable 中调用,返回当前主题模式并订阅变化。
     */
    @Composable
    fun currentMode(): ThemeMode = modeState.value

    @Composable
    fun isDynamicColorEnabled(): Boolean = dynamicColorState.value
}
