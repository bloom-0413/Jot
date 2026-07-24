package com.jot.app

import android.content.Context
import com.jot.app.behavior.Behavior
import com.jot.app.db.AppDatabase
import com.jot.app.db.parseNotesFromJson
import com.jot.app.db.serializeNotesToJson
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private const val BEHAVIOR_PREFS = "behavior_prefs"
    private const val NOTES_JSON = "notes.json"
    private const val BEHAVIOR_JSON = "behavior.json"

    fun exportBackup(context: Context, outputStream: OutputStream) {
        val dao = AppDatabase.getInstance(context).noteDao()
        val behaviorPrefs = context.getSharedPreferences(BEHAVIOR_PREFS, Context.MODE_PRIVATE)

        ZipOutputStream(outputStream.buffered()).use { zos ->
            zos.putNextEntry(ZipEntry(NOTES_JSON))
            zos.write(serializeNotesToJson(dao.getAllNotes()).toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry(BEHAVIOR_JSON))
            val behavior = JSONObject()
            behaviorPrefs.all.forEach { (key, value) ->
                behavior.put(key, value.toString())
            }
            zos.write(behavior.toString().toByteArray())
            zos.closeEntry()
        }
    }

    fun importBackup(context: Context, inputStream: InputStream) {
        val dao = AppDatabase.getInstance(context).noteDao()

        val data = mutableMapOf<String, String>()
        ZipInputStream(inputStream.buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name.endsWith(".json")) {
                    data[name] = zis.readBytes().toString(Charsets.UTF_8)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val notesJson = data.remove(NOTES_JSON) ?: throw IllegalArgumentException("Missing notes.json")
        val behaviorJson = data.remove(BEHAVIOR_JSON)

        val existingIds = dao.getAllNotes().map { it.id }.toSet()
        parseNotesFromJson(notesJson, parseTrashedAt = true)
            .filter { it.id !in existingIds }
            .forEach { dao.upsertNote(it) }

        if (behaviorJson != null) {
            val behaviorPrefs = context.getSharedPreferences(BEHAVIOR_PREFS, Context.MODE_PRIVATE)
            val behavior = JSONObject(behaviorJson)
            behavior.keys().forEach { key ->
                behaviorPrefs.edit().putString(key, behavior.getString(key)).apply()
            }
            Behavior.init(context, behaviorPrefs)
        }
    }
}