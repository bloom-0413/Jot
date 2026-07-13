package com.jot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemeMode
import com.jot.app.ui.theme.ThemePreferences

class AppearanceActivity : ComponentActivity() {
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
                            title = stringResource(R.string.appearance),
                            onBack = { finish() }
                        )
                    }
                ) { innerPadding ->
                    AppearanceScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentMode = ThemePreferences.currentMode()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // 主题项(圆角框,点击循环切换: 浅色 → 深色 → 系统 → 浅色)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .clickable {
                    val next = when (currentMode) {
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.SYSTEM
                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                    }
                    ThemePreferences.setMode(context, next)
                }
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fadeSpec = tween<Float>(durationMillis = 175, easing = FastOutSlowInEasing)
                    Crossfade(
                        targetState = currentMode,
                        animationSpec = fadeSpec,
                        label = "themeIcon"
                    ) { mode ->
                        Icon(
                            painter = painterResource(mode.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.theme),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Crossfade(
                        targetState = currentMode,
                        animationSpec = fadeSpec,
                        label = "themeLabel"
                    ) { mode ->
                        Text(
                            text = stringResource(mode.labelRes),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
    }
}
