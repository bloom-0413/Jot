package com.jot.app

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

/**
 * 为子 Activity 注册系统返回键处理:
 * - 若当前 Activity 是任务根(没有父 Activity),回到 MainActivity 避免直接退出应用
 * - 否则正常 finish 返回上一级
 *
 * 用于外观/行为/关于/搜索等从侧边栏或设置入口启动的子页面。
 */
fun ComponentActivity.registerBackToMainIfNeeded() {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isTaskRoot) {
                startActivity(Intent(this@registerBackToMainIfNeeded, MainActivity::class.java))
            }
            finish()
        }
    })
}
