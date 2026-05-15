package com.startscope.coldstart.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.startscope.coldstart.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    retentionDays: Int,
    collectionEnabled: Boolean,
    onRetentionChange: (Int) -> Unit,
    onCollectionChange: (Boolean) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.tab_timeline),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                stringResource(R.string.privacy_no_internet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.start_service), style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = collectionEnabled,
                onCheckedChange = onCollectionChange,
            )
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.retention_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.retention_days, retentionDays),
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = retentionDays.toFloat(),
                onValueChange = { onRetentionChange(it.toInt().coerceIn(1, 30)) },
                valueRange = 1f..30f,
                steps = 28,
            )
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.about_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
