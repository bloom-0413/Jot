package com.jot.app

import android.content.Context
import com.jot.app.behavior.Behavior
import com.jot.app.behavior.NoteSort
import com.jot.app.behavior.TrashAutoDelete
import com.jot.app.db.AppDatabase

class NoteRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).noteDao()

    fun loadNotes(sort: NoteSort): List<Note> = sortNotes(dao.loadNotes(), sort)

    fun loadArchivedNotes(sort: NoteSort): List<Note> = sortNotes(dao.loadArchivedNotes(), sort)

    fun loadTrashedNotes(sort: NoteSort): List<Note> {
        maybeCleanExpiredTrash()
        return sortNotes(dao.loadTrashedNotes(), sort)
    }

    fun addNote(note: Note) = dao.upsertNote(note)

    fun findNoteById(id: Long): Note? = dao.findNoteById(id)

    fun upsertNote(note: Note) = dao.upsertNote(note)

    fun upsertArchivedNote(note: Note) = dao.upsertNote(note.copy(isArchived = true))

    fun deleteNote(id: Long) = dao.deleteNoteById(id)

    fun trashNote(id: Long) = dao.trashNote(id, System.currentTimeMillis())

    fun restoreFromTrash(id: Long) = dao.restoreFromTrash(id)

    fun clearAllTrash() = dao.clearAllTrash()

    fun archiveNote(id: Long) = dao.archiveNote(id)

    fun restoreFromArchive(id: Long) = dao.restoreFromArchive(id)

    fun searchNotes(query: String, sort: NoteSort): List<Note> {
        if (query.isBlank()) return emptyList()
        return sortNotes(dao.searchNotes(query.lowercase()), sort)
    }

    private fun maybeCleanExpiredTrash() {
        if (Behavior.trashAutoDelete != TrashAutoDelete.ENABLED) return
        val cutoff = System.currentTimeMillis() - TRASH_EXPIRY_MS
        dao.cleanExpiredTrash(cutoff)
    }

    private fun sortNotes(notes: List<Note>, sort: NoteSort): List<Note> = when (sort) {
        NoteSort.CREATED_AT -> notes.sortedByDescending { it.createdAt }
        NoteSort.UPDATED_AT -> notes.sortedByDescending { it.updatedAt }
    }

    companion object {
        private const val TRASH_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000

        fun cleanExpiredTrash(context: Context) {
            if (Behavior.trashAutoDelete != TrashAutoDelete.ENABLED) return
            val dao = AppDatabase.getInstance(context).noteDao()
            val cutoff = System.currentTimeMillis() - TRASH_EXPIRY_MS
            dao.cleanExpiredTrash(cutoff)
        }
    }
}