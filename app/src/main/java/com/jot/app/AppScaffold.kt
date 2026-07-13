package com.jot.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// 应用路由:每个页面对应一个侧边栏项
enum class AppRoute(val labelRes: Int, val iconRes: Int) {
    NOTES(R.string.notes, R.drawable.ic_notes),
    ARCHIVE(R.string.archive, R.drawable.ic_archive),
    TRASH(R.string.trash, R.drawable.ic_trash),
    SETTINGS(R.string.settings, R.drawable.ic_settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidthDp = screenWidthDp * 0.75f
    val drawerWidthPx = with(density) { drawerWidthDp.toPx() }
    val drawerOffset = remember { Animatable(0f) }
    val drawerColor = MaterialTheme.colorScheme.surfaceVariant
    val onDrawerColor = MaterialTheme.colorScheme.onSurfaceVariant
    val drawerAnimSpec = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)
    val crossfadeSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

    var currentRoute by remember { mutableStateOf(AppRoute.NOTES) }

    suspend fun openDrawer() = drawerOffset.animateTo(1f, drawerAnimSpec)
    suspend fun closeDrawer() = drawerOffset.animateTo(0f, drawerAnimSpec)
    fun navigateTo(route: AppRoute) {
        if (route == currentRoute) {
            scope.launch { closeDrawer() }
            return
        }
        scope.launch {
            closeDrawer()
            currentRoute = route
        }
    }

    // 侧边栏打开时,系统返回键先关闭侧边栏
    if (drawerOffset.value > 0f) {
        BackHandler { scope.launch { closeDrawer() } }
    }
    // 不在笔记页且侧边栏已关闭时,系统返回键回到笔记页
    if (drawerOffset.value == 0f && currentRoute != AppRoute.NOTES) {
        BackHandler { currentRoute = AppRoute.NOTES }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 主内容区(渐变动画切换页面)
        Crossfade(
            targetState = currentRoute,
            animationSpec = crossfadeSpec,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) { route ->
            when (route) {
                AppRoute.NOTES -> NotesPage(onOpenDrawer = { scope.launch { openDrawer() } })
                AppRoute.ARCHIVE -> ArchivePage(onOpenDrawer = { scope.launch { openDrawer() } })
                AppRoute.TRASH -> TrashPage(onOpenDrawer = { scope.launch { openDrawer() } })
                AppRoute.SETTINGS -> SettingsPage(onOpenDrawer = { scope.launch { openDrawer() } })
            }
        }

        // 遮罩层(抽屉打开时渐显,点击关闭)
        if (drawerOffset.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f * drawerOffset.value))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { scope.launch { closeDrawer() } }
            )
        }

        // 侧边栏(从左侧滑入)
        Box(
            modifier = Modifier
                .offset { IntOffset(((drawerOffset.value - 1f) * drawerWidthPx).roundToInt(), 0) }
                .width(drawerWidthDp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(drawerColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // 文字 Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = onDrawerColor
                    )
                }
                // 导航项(当前页高亮)
                AppRoute.entries.forEach { route ->
                    DrawerItem(
                        iconRes = route.iconRes,
                        label = stringResource(route.labelRes),
                        iconColor = onDrawerColor,
                        textColor = onDrawerColor,
                        selected = route == currentRoute,
                        onClick = { navigateTo(route) }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    iconRes: Int,
    label: String,
    iconColor: Color,
    textColor: Color,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (selected) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = iconColor
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp),
            color = textColor
        )
    }
}
