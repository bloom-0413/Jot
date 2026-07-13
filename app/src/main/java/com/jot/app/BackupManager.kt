package com.jot.app

import android.content.Context
import com.jot.app.behavior.Behavior
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private const val NOTES_PREFS = "notes_prefs"
    private const val BEHAVIOR_PREFS = "behavior_prefs"
    private const val KEY_NOTES = "notes"
    private const val KEY_ARCHIVE = "archive"
    private const val KEY_TRASH = "trash"

    fun exportBackup(context: Context, outputStream: OutputStream) {
        val notesPrefs = context.getSharedPreferences(NOTES_PREFS, Context.MODE_PRIVATE)
        val behaviorPrefs = context.getSharedPreferences(BEHAVIOR_PREFS, Context.MODE_PRIVATE)

        ZipOutputStream(outputStream.buffered()).use { zos ->
            zos.putNextEntry(ZipEntry("$KEY_NOTES.json"))
            zos.write(notesPrefs.getString(KEY_NOTES, "[]")!!.toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("$KEY_ARCHIVE.json"))
            zos.write(notesPrefs.getString(KEY_ARCHIVE, "[]")!!.toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("$KEY_TRASH.json"))
            zos.write(notesPrefs.getString(KEY_TRASH, "[]")!!.toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("behavior.json"))
            val behavior = JSONObject()
            behaviorPrefs.all.forEach { (key, value) ->
                behavior.put(key, value.toString())
            }
            zos.write(behavior.toString().toByteArray())
            zos.closeEntry()
        }
    }

    fun importBackup(context: Context, inputStream: InputStream) {
        val notesPrefs = context.getSharedPreferences(NOTES_PREFS, Context.MODE_PRIVATE)

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

        val notesJson = data.remove("notes.json") ?: throw IllegalArgumentException("Missing notes.json")
        val archiveJson = data.remove("archive.json") ?: throw IllegalArgumentException("Missing archive.json")
        val trashJson = data.remove("trash.json") ?: throw IllegalArgumentException("Missing trash.json")
        val behaviorJson = data.remove("behavior.json")

        mergeJsonArray(notesPrefs, KEY_NOTES, notesJson)
        mergeJsonArray(notesPrefs, KEY_ARCHIVE, archiveJson)
        mergeJsonArray(notesPrefs, KEY_TRASH, trashJson)

        if (behaviorJson != null) {
            val behaviorPrefs = context.getSharedPreferences(BEHAVIOR_PREFS, Context.MODE_PRIVATE)
            val behavior = JSONObject(behaviorJson)
            behavior.keys().forEach { key ->
                behaviorPrefs.edit().putString(key, behavior.getString(key)).apply()
            }
            Behavior.init(context, behaviorPrefs)
        }
    }

    private fun mergeJsonArray(prefs: android.content.SharedPreferences, key: String, backupJson: String) {
        val localArray = JSONArray(prefs.getString(key, "[]") ?: "[]")
        val backupArray = JSONArray(backupJson)

        val localIds = mutableSetOf<Long>()
        for (i in 0 until localArray.length()) {
            localIds.add(localArray.getJSONObject(i).getLong("id"))
        }

        for (i in 0 until backupArray.length()) {
            val obj = backupArray.getJSONObject(i)
            if (obj.getLong("id") !in localIds) {
                localArray.put(obj)
            }
        }

        prefs.edit().putString(key, localArray.toString()).apply()
    }
}
