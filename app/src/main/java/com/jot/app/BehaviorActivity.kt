package com.jot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jot.app.behavior.AutoUpdate
import com.jot.app.behavior.Behavior
import com.jot.app.behavior.CrashLog
import com.jot.app.behavior.NewNoteKeyboard
import com.jot.app.behavior.NoteSort
import com.jot.app.behavior.TrashAutoDelete
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemePreferences

class BehaviorActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerBackToMainIfNeeded()
        setContent {
            val themeMode = ThemePreferences.currentMode()
            JotTheme(themeMode = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        JotTopAppBar(
                            title = stringResource(R.string.behavior),
                            onBack = { finish() }
                        )
                    }
                ) { innerPadding ->
                    BehaviorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BehaviorScreen(modifier: Modifier = Modifier) {
    var keyboardEnabled by remember { mutableStateOf(Behavior.newNoteKeyboard == NewNoteKeyboard.ENABLED) }
    var trashAutoDeleteEnabled by remember { mutableStateOf(Behavior.trashAutoDelete == TrashAutoDelete.ENABLED) }
    var crashLogEnabled by remember { mutableStateOf(Behavior.crashLog == CrashLog.ENABLED) }
    var autoUpdateEnabled by remember { mutableStateOf(Behavior.autoUpdate == AutoUpdate.ENABLED) }
    var currentSort by remember { mutableStateOf(Behavior.noteSort) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavEntryItem(
            label = stringResource(R.string.auto_keyboard),
            iconRes = R.drawable.ic_keyboard,
            onClick = {
                keyboardEnabled = !keyboardEnabled
                Behavior.updateNewNoteKeyboard(if (keyboardEnabled) NewNoteKeyboard.ENABLED else NewNoteKeyboard.DISABLED)
            },
            trailing = {
                Text(
                    text = stringResource(if (keyboardEnabled) R.string.enabled else R.string.disabled),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
        NavEntryItem(
            label = stringResource(R.string.trash_auto_delete),
            iconRes = R.drawable.ic_auto_delete,
            onClick = {
                trashAutoDeleteEnabled = !trashAutoDeleteEnabled
                Behavior.updateTrashAutoDelete(if (trashAutoDeleteEnabled) TrashAutoDelete.ENABLED else TrashAutoDelete.DISABLED)
            },
            trailing = {
                Text(
                    text = stringResource(if (trashAutoDeleteEnabled) R.string.enabled else R.string.disabled),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
        NavEntryItem(
            label = stringResource(R.string.auto_update),
            iconRes = if (autoUpdateEnabled) R.drawable.ic_update_enabled else R.drawable.ic_update_disabled,
            onClick = {
                autoUpdateEnabled = !autoUpdateEnabled
                Behavior.updateAutoUpdate(if (autoUpdateEnabled) AutoUpdate.ENABLED else AutoUpdate.DISABLED)
            },
            trailing = {
                Text(
                    text = stringResource(if (autoUpdateEnabled) R.string.enabled else R.string.disabled),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
        NavEntryItem(
            label = stringResource(R.string.sort_order),
            iconRes = R.drawable.ic_sort,
            onClick = {
                currentSort = if (currentSort == NoteSort.CREATED_AT) NoteSort.UPDATED_AT else NoteSort.CREATED_AT
                Behavior.updateNoteSort(currentSort)
            },
            trailing = {
                Text(
                    text = stringResource(if (currentSort == NoteSort.CREATED_AT) R.string.sort_created else R.string.sort_updated),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
        NavEntryItem(
            label = stringResource(R.string.crash_log),
            iconRes = R.drawable.ic_report,
            onClick = {
                crashLogEnabled = !crashLogEnabled
                Behavior.updateCrashLog(if (crashLogEnabled) CrashLog.ENABLED else CrashLog.DISABLED)
            },
            trailing = {
                Text(
                    text = stringResource(if (crashLogEnabled) R.string.enabled else R.string.disabled),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}
