package com.startscope.coldstart.ui.timeline

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.startscope.coldstart.R
import com.startscope.coldstart.data.StartReasonStrings
import com.startscope.coldstart.data.StartTypeStrings
import com.startscope.coldstart.data.local.StartEntity
import com.startscope.coldstart.ui.DaySection
import com.startscope.coldstart.ui.theme.startTypeColor
import com.startscope.coldstart.util.packageLabel
import com.startscope.coldstart.work.WorkScheduling
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TimelineScreen(
    sections: List<DaySection>,
    packages: List<String>,
    selectedPackage: String?,
    onPackageSelected: (String?) -> Unit,
    onStartClick: (Long) -> Unit,
    onAppStatsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val timeFmt = remember {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.timeline_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { WorkScheduling.enqueueOneTime(context) },
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.refresh_now))
            }
            if (selectedPackage != null) {
                IconButton(
                    onClick = { onAppStatsClick(selectedPackage) },
                ) {
                    Icon(Icons.Filled.BarChart, contentDescription = stringResource(R.string.app_detail_title))
                }
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                FilterChip(
                    selected = selectedPackage == null,
                    onClick = { onPackageSelected(null) },
                    label = { Text(stringResource(R.string.filter_all_apps)) },
                )
            }
            items(packages, key = { it }) { pkg ->
                FilterChip(
                    selected = selectedPackage == pkg,
                    onClick = { onPackageSelected(pkg) },
                    label = { Text(packageLabel(pm, pkg)) },
                )
            }
        }

        if (sections.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.empty_timeline_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.empty_timeline_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEach { section ->
                    item(key = section.dayKey + "_header") {
                        Text(
                            text = section.dayLabel,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(section.items, key = { it.id }) { start ->
                        StartRow(
                            start = start,
                            pm = pm,
                            timeFmt = timeFmt,
                            onRowClick = { onStartClick(start.id) },
                            onPackageClick = { onAppStatsClick(start.packageName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartRow(
    start: StartEntity,
    pm: PackageManager,
    timeFmt: DateTimeFormatter,
    onRowClick: () -> Unit,
    onPackageClick: () -> Unit,
) {
    val context = LocalContext.current
    val label = packageLabel(pm, start.packageName)
    val time = timeFmt.format(Instant.ofEpochMilli(start.timestampMs))
    val typeLabel = StartTypeStrings.label(context, start.startType)
    val reason = StartReasonStrings.label(context, start.reason)
    val ttid = start.ttidMs?.let { context.getString(R.string.ms_suffix, it) } ?: stringResource(R.string.na)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = startTypeColor(start.startType),
                modifier = Modifier.clickable(onClick = onPackageClick),
            )
            Text(
                text = "$time · $typeLabel · $ttid",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
