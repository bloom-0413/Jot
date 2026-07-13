package com.jot.app

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jot.app.behavior.Behavior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashPage(onOpenDrawer: () -> Unit = {}) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    // 点击删除按钮时快照选中集合,避免对话框显示期间选中被清空导致行为不一致
    var pendingSelection by remember { mutableStateOf<Set<Long>>(emptySet()) }

    NoteListScaffold(
        title = stringResource(R.string.trash),
        onOpenDrawer = onOpenDrawer,
        loadNotes = { NoteRepository(it).loadTrashedNotes(Behavior.noteSort) },
        emptyIconRes = R.drawable.ic_trash,
        onNoteClick = { note ->
            val intent = Intent(context, CreateNoteActivity::class.java).apply {
                putExtra(CreateNoteActivity.EXTRA_READ_ONLY, true)
                putExtra(CreateNoteActivity.EXTRA_VIEW_TITLE, note.title)
                putExtra(CreateNoteActivity.EXTRA_VIEW_CONTENT, note.content)
            }
            context.startActivity(intent)
        },
        actions = { hasSelection, selectedNoteIds, refresh, clearSelection ->
            AnimatedVisibility(
                visible = hasSelection,
                enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing))
            ) {
                IconButton(onClick = {
                    val repo = NoteRepository(context)
                    selectedNoteIds.forEach { id -> repo.restoreFromTrash(id) }
                    clearSelection()
                    refresh()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_restore),
                        contentDescription = stringResource(R.string.restore),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            IconButton(onClick = {
                pendingSelection = selectedNoteIds
                showDeleteDialog = true
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever),
                    contentDescription = stringResource(R.string.delete_forever),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        dialog = { _, refresh, clearSelection ->
            if (showDeleteDialog) {
                JotAlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(
                            text = if (pendingSelection.isNotEmpty()) {
                                stringResource(R.string.delete_forever_selected_title)
                            } else {
                                stringResource(R.string.delete_forever_all_title)
                            }
                        )
                    },
                    text = {
                        Text(
                            text = if (pendingSelection.isNotEmpty()) {
                                stringResource(R.string.delete_forever_selected_message, pendingSelection.size)
                            } else {
                                stringResource(R.string.delete_forever_all_message)
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val repo = NoteRepository(context)
                            if (pendingSelection.isNotEmpty()) {
                                pendingSelection.forEach { id -> repo.permanentlyDeleteTrash(id) }
                            } else {
                                repo.clearAllTrash()
                            }
                            showDeleteDialog = false
                            clearSelection()
                            refresh()
                        }) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    )
}
