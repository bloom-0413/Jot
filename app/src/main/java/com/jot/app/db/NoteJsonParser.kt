package com.jot.app.db

import com.jot.app.Note
import org.json.JSONArray

fun parseNotesFromJson(json: String, parseTrashedAt: Boolean = false, isArchived: Boolean = false): List<Note> {
    val array = JSONArray(json)
    val notes = mutableListOf<Note>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        notes.add(
            Note(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                content = obj.getString("content"),
                createdAt = obj.getLong("createdAt"),
                updatedAt = obj.optLong("updatedAt", obj.getLong("createdAt")),
                trashedAt = if (parseTrashedAt) obj.optLong("trashedAt", 0L) else 0L,
                isArchived = isArchived
            )
        )
    }
    return notes
}

fun serializeNotesToJson(notes: List<Note>): String {
    val array = JSONArray()
    notes.forEach { note ->
        array.put(
            org.json.JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
                put("trashedAt", note.trashedAt)
                put("isArchived", note.isArchived)
            }
        )
    }
    return array.toString()
}