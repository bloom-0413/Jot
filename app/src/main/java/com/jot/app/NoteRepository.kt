package com.jot.app

import android.content.Context
import com.jot.app.behavior.Behavior
import com.jot.app.behavior.NoteSort
import com.jot.app.behavior.TrashAutoDelete
import org.json.JSONArray
import org.json.JSONObject

class NoteRepository(context: Context) {
    private val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)

    fun loadNotes(): List<Note> = loadList(KEY_NOTES, parseTrashedAt = false)

    fun loadArchivedNotes(): List<Note> = loadList(KEY_ARCHIVE, parseTrashedAt = false)

    /**
     * 加载回收站笔记。
     * 若启用了自动清理,会顺带删掉超过 30 天的项(写入 prefs 副作用)。
     */
    fun loadTrashedNotes(): List<Note> {
        val raw = prefs.getString(KEY_TRASH, "[]") ?: "[]"
        val array = JSONArray(raw)
        val cutoff = System.currentTimeMillis() - TRASH_EXPIRY_MS
        val autoDelete = Behavior.trashAutoDelete == TrashAutoDelete.ENABLED

        val remaining = JSONArray()
        val notes = mutableListOf<Note>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val trashedAt = obj.optLong("trashedAt", 0L)
            // 启用自动清理时跳过过期项
            if (autoDelete && trashedAt > 0L && trashedAt < cutoff) continue
            remaining.put(obj)
            notes.add(noteFromJson(obj, trashedAt))
        }
        // 如果有过期项被过滤掉,持久化剩余列表
        if (autoDelete && array.length() != remaining.length()) {
            prefs.edit().putString(KEY_TRASH, remaining.toString()).apply()
        }
        return sortNotes(notes)
    }

    fun addNote(note: Note) {
        val notes = loadNotes().toMutableList()
        notes.add(note)
        saveNotes(notes)
    }

    /**
     * 按 id 查找单条笔记,跳过排序与完整解析,适合编辑页/查看页取数据。
     */
    fun findNoteById(id: Long): Note? = findById(KEY_NOTES, id)

    fun findArchivedNoteById(id: Long): Note? = findById(KEY_ARCHIVE, id)

    fun upsertNote(note: Note) = upsert(KEY_NOTES, note, saveTrashedAt = false)

    fun upsertArchivedNote(note: Note) = upsert(KEY_ARCHIVE, note, saveTrashedAt = false)

    fun deleteNote(id: Long) = deleteById(KEY_NOTES, id)

    fun trashArchivedNote(id: Long) = moveById(KEY_ARCHIVE, KEY_TRASH, id, trashed = true)

    fun trashNote(id: Long) = moveById(KEY_NOTES, KEY_TRASH, id, trashed = true)

    fun permanentlyDeleteTrash(id: Long) = deleteById(KEY_TRASH, id)

    fun restoreFromTrash(id: Long) = moveById(KEY_TRASH, KEY_NOTES, id, trashed = false)

    fun clearAllTrash() {
        prefs.edit().putString(KEY_TRASH, "[]").apply()
    }

    fun archiveNote(id: Long) = moveById(KEY_NOTES, KEY_ARCHIVE, id, trashed = false)

    fun restoreFromArchive(id: Long) = moveById(KEY_ARCHIVE, KEY_NOTES, id, trashed = false)

    fun searchNotes(query: String): List<Note> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return loadNotes().filter {
            it.title.lowercase().contains(q) ||
            it.content.lowercase().contains(q)
        }
    }

    // ---- 通用私有方法 ----

    private fun loadList(key: String, parseTrashedAt: Boolean): List<Note> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(json)
        val notes = mutableListOf<Note>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            notes.add(
                if (parseTrashedAt) noteFromJson(obj, obj.optLong("trashedAt", 0L))
                else noteFromJson(obj, trashedAt = 0L)
            )
        }
        return sortNotes(notes)
    }

    private fun noteFromJson(obj: JSONObject, trashedAt: Long) = Note(
        id = obj.getLong("id"),
        title = obj.getString("title"),
        content = obj.getString("content"),
        createdAt = obj.getLong("createdAt"),
        updatedAt = obj.optLong("updatedAt", obj.getLong("createdAt")),
        trashedAt = trashedAt
    )

    private fun sortNotes(notes: List<Note>): List<Note> = when (Behavior.noteSort) {
        NoteSort.CREATED_AT -> notes.sortedByDescending { it.createdAt }
        NoteSort.UPDATED_AT -> notes.sortedByDescending { it.updatedAt }
    }

    private fun saveNotes(notes: List<Note>) = saveList(KEY_NOTES, notes, includeTrashedAt = false)

    private fun saveArchivedNotes(notes: List<Note>) = saveList(KEY_ARCHIVE, notes, includeTrashedAt = false)

    private fun saveTrashedNotes(notes: List<Note>) = saveList(KEY_TRASH, notes, includeTrashedAt = true)

    private fun saveList(key: String, notes: List<Note>, includeTrashedAt: Boolean) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("content", note.content)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                    if (includeTrashedAt) put("trashedAt", note.trashedAt)
                }
            )
        }
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun upsert(key: String, note: Note, saveTrashedAt: Boolean) {
        val notes = loadList(key, parseTrashedAt = saveTrashedAt).toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) notes[index] = note else notes.add(note)
        saveList(key, notes, includeTrashedAt = saveTrashedAt)
    }

    private fun deleteById(key: String, id: Long) {
        val notes = loadList(key, parseTrashedAt = key == KEY_TRASH).toMutableList()
        notes.removeAll { it.id == id }
        saveList(key, notes, includeTrashedAt = key == KEY_TRASH)
    }

    private fun findById(key: String, id: Long): Note? {
        val array = JSONArray(prefs.getString(key, "[]") ?: "[]")
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            if (obj.getLong("id") == id) {
                return noteFromJson(obj, obj.optLong("trashedAt", 0L))
            }
        }
        return null
    }

    /**
     * 把一条笔记从 [fromKey] 移到 [toKey]。
     * [trashed] = true 时,目标为回收站,需带上 trashedAt 时间戳。
     */
    private fun moveById(fromKey: String, toKey: String, id: Long, trashed: Boolean) {
        val fromNotes = loadList(fromKey, parseTrashedAt = fromKey == KEY_TRASH).toMutableList()
        val note = fromNotes.find { it.id == id } ?: return
        fromNotes.removeAll { it.id == id }
        saveList(fromKey, fromNotes, includeTrashedAt = fromKey == KEY_TRASH)

        val toNotes = loadList(toKey, parseTrashedAt = toKey == KEY_TRASH).toMutableList()
        toNotes.add(
            if (trashed) note.copy(trashedAt = System.currentTimeMillis()) else note
        )
        saveList(toKey, toNotes, includeTrashedAt = toKey == KEY_TRASH)
    }

    companion object {
        private const val KEY_NOTES = "notes"
        private const val KEY_ARCHIVE = "archive"
        private const val KEY_TRASH = "trash"
        private const val TRASH_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000

        /**
         * 启动时清理回收站过期项。与 loadTrashedNotes 内部清理逻辑一致。
         */
        fun cleanExpiredTrash(context: Context) {
            if (Behavior.trashAutoDelete != TrashAutoDelete.ENABLED) return
            NoteRepository(context).loadTrashedNotes()
        }
    }
}
