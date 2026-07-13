package com.jot.app

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivePage(onOpenDrawer: () -> Unit = {}) {
    val context = LocalContext.current

    NoteListScaffold(
        title = stringResource(R.string.archive),
        onOpenDrawer = onOpenDrawer,
        loadNotes = { NoteRepository(it).loadArchivedNotes() },
        emptyIconRes = R.drawable.ic_archive,
        onNoteClick = { note ->
            val intent = Intent(context, CreateNoteActivity::class.java).apply {
                putExtra(CreateNoteActivity.EXTRA_NOTE_ID, note.id)
                putExtra(CreateNoteActivity.EXTRA_ARCHIVED, true)
            }
            context.startActivity(intent)
        },
        actions = { hasSelection, selectedNoteIds, refresh, clearSelection ->
            AnimatedVisibility(
                visible = hasSelection,
                enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing))
            ) {
                Row {
                    IconButton(onClick = {
                        val repo = NoteRepository(context)
                        selectedNoteIds.forEach { id -> repo.restoreFromArchive(id) }
                        clearSelection()
                        refresh()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_unarchive),
                            contentDescription = stringResource(R.string.unarchive),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = {
                        val repo = NoteRepository(context)
                        selectedNoteIds.forEach { id -> repo.trashArchivedNote(id) }
                        clearSelection()
                        refresh()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    )
}
