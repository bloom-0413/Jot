package com.jot.app

import android.content.Context
import com.jot.app.behavior.Behavior
import com.jot.app.behavior.NoteSort
import com.jot.app.behavior.TrashAutoDelete
import com.jot.app.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).noteDao()

    fun loadNotes(sort: NoteSort): Flow<List<Note>> = dao.loadNotes().map { sortNotes(it, sort) }

    fun loadArchivedNotes(sort: NoteSort): Flow<List<Note>> = dao.loadArchivedNotes().map { sortNotes(it, sort) }

    fun loadTrashedNotes(sort: NoteSort): Flow<List<Note>> = dao.loadTrashedNotes().map { sortNotes(it, sort) }

    suspend fun addNote(note: Note) = withContext(Dispatchers.IO) { dao.upsertNote(note) }

    suspend fun findNoteById(id: Long): Note? = withContext(Dispatchers.IO) { dao.findNoteById(id) }

    suspend fun upsertNote(note: Note) = withContext(Dispatchers.IO) { dao.upsertNote(note) }

    suspend fun upsertArchivedNote(note: Note) = withContext(Dispatchers.IO) {
        dao.upsertNote(note.copy(isArchived = true))
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) { dao.deleteNoteById(id) }

    suspend fun trashNote(id: Long) = withContext(Dispatchers.IO) {
        dao.trashNote(id, System.currentTimeMillis())
    }

    suspend fun restoreFromTrash(id: Long) = withContext(Dispatchers.IO) { dao.restoreFromTrash(id) }

    suspend fun clearAllTrash() = withContext(Dispatchers.IO) { dao.clearAllTrash() }

    suspend fun archiveNote(id: Long) = withContext(Dispatchers.IO) { dao.archiveNote(id) }

    suspend fun restoreFromArchive(id: Long) = withContext(Dispatchers.IO) { dao.restoreFromArchive(id) }

    suspend fun searchNotes(query: String, sort: NoteSort): List<Note> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        sortNotes(dao.searchNotes(query.lowercase()), sort)
    }

    private fun cleanupExpiredTrashInternal() {
        if (Behavior.trashAutoDelete != TrashAutoDelete.ENABLED) return
        dao.cleanExpiredTrash(System.currentTimeMillis() - TRASH_EXPIRY_MS)
    }

    private fun sortNotes(notes: List<Note>, sort: NoteSort): List<Note> = when (sort) {
        NoteSort.CREATED_AT -> notes.sortedByDescending { it.createdAt }
        NoteSort.UPDATED_AT -> notes.sortedByDescending { it.updatedAt }
    }

    companion object {
        private const val TRASH_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000

        suspend fun cleanExpiredTrash(context: Context) {
            if (Behavior.trashAutoDelete != TrashAutoDelete.ENABLED) return
            withContext(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(context).noteDao()
                dao.cleanExpiredTrash(System.currentTimeMillis() - TRASH_EXPIRY_MS)
            }
        }
    }
}