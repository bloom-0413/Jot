package com.jot.app.behavior

import android.content.Context
import android.content.SharedPreferences

object Behavior {
    private const val PREFS_NAME = "behavior_prefs"
    private const val KEY_NEW_NOTE_KEYBOARD = "new_note_keyboard"
    private const val KEY_CRASH_LOG = "crash_log"
    private const val KEY_NOTE_SORT = "note_sort"
    private const val KEY_TRASH_AUTO_DELETE = "trash_auto_delete"
    private const val KEY_AUTO_UPDATE = "auto_update"

    private lateinit var prefs: SharedPreferences

    var newNoteKeyboard = NewNoteKeyboard.ENABLED
    var crashLog = CrashLog.DISABLED
    var noteSort = NoteSort.UPDATED_AT
    var trashAutoDelete = TrashAutoDelete.ENABLED
    var autoUpdate = AutoUpdate.ENABLED

    fun init(context: Context, overridePrefs: SharedPreferences? = null) {
        prefs = overridePrefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        newNoteKeyboard = enumValueOf<NewNoteKeyboard>(
            prefs.getString(KEY_NEW_NOTE_KEYBOARD, NewNoteKeyboard.ENABLED.name)!!
        )
        crashLog = enumValueOf<CrashLog>(
            prefs.getString(KEY_CRASH_LOG, CrashLog.DISABLED.name)!!
        )
        noteSort = enumValueOf<NoteSort>(
            prefs.getString(KEY_NOTE_SORT, NoteSort.UPDATED_AT.name)!!
        )
        trashAutoDelete = enumValueOf<TrashAutoDelete>(
            prefs.getString(KEY_TRASH_AUTO_DELETE, TrashAutoDelete.ENABLED.name)!!
        )
        autoUpdate = enumValueOf<AutoUpdate>(
            prefs.getString(KEY_AUTO_UPDATE, AutoUpdate.ENABLED.name)!!
        )
    }

    fun updateNewNoteKeyboard(value: NewNoteKeyboard) {
        newNoteKeyboard = value
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_NEW_NOTE_KEYBOARD, value.name).apply()
        }
    }

    fun updateCrashLog(value: CrashLog) {
        crashLog = value
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_CRASH_LOG, value.name).apply()
        }
    }

    fun updateNoteSort(value: NoteSort) {
        noteSort = value
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_NOTE_SORT, value.name).apply()
        }
    }

    fun updateTrashAutoDelete(value: TrashAutoDelete) {
        trashAutoDelete = value
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_TRASH_AUTO_DELETE, value.name).apply()
        }
    }

    fun updateAutoUpdate(value: AutoUpdate) {
        autoUpdate = value
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_AUTO_UPDATE, value.name).apply()
        }
    }
}
