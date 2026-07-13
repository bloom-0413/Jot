package com.jot.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Release 更新检测器。
 *
 * 通过 GitHub 公开 API 查询最新 release，与当前应用版本号比对，
 * 发现新版本时将结果写入 [updateInfo]，由 UI 层观察并展示更新提示弹窗。
 */
object UpdateChecker {
    private const val REPO = "bloom-0413/Jot"
    private const val API_URL = "https://api.github.com/repos/$REPO/releases/latest"
    private const val RELEASE_PAGE = "https://github.com/$REPO/releases/latest"
    private const val PREFS_NAME = "update_prefs"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"

    /** 当前检测到的新版本信息；null 表示无更新或尚未检测完成。 */
    var updateInfo: UpdateInfo? by mutableStateOf(null)
        private set

    /** 启动时自动检查（忽略已忽略版本），结果写入 [updateInfo]。 */
    fun check(context: Context) {
        Thread {
            val info = fetchLatestRelease(context)
            if (info != null) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (info.newVersion == prefs.getString(KEY_DISMISSED_VERSION, null)) return@Thread
                Handler(Looper.getMainLooper()).post { updateInfo = info }
            }
        }.start()
    }

    /** 手动检查更新（忽略忽略列表），通过 [onResult] 返回结果。 */
    fun checkForUpdates(context: Context, onResult: (UpdateInfo?) -> Unit) {
        Thread {
            val info = fetchLatestRelease(context)
            Handler(Looper.getMainLooper()).post { onResult(info) }
        }.start()
    }

    /** 关闭当前更新提示，并记住该版本号，后续启动不再弹窗提醒。 */
    fun dismiss(context: Context) {
        val version = updateInfo?.newVersion ?: return
        updateInfo = null
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DISMISSED_VERSION, version)
            .apply()
    }

    private fun fetchLatestRelease(context: Context): UpdateInfo? {
        val currentVersion = currentVersionName(context) ?: return null
        return try {
            val conn = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            if (conn.responseCode != 200) {
                conn.disconnect()
                return null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(body)
            val tagName = json.optString("tag_name")
            if (tagName.isEmpty()) return null
            val remoteVersion = tagName.removePrefix("v").trim()
            if (!isNewer(remoteVersion, currentVersion)) return null

            UpdateInfo(
                currentVersion = currentVersion,
                newVersion = remoteVersion,
                releaseUrl = json.optString("html_url").ifEmpty { RELEASE_PAGE },
                releaseNotes = json.optString("body").trim()
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun currentVersionName(context: Context): String? = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: Exception) {
        null
    }

    private fun isNewer(remote: String, current: String): Boolean {
        val r = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val c = current.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, c.size)) {
            val rv = r.getOrNull(i) ?: 0
            val cv = c.getOrNull(i) ?: 0
            if (rv != cv) return rv > cv
        }
        return false
    }
}

/** 一条 GitHub Release 的展示信息。 */
data class UpdateInfo(
    val currentVersion: String,
    val newVersion: String,
    val releaseUrl: String,
    val releaseNotes: String
)
