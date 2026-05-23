package id.biz.rrndev.appanalyzer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.ApkAnalyzerUiState
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.AppSortMode
import id.biz.rrndev.appanalyzer.model.InstalledAppsUiState
import id.biz.rrndev.appanalyzer.model.TrackerScanStatus
import id.biz.rrndev.appanalyzer.ui.components.AppIconBitmap
import id.biz.rrndev.appanalyzer.ui.components.EmptyState
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.PackageAppIcon
import id.biz.rrndev.appanalyzer.ui.components.PrivacyScoreCard
import id.biz.rrndev.appanalyzer.ui.components.ScoreBadge

@Composable
fun AppsScreen(
    installedAppsState: InstalledAppsUiState,
    apkState: ApkAnalyzerUiState,
    deepTrackerScan: Boolean,
    onQueryChange: (String) -> Unit,
    onSortChange: (AppSortMode) -> Unit,
    onHighRiskOnlyChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onOpenApp: (AppAnalysis) -> Unit,
    onAnalyzeApk: (Uri) -> Unit,
    onClear: () -> Unit,
    onOpenApkResult: (AppAnalysis) -> Unit,
    onTabChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        stringResource(R.string.tab_apps),
        stringResource(R.string.tab_apk)
    )

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                selectedTab = index
                                onTabChange(index)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val toRight = targetState > initialState
                slideInHorizontally { if (toRight) it else -it } + fadeIn() togetherWith
                    slideOutHorizontally { if (toRight) -it else it } + fadeOut()
            },
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                0 -> InstalledAppsTab(
                    state = installedAppsState,
                    onQueryChange = onQueryChange,
                    onSortChange = onSortChange,
                    onHighRiskOnlyChange = onHighRiskOnlyChange,
                    onRefresh = onRefresh,
                    onOpenApp = onOpenApp
                )
                else -> ApkAnalyzerTab(
                    state = apkState,
                    deepTrackerScan = deepTrackerScan,
                    onAnalyzeApk = onAnalyzeApk,
                    onClear = onClear,
                    onOpenResult = onOpenApkResult
                )
            }
        }
    }
}

@Composable
private fun InstalledAppsTab(
    state: InstalledAppsUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (AppSortMode) -> Unit,
    onHighRiskOnlyChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onOpenApp: (AppAnalysis) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.apps_search_label)) }
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
                label = { Text(stringResource(R.string.apps_filter_high_risk)) }
            )
        }

        if (state.errorMessage != null) {
            EmptyState(title = stringResource(R.string.scan_failed_title), message = state.errorMessage)
            Button(onClick = onRefresh) { Text(stringResource(R.string.action_retry)) }
        }

        val visibleApps = state.visibleApps

        if (state.isLoading && visibleApps.isEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(5) {
                    AppCardSkeleton()
                }
            }
        } else if (visibleApps.isEmpty() && state.errorMessage == null) {
            EmptyState(
                title = stringResource(R.string.empty_no_apps_found_title),
                message = stringResource(R.string.empty_no_apps_found_message)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(items = visibleApps, key = { _, app -> app.id }) { index, analysis ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(analysis.id) {
                        delay((index * 40L).coerceAtMost(400L))
                        visible = true
                    }
                    val alpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(200),
                        label = "item_alpha_$index"
                    )
                    val offsetY by animateDpAsState(
                        targetValue = if (visible) 0.dp else 20.dp,
                        animationSpec = tween(200),
                        label = "item_offset_$index"
                    )
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .offset(y = offsetY)
                            .graphicsLayer { this.alpha = alpha }
                    ) {
                        InstalledAppCard(
                            analysis = analysis,
                            onClick = { onOpenApp(analysis) }
                        )
                    }
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun AppCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.75f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.75f))
                )
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

@Composable
private fun ApkAnalyzerTab(
    state: ApkAnalyzerUiState,
    deepTrackerScan: Boolean,
    onAnalyzeApk: (Uri) -> Unit,
    onClear: () -> Unit,
    onOpenResult: (AppAnalysis) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onAnalyzeApk(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (!state.isAnalyzed && !state.isLoading) {
            if (state.errorMessage == null) {
                Text(
                    text = stringResource(R.string.select_apk_to_inspect),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = { launcher.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream", "application/zip", "*/*")) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_choose_apk))
            }
        }

        if (state.isLoading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.loading_analyzing))
            }
        }

        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            state.errorMessage?.let { message ->
                EmptyState(title = stringResource(R.string.analysis_failed_title), message = message)
            }
        }

        state.analysis?.let { analysis ->
            if (state.isAnalyzed) {
                OutlinedButton(
                    onClick = onClear,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_clear))
                }
                Button(
                    onClick = { onOpenResult(analysis) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_view_apk_details))
                }
            }
            ApkResultCard(analysis = analysis, onOpen = { onOpenResult(analysis) })
            PrivacyScoreCard(analysis = analysis)
        }
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun ApkResultCard(
    analysis: AppAnalysis,
    onOpen: () -> Unit
) {
    val permissionText = pluralStringResource(
        R.plurals.permissions_count,
        analysis.permissionCount,
        analysis.permissionCount
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconBitmap(
                bitmap = analysis.icon,
                fallbackText = analysis.appName,
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    text = stringResource(
                        R.string.apk_version_permissions_summary,
                        analysis.versionName,
                        permissionText
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ScoreBadge(score = analysis.score.value, riskLevel = analysis.score.riskLevel)
        }
    }
}
