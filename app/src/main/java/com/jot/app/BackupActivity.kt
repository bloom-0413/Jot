package com.jot.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jot.app.ui.theme.JotTheme
import com.jot.app.ui.theme.ThemePreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerBackToMainIfNeeded()

        val exportLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        BackupManager.exportBackup(this, outputStream)
                    }
                    Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val importLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BackupManager.importBackup(this, inputStream)
                        Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.import_error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContent {
            val themeMode = ThemePreferences.currentMode()
            JotTheme(themeMode = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        JotTopAppBar(
                            title = stringResource(R.string.backup),
                            onBack = { finish() }
                        )
                    }
                ) { innerPadding ->
                    BackupScreen(
                        modifier = Modifier.padding(innerPadding),
                        onExport = {
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            exportLauncher.launch("Jot_$dateStr.backup")
                        },
                        onImport = {
                            importLauncher.launch(arrayOf("*/*"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BackupScreen(
    modifier: Modifier = Modifier,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavEntryItem(
            label = stringResource(R.string.export_backup),
            iconRes = R.drawable.ic_upload,
            onClick = onExport
        )
        NavEntryItem(
            label = stringResource(R.string.import_backup),
            iconRes = R.drawable.ic_download,
            onClick = onImport
        )
    }
}
