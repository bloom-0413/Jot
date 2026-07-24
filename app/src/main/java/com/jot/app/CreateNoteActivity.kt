package com.jot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jot.app.behavior.Behavior
import com.jot.app.behavior.NewNoteKeyboard
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreateNoteActivity : ComponentActivity() {

    private val noteId: Long by lazy {
        intent?.getLongExtra(EXTRA_NOTE_ID, -1L) ?: -1L
    }

    private val isEditing: Boolean
        get() = noteId != -1L

    // 编辑过程中持有的状态(供 debounce 保存逻辑读取)
    private var currentTitle: String = ""
    private var currentContent: String = ""
    private var existingCreatedAt: Long = 0L
    private var dirty: Boolean = false
    // 新笔记首次保存后生成的 id(后续 debounce 保存复用,避免重复创建)
    private var newNoteId: Long = 0L

    // 协程作用域与 debounce 任务句柄
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var debounceJob: Job? = null

    override fun onDestroy() {
        super.onDestroy()
        // Activity 销毁时立即落盘(取消未触发的 debounce,直接保存最终内容)
        saveImmediately()
        scope.cancel()
    }

    /**
     * 触发 debounce 保存:500ms 内若再次输入则取消上次,只保存最后一次。
     */
    private fun scheduleSave(title: String, content: String) {
        currentTitle = title
        currentContent = content
        dirty = true
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(500)
            doSave()
        }
    }

    /**
     * 立即保存(退出时调用),取消 debounce 直接落盘。
     * 内容为空且非编辑模式时不留垃圾记录。
     * 未修改内容时跳过保存，不更新 updatedAt。
     */
    private fun saveImmediately() {
        debounceJob?.cancel()
        if (!dirty) return
        // 仅在 Activity 已启动且有输入过内容时保存
        if (currentTitle.isBlank() && currentContent.isBlank() && !isEditing) return
        if (currentTitle.isBlank() && currentContent.isBlank() && isEditing) {
            // 编辑模式下若被清空,删除该笔记
            deleteNote()
            return
        }
        doSave()
    }

    private fun doSave() {
        val title = currentTitle
        val content = currentContent
        if (title.isBlank() && content.isBlank()) return
        val now = System.currentTimeMillis()
        // 新笔记首次保存时生成 id 并缓存,后续 debounce 保存复用同一 id
        val id = when {
            isEditing -> noteId
            newNoteId != 0L -> newNoteId
            else -> {
                newNoteId = now
                newNoteId
            }
        }
        val note = Note(
            id = id,
            title = title,
            content = content,
            createdAt = if (isEditing) existingCreatedAt.takeIf { it != 0L } ?: now else id,
            updatedAt = now
        )
        // 切到 IO 线程写盘
        scope.launch(Dispatchers.IO) {
            val repo = NoteRepository(this@CreateNoteActivity)
            if (isArchived) {
                repo.upsertArchivedNote(note)
            } else {
                repo.upsertNote(note)
            }
        }
    }

    private fun deleteNote() {
        val idToDelete = if (isEditing) noteId else newNoteId
        if (idToDelete == -1L || idToDelete == 0L) return
        scope.launch(Dispatchers.IO) {
            NoteRepository(this@CreateNoteActivity).deleteNote(idToDelete)
        }
    }

    private val isReadOnly: Boolean by lazy {
        intent?.getBooleanExtra(EXTRA_READ_ONLY, false) ?: false
    }

    private val isArchived: Boolean by lazy {
        intent?.getBooleanExtra(EXTRA_ARCHIVED, false) ?: false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isReadOnly) {
            val viewTitle = intent?.getStringExtra(EXTRA_VIEW_TITLE) ?: ""
            val viewContent = intent?.getStringExtra(EXTRA_VIEW_CONTENT) ?: ""

            setContent {
                JotTheme(themeMode = ThemePreferences.currentMode()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            JotTopAppBar(
                                title = stringResource(R.string.view),
                                onBack = { finish() }
                            )
                        }
                    ) { innerPadding ->
                        CreateNoteScreen(
                            modifier = Modifier.padding(innerPadding),
                            title = viewTitle,
                            content = viewContent,
                            onTitleChange = {},
                            onContentChange = {},
                            readOnly = true
                        )
                    }
                }
            }
            return
        }

        val existingNote: Note? = if (isEditing) {
            if (isArchived) {
                NoteRepository(this).findNoteById(noteId)
            } else {
                NoteRepository(this).findNoteById(noteId)
            }
        } else null
        // 初始化状态供退出时使用
        currentTitle = existingNote?.title ?: ""
        currentContent = existingNote?.content ?: ""
        existingCreatedAt = existingNote?.createdAt ?: 0L

        setContent {
            JotTheme(themeMode = ThemePreferences.currentMode()) {
                var title by remember { mutableStateOf(existingNote?.title ?: "") }
                var content by remember { mutableStateOf(existingNote?.content ?: "") }

                val onExit: () -> Unit = {
                    // 退出时立即落盘(覆盖 debounce 中尚未保存的内容)
                    saveImmediately()
                    finish()
                }

                // 系统返回键保存并退出
                BackHandler { onExit() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        JotTopAppBar(
                            title = stringResource(R.string.edit),
                            onBack = { onExit() }
                        )
                    }
                ) { innerPadding ->
                    CreateNoteScreen(
                        modifier = Modifier.padding(innerPadding),
                        title = title,
                        content = content,
                        onTitleChange = {
                            title = it
                            scheduleSave(title, content)
                        },
                        onContentChange = {
                            content = it
                            scheduleSave(title, content)
                        },
                        autoFocusTitle = !isEditing && Behavior.newNoteKeyboard == NewNoteKeyboard.ENABLED
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_READ_ONLY = "extra_read_only"
        const val EXTRA_VIEW_TITLE = "extra_view_title"
        const val EXTRA_VIEW_CONTENT = "extra_view_content"
        const val EXTRA_ARCHIVED = "extra_archived"
    }
}

@Composable
fun CreateNoteScreen(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    autoFocusTitle: Boolean = false,
    readOnly: Boolean = false
) {
    var titleFocused by remember { mutableStateOf(false) }
    var contentFocused by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val titleBorderColor by animateColorAsState(
        targetValue = if (titleFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(175),
        label = "titleBorder"
    )
    val contentBorderColor by animateColorAsState(
        targetValue = if (contentFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(175),
        label = "contentBorder"
    )

    LaunchedEffect(autoFocusTitle) {
        if (autoFocusTitle) {
            titleFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题(带圆角边框,高度随文字自适应,支持多行)
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onFocusChanged { titleFocused = it.isFocused }
                .focusRequester(titleFocusRequester)
                .border(1.dp, titleBorderColor, RoundedCornerShape(16.dp)),
            enabled = !readOnly,
            placeholder = {
                Text(
                    text = stringResource(R.string.title_placeholder),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    style = TextStyle(fontSize = 20.sp)
                )
            },
            textStyle = TextStyle(fontSize = 20.sp),
            colors = JotTextFieldColors()
        )

        // 笔记内容(带圆角边框,默认延伸到底部)
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onFocusChanged { contentFocused = it.isFocused }
                .border(1.dp, contentBorderColor, RoundedCornerShape(16.dp)),
            enabled = !readOnly,
            placeholder = {
                Text(
                    text = stringResource(R.string.content_placeholder),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    style = TextStyle(fontSize = 16.sp)
                )
            },
            textStyle = TextStyle(fontSize = 16.sp),
            colors = JotTextFieldColors()
        )
    }
}

/**
 * 标题/正文输入框统一配色:透明容器与下划线,禁用时文字变浅。
 */
@Composable
fun JotTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    disabledTextColor = MaterialTheme.colorScheme.outlineVariant
)
