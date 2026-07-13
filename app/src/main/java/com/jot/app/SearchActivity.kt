package com.jot.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jot.app.behavior.Behavior
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SearchActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerBackToMainIfNeeded()
        setContent {
            val themeMode = ThemePreferences.currentMode()
            JotTheme(themeMode = themeMode) {
                SearchContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Note>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(175, easing = FastOutSlowInEasing),
        label = "searchBorder"
    )

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(250)
        withContext(Dispatchers.IO) {
            results = NoteRepository(context).searchNotes(query, Behavior.noteSort)
        }
    }
    val hasResults = results.isNotEmpty() && query.isNotBlank()
    val noTitle = stringResource(R.string.no_title)
    val noContentPreview = stringResource(R.string.no_content_preview)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            JotTopAppBar(
                title = stringResource(R.string.search),
                onBack = { (context as? ComponentActivity)?.finish() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp, bottom = 12.dp)
                    .align(Alignment.TopCenter)
                    .onFocusChanged { isFocused = it.isFocused }
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            AnimatedVisibility(
                visible = hasResults,
                enter = fadeIn(animationSpec = tween(175, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 76.dp)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(175, easing = FastOutSlowInEasing),
                                placementSpec = tween(175, easing = FastOutSlowInEasing),
                                fadeOutSpec = tween(175, easing = FastOutSlowInEasing)
                            ),
                            isSelected = false,
                            onClick = {
                                val intent = Intent(context, CreateNoteActivity::class.java).apply {
                                    putExtra(CreateNoteActivity.EXTRA_NOTE_ID, note.id)
                                }
                                context.startActivity(intent)
                            },
                            onLongClick = {},
                            noTitle = noTitle,
                            noContentPreview = noContentPreview
                        )
                    }
                }
            }

            if (query.isBlank() || (!hasResults && query.isNotBlank())) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(120.dp)
                )
            }
        }
    }
}
