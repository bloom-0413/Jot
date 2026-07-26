package com.jot.app

import android.app.Application
import com.jot.app.behavior.AutoUpdate
import com.jot.app.behavior.Behavior
import com.jot.app.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JotApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        ThemePreferences.init(this)
        Behavior.init(this)
        applicationScope.launch { NoteRepository.cleanExpiredTrash(this@JotApplication) }
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        if (Behavior.autoUpdate == AutoUpdate.ENABLED) {
            UpdateChecker.check(this, applicationScope)
        }
    }
}
