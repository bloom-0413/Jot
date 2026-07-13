package com.jot.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jot.app.behavior.Behavior
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode = ThemePreferences.currentMode()
            JotTheme(themeMode = themeMode) {
                UnifiedApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPage(onOpenDrawer: () -> Unit = {}) {
    val context = LocalContext.current

    NoteListScaffold(
        title = stringResource(R.string.notes),
        onOpenDrawer = onOpenDrawer,
        loadNotes = { NoteRepository(it).loadNotes(Behavior.noteSort) },
        emptyIconRes = R.drawable.ic_notes,
        onNoteClick = { note ->
            val intent = Intent(context, CreateNoteActivity::class.java).apply {
                putExtra(CreateNoteActivity.EXTRA_NOTE_ID, note.id)
            }
            context.startActivity(intent)
        },
        actions = { hasSelection, selectedNoteIds, refresh, clearSelection ->
            Box(Modifier.fillMaxHeight()) {
                // 非选中: 搜索按钮
                AnimatedVisibility(
                    visible = !hasSelection,
                    enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SearchActivity::class.java))
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                // 选中: 归档 + 删除
                AnimatedVisibility(
                    visible = hasSelection,
                    enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Row {
                        IconButton(onClick = {
                            val repo = NoteRepository(context)
                            selectedNoteIds.forEach { id -> repo.archiveNote(id) }
                            clearSelection()
                            refresh()
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_archive),
                                contentDescription = stringResource(R.string.archive),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = {
                            val repo = NoteRepository(context)
                            selectedNoteIds.forEach { id -> repo.trashNote(id) }
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
        },
        floatingActionButton = { hasSelection ->
            val fabScale by animateFloatAsState(
                targetValue = if (hasSelection) 0f else 1f,
                animationSpec = tween(175, easing = FastOutSlowInEasing),
                label = "fabScale"
            )
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, CreateNoteActivity::class.java))
                },
                modifier = Modifier.scale(fabScale),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.create_note),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    )
}
