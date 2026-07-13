package com.jot.app

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(onOpenDrawer: () -> Unit = {}) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            JotTopAppBar(
                title = stringResource(R.string.settings),
                onMenu = onOpenDrawer
            )
        }
    ) { innerPadding ->
        SettingsScreen(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavEntryItem(
            label = stringResource(R.string.appearance),
            iconRes = R.drawable.ic_palette,
            onClick = {
                context.startActivity(Intent(context, AppearanceActivity::class.java))
            }
        )
        NavEntryItem(
            label = stringResource(R.string.behavior),
            iconRes = R.drawable.ic_tune,
            onClick = {
                context.startActivity(Intent(context, BehaviorActivity::class.java))
            }
        )
        NavEntryItem(
            label = stringResource(R.string.backup),
            iconRes = R.drawable.ic_sd_card,
            onClick = {
                context.startActivity(Intent(context, BackupActivity::class.java))
            }
        )
        NavEntryItem(
            label = stringResource(R.string.about),
            iconRes = R.drawable.ic_info,
            onClick = {
                context.startActivity(Intent(context, AboutActivity::class.java))
            }
        )
    }
}
