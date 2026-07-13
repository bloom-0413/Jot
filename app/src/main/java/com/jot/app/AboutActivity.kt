package com.jot.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemePreferences

class AboutActivity : ComponentActivity() {
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
                            title = stringResource(R.string.about),
                            onBack = { finish() }
                        )
                    }
                ) { innerPadding ->
                    AboutScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
    var checking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpToDate by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 应用图标 + 名称 + 版本
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        ) {
                Icon(
                    painter = painterResource(R.drawable.ic_about),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.Unspecified
                )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Version $versionName",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 检查更新
        NavEntryItem(
            label = stringResource(R.string.check_update),
            iconRes = R.drawable.ic_update,
            onClick = {
                checking = true
                UpdateChecker.checkForUpdates(context) { info ->
                    checking = false
                    if (info != null) {
                        updateResult = info
                    } else {
                        showUpToDate = true
                    }
                }
            }
        )
        // 问题反馈
        NavEntryItem(
            label = stringResource(R.string.feedback),
            iconRes = R.drawable.ic_forum,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bloom-0413/Jot/issues/new"))
                context.startActivity(intent)
            }
        )
        // GitHub 仓库
        NavEntryItem(
            label = stringResource(R.string.github_repository),
            iconRes = R.drawable.ic_code,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bloom-0413/Jot"))
                context.startActivity(intent)
            }
        )
    }

    // 检测中弹窗
    if (checking) {
        AlertDialog(
            onDismissRequest = { checking = false },
            title = { Text(text = stringResource(R.string.check_update)) },
            text = { Text(text = stringResource(R.string.checking_update)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { checking = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // 已是最新版
    if (showUpToDate) {
        AlertDialog(
            onDismissRequest = { showUpToDate = false },
            title = { Text(text = stringResource(R.string.check_update)) },
            text = { Text(text = stringResource(R.string.up_to_date)) },
            confirmButton = {
                TextButton(onClick = { showUpToDate = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // 发现新版本
    updateResult?.let { info ->
        UpdateDialog(
            info = info,
            onDismiss = { updateResult = null }
        )
    }
}
