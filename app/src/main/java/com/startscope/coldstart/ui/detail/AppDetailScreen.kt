package com.startscope.coldstart.ui.detail

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.startscope.coldstart.R
import com.startscope.coldstart.data.StartReasonStrings
import com.startscope.coldstart.data.StartTypeStrings
import com.startscope.coldstart.data.local.StartEntity
import com.startscope.coldstart.util.packageLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    state: AppDetailUiState,
    packageName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val title = packageLabel(pm, packageName)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.tab_timeline),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(stringResource(R.string.avg_cold), style = MaterialTheme.typography.labelLarge)
                Text(formatAvg(state.averages.coldMs))
            }
            item {
                Text(stringResource(R.string.avg_warm), style = MaterialTheme.typography.labelLarge)
                Text(formatAvg(state.averages.warmMs))
            }
            item {
                Text(stringResource(R.string.avg_hot), style = MaterialTheme.typography.labelLarge)
                Text(formatAvg(state.averages.hotMs))
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.starts_by_reason), style = MaterialTheme.typography.titleMedium)
            }
            items(state.reasonCounts.entries.sortedByDescending { it.value }) { (reason, count) ->
                RowStat(
                    label = StartReasonStrings.label(context, reason),
                    value = count.toString(),
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.last_starts), style = MaterialTheme.typography.titleMedium)
            }
            items(state.recent) { row ->
                RecentRow(row, pm)
            }
        }
    }
}

@Composable
private fun formatAvg(ms: Long?): String =
    if (ms == null) {
        stringResource(R.string.na)
    } else {
        stringResource(R.string.ms_suffix, ms)
    }

@Composable
private fun RowStat(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RecentRow(entity: StartEntity, pm: PackageManager) {
    val context = LocalContext.current
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(
            packageLabel(pm, entity.packageName),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            StartTypeStrings.label(context, entity.startType) + " · " +
                (entity.ttidMs?.let { stringResource(R.string.ms_suffix, it) } ?: stringResource(R.string.na)),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            StartReasonStrings.label(context, entity.reason),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
