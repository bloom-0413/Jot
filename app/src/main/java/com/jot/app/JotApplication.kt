package com.jot.app

import android.app.Application
import com.jot.app.behavior.AutoUpdate
import com.jot.app.behavior.Behavior
import com.jot.app.ui.theme.ThemePreferences

class JotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePreferences.init(this)
        Behavior.init(this)
        NoteRepository.cleanExpiredTrash(this)
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        if (Behavior.autoUpdate == AutoUpdate.ENABLED) {
            UpdateChecker.check(this)
        }
    }
}
