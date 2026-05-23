package id.biz.rrndev.appanalyzer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.AppSortMode
import id.biz.rrndev.appanalyzer.model.InstalledAppsUiState
import id.biz.rrndev.appanalyzer.model.TrackerScanStatus
import id.biz.rrndev.appanalyzer.ui.components.EmptyState
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.PackageAppIcon
import id.biz.rrndev.appanalyzer.ui.components.ScoreBadge
import id.biz.rrndev.appanalyzer.ui.components.SectionTitle

@Composable
fun InstalledAppsScreen(
    state: InstalledAppsUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (AppSortMode) -> Unit,
    onHighRiskOnlyChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onOpenApp: (AppAnalysis) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        SectionTitle(
            title = stringResource(R.string.installed_apps_title),
            subtitle = stringResource(R.string.installed_apps_subtitle)
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.installed_apps_search_label)) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppSortMode.entries.forEach { sortMode ->
                FilterChip(
                    selected = state.sortMode == sortMode,
                    onClick = { onSortChange(sortMode) },
                    label = { Text(stringResource(sortMode.labelRes)) }
                )
            }
            FilterChip(
                selected = state.highRiskOnly,
                onClick = { onHighRiskOnlyChange(!state.highRiskOnly) },
                label = { Text(stringResource(R.string.installed_apps_filter_high_risk_only)) }
            )
        }

        if (state.errorMessage != null) {
            EmptyState(
                title = stringResource(R.string.scan_failed_title),
                message = state.errorMessage
            )
            Button(onClick = onRefresh) {
                Text(stringResource(R.string.action_retry))
            }
        }

        if (state.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    if (state.apps.isEmpty()) {
                        stringResource(R.string.loading_fast_scanning_installed_apps)
                    } else {
                        stringResource(R.string.loading_refreshing_app_list)
                    }
                )
            }
        }

        val visibleApps = state.visibleApps
        if (!state.isLoading && visibleApps.isEmpty() && state.errorMessage == null) {
            EmptyState(
                title = stringResource(R.string.empty_no_apps_match_view_title),
                message = stringResource(R.string.empty_no_apps_match_view_message)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = visibleApps,
                key = { it.id }
            ) { analysis ->
                InstalledAppCard(
                    analysis = analysis,
                    onClick = { onOpenApp(analysis) }
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InstalledAppCard(
    analysis: AppAnalysis,
    onClick: () -> Unit
) {
    val permissionText = pluralStringResource(
        R.plurals.permissions_count,
        analysis.permissionCount,
        analysis.permissionCount
    )
    val trackerText = if (analysis.trackerScanStatus == TrackerScanStatus.Scanned) {
        pluralStringResource(
            R.plurals.trackers_count,
            analysis.trackerCount,
            analysis.trackerCount
        )
    } else {
        stringResource(R.string.tracker_scan_tap_hint)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PackageAppIcon(
                packageName = analysis.packageName,
                bitmap = analysis.icon,
                fallbackText = analysis.appName,
                modifier = Modifier.size(46.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = analysis.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = analysis.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.metadata_two_part_format, permissionText, trackerText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            ScoreBadge(score = analysis.score.value, riskLevel = analysis.score.riskLevel)
        }
    }
}
