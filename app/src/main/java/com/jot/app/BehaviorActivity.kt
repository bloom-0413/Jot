package com.jot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    var currentSort by remember { mutableStateOf(Behavior.noteSort) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BehaviorToggleItem(
            label = stringResource(R.string.auto_keyboard),
            iconRes = R.drawable.ic_keyboard,
            enabled = keyboardEnabled,
            onClick = {
                keyboardEnabled = !keyboardEnabled
                Behavior.updateNewNoteKeyboard(if (keyboardEnabled) NewNoteKeyboard.ENABLED else NewNoteKeyboard.DISABLED)
            }
        )
        BehaviorToggleItem(
            label = stringResource(R.string.trash_auto_delete),
            iconRes = R.drawable.ic_auto_delete,
            enabled = trashAutoDeleteEnabled,
            onClick = {
                trashAutoDeleteEnabled = !trashAutoDeleteEnabled
                Behavior.updateTrashAutoDelete(if (trashAutoDeleteEnabled) TrashAutoDelete.ENABLED else TrashAutoDelete.DISABLED)
            }
        )
        BehaviorToggleItem(
            label = stringResource(R.string.sort_order),
            iconRes = R.drawable.ic_sort,
            enabled = currentSort == NoteSort.CREATED_AT,
            valueText = if (currentSort == NoteSort.CREATED_AT) stringResource(R.string.sort_created) else stringResource(R.string.sort_updated),
            onClick = {
                currentSort = if (currentSort == NoteSort.CREATED_AT) NoteSort.UPDATED_AT else NoteSort.CREATED_AT
                Behavior.updateNoteSort(currentSort)
            }
        )
        BehaviorToggleItem(
            label = stringResource(R.string.crash_log),
            iconRes = R.drawable.ic_report,
            enabled = crashLogEnabled,
            onClick = {
                crashLogEnabled = !crashLogEnabled
                Behavior.updateCrashLog(if (crashLogEnabled) CrashLog.ENABLED else CrashLog.DISABLED)
            }
        )
    }
}

@Composable
fun BehaviorToggleItem(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    valueText: String? = null,
    iconRes: Int? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText ?: if (enabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
