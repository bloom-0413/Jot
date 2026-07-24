package com.jot.app

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
                UpdateDialog()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPage(onOpenDrawer: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                AnimatedVisibility(
                    visible = hasSelection,
                    enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Row {
                        IconButton(onClick = {
                            scope.launch {
                                val repo = NoteRepository(context)
                                selectedNoteIds.forEach { id -> repo.archiveNote(id) }
                                clearSelection()
                                refresh()
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_archive),
                                contentDescription = stringResource(R.string.archive),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                val repo = NoteRepository(context)
                                selectedNoteIds.forEach { id -> repo.trashNote(id) }
                                clearSelection()
                                refresh()
                            }
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

@Composable
fun UpdateDialog() {
    val info = UpdateChecker.updateInfo ?: return
    val context = LocalContext.current
    UpdateDialog(
        info = info,
        onDismiss = { UpdateChecker.dismiss(context) }
    )
}

@Composable
fun UpdateDialog(
    info: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    JotAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.update_available_title)
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.update_version_message,
                        info.currentVersion,
                        info.newVersion
                    )
                )
                if (info.releaseNotes.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = info.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.releaseUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }) {
                Text(stringResource(R.string.update_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_later))
            }
        }
    )
}
