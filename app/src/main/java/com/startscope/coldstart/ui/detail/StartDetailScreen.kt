package com.startscope.coldstart.ui.detail

import android.app.ApplicationStartInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.startscope.coldstart.R
import com.startscope.coldstart.data.StartReasonStrings
import com.startscope.coldstart.data.StartTypeStrings
import com.startscope.coldstart.data.local.StartEntity
import com.startscope.coldstart.ui.theme.startTypeColor
import com.startscope.coldstart.util.packageLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDetailScreen(
    start: StartEntity?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.start_detail_title)) },
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
        if (start == null) {
            Text(
                stringResource(R.string.na),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
            return@Scaffold
        }
        val maxMs = maxOf(start.ttidMs ?: 0L, start.ttfdMs ?: 0L, 1L)
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                packageLabel(pm, start.packageName),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                StartTypeStrings.label(context, start.startType),
                style = MaterialTheme.typography.titleMedium,
                color = startTypeColor(start.startType),
            )
            Text(
                stringResource(R.string.reason_label) + ": " + StartReasonStrings.label(context, start.reason),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (start.intentAction != null) {
                Text(
                    text = start.intentAction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val startupNote = when (start.startupState) {
                ApplicationStartInfo.STARTUP_STATE_STARTED ->
                    stringResource(R.string.startup_incomplete)
                ApplicationStartInfo.STARTUP_STATE_ERROR ->
                    stringResource(R.string.startup_error)
                else -> null
            }
            if (startupNote != null) {
                Text(startupNote, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            PhaseBar(
                title = stringResource(R.string.ttid_label),
                valueMs = start.ttidMs,
                maxMs = maxMs,
            )
            PhaseBar(
                title = stringResource(R.string.ttfd_label),
                valueMs = start.ttfdMs,
                maxMs = maxMs,
            )
        }
    }
}

@Composable
private fun PhaseBar(
    title: String,
    valueMs: Long?,
    maxMs: Long,
) {
    val progress = if (valueMs != null && maxMs > 0) {
        (valueMs.toFloat() / maxMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                valueMs?.let { stringResource(R.string.ms_suffix, it) } ?: stringResource(R.string.na),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(top = 4.dp),
        )
    }
}
