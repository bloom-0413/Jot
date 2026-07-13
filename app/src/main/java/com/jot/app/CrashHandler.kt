package com.jot.app

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.jot.app.behavior.Behavior
import com.jot.app.behavior.CrashLog
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (Behavior.crashLog == CrashLog.ENABLED) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val fileName = "crash_${sdf.format(Date())}.log"
                val content = buildReport(throwable)

                // 用 MediaStore 写入公共 Downloads 目录
                // 文件出现在 /Download/Jot/crashes/ 下,系统文件管理器可见
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Jot/crashes")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    resolver.openOutputStream(it)?.use { os ->
                        os.write(content.toByteArray())
                    }
                }
            } catch (_: Exception) {
            }
        }

        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun buildReport(throwable: Throwable): String {
        val writer = StringWriter()
        PrintWriter(writer).use { pw ->
            pw.println("=== Jot Crash Report ===")
            pw.println("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            pw.println("Device: ${android.os.Build.MODEL}")
            pw.println("Android: ${android.os.Build.VERSION.SDK_INT}")
            pw.println()
            throwable.printStackTrace(pw)
        }
        return writer.toString()
    }
}
