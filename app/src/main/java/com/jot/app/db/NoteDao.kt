package com.jot.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jot.app.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE trashedAt = 0 AND isArchived = 0")
    fun loadNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND trashedAt = 0")
    fun loadArchivedNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE trashedAt > 0")
    fun loadTrashedNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun findNoteById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteNoteById(id: Long)

    @Query("UPDATE notes SET isArchived = 1, trashedAt = 0 WHERE id = :id")
    fun archiveNote(id: Long)

    @Query("UPDATE notes SET isArchived = 0 WHERE id = :id")
    fun restoreFromArchive(id: Long)

    @Query("UPDATE notes SET trashedAt = :trashedAt, isArchived = 0 WHERE id = :id")
    fun trashNote(id: Long, trashedAt: Long)

    @Query("UPDATE notes SET trashedAt = 0 WHERE id = :id")
    fun restoreFromTrash(id: Long)

    @Query("DELETE FROM notes WHERE trashedAt > 0 AND trashedAt < :cutoff")
    fun cleanExpiredTrash(cutoff: Long)

    @Query("SELECT * FROM notes WHERE trashedAt = 0 AND isArchived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun searchNotes(query: String): List<Note>

    @Query("DELETE FROM notes WHERE trashedAt > 0")
    fun clearAllTrash()

    @Query("SELECT * FROM notes")
    fun getAllNotes(): List<Note>
}