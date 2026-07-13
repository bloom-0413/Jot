package com.jot.app

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * 三个笔记列表页(MainActivity / ArchivePage / TrashPage)共享的容器。
 *
 * 内部统一处理:
 * - 选中状态 [Set<Long>]: 长按切换、单击在选中模式下切换、BackHandler 取消选中
 * - ON_PAUSE 时清空选中,避免跳页后遗留
 * - ON_RESUME 时重新加载笔记(配合 [refreshKey] 用于手动触发刷新)
 * - LazyColumn + NoteCard + 统一的 175ms FastOutSlowInEasing 进出场动画
 * - 空状态 Icon(120dp,AnimatedVisibility 淡入)
 *
 * 调用方只需提供: 标题、加载函数、空状态图标、单击(非选中模式)行为,以及可选的顶栏 actions 与 FAB。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScaffold(
    title: String,
    onOpenDrawer: () -> Unit = {},
    loadNotes: (Context) -> List<Note>,
    emptyIconRes: Int,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (
        hasSelection: Boolean,
        selectedNoteIds: Set<Long>,
        refresh: () -> Unit,
        clearSelection: () -> Unit
    ) -> Unit = { _, _, _, _ -> },
    floatingActionButton: @Composable (hasSelection: Boolean) -> Unit = {},
    dialog: @Composable (
        selectedNoteIds: Set<Long>,
        refresh: () -> Unit,
        clearSelection: () -> Unit
    ) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var selectedNoteIds by remember { mutableStateOf(setOf<Long>()) }
    var refreshKey by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasSelection = selectedNoteIds.isNotEmpty()
    val refresh: () -> Unit = { refreshKey++ }
    val clearSelection: () -> Unit = { selectedNoteIds = emptySet() }

    if (hasSelection) {
        BackHandler { selectedNoteIds = emptySet() }
    }

    // ON_PAUSE 清空选中(跳转其他页时取消高亮)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                selectedNoteIds = emptySet()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 首次 + refreshKey 变化 + ON_RESUME 时加载笔记
    DisposableEffect(lifecycleOwner, refreshKey) {
        notes = loadNotes(context)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notes = loadNotes(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val noTitle = stringResource(R.string.no_title)
    val noContentPreview = stringResource(R.string.no_content_preview)

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                JotTopAppBar(
                    title = title,
                    onMenu = onOpenDrawer,
                    actions = { actions(hasSelection, selectedNoteIds, refresh, clearSelection) }
                )
            },
            floatingActionButton = { floatingActionButton(hasSelection) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        val animModifier = Modifier.animateItem(
                            fadeInSpec = tween(175, easing = FastOutSlowInEasing),
                            placementSpec = tween(175, easing = FastOutSlowInEasing),
                            fadeOutSpec = tween(175, easing = FastOutSlowInEasing)
                        )
                        NoteCard(
                            note = note,
                            modifier = animModifier,
                            isSelected = note.id in selectedNoteIds,
                            onClick = {
                                if (hasSelection) {
                                    selectedNoteIds = if (note.id in selectedNoteIds) {
                                        selectedNoteIds - note.id
                                    } else {
                                        selectedNoteIds + note.id
                                    }
                                } else {
                                    onNoteClick(note)
                                }
                            },
                            onLongClick = {
                                selectedNoteIds = if (note.id in selectedNoteIds) {
                                    selectedNoteIds - note.id
                                } else {
                                    selectedNoteIds + note.id
                                }
                            },
                            noTitle = noTitle,
                            noContentPreview = noContentPreview
                        )
                    }
                }
                AnimatedVisibility(
                    visible = notes.isEmpty(),
                    enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(0)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(emptyIconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
        }
        // 对话框层(回收站删除确认等)
        dialog(selectedNoteIds, refresh, clearSelection)
    }
}
