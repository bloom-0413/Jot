package com.jot.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

/**
 * 统一的顶栏配置:背景色/标题色/图标色与页面背景一致。
 * - onMenu != null 时显示左侧菜单按钮(用于侧边栏入口)
 * - onMenu == null 时显示左侧返回按钮(onBack 控制行为,默认 finish 当前 Activity)
 *
 * actions 用普通 @Composable 而非 RowScope.() -> Unit,避免调用方在 lambda 内
 * 直接写 AnimatedVisibility 时被编译器优先匹配到 RowScope.AnimatedVisibility 重载。
 * TopAppBar 内部会把 actions 包在 Row 里,布局效果不变。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JotTopAppBar(
    title: String,
    onBack: () -> Unit = {},
    onMenu: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenu ?: onBack) {
                Icon(
                    painter = painterResource(
                        if (onMenu != null) R.drawable.ic_menu else R.drawable.ic_back
                    ),
                    contentDescription = stringResource(
                        if (onMenu != null) R.string.menu else R.string.back
                    )
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
