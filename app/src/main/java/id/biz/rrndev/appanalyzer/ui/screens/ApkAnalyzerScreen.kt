package id.biz.rrndev.appanalyzer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import id.biz.rrndev.appanalyzer.model.ApkAnalyzerUiState
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.ui.components.AppIconBitmap
import id.biz.rrndev.appanalyzer.ui.components.EmptyState
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.PrivacyScoreCard
import id.biz.rrndev.appanalyzer.ui.components.ScoreBadge
import id.biz.rrndev.appanalyzer.ui.components.SectionTitle

@Composable
fun ApkAnalyzerScreen(
    state: ApkAnalyzerUiState,
    deepTrackerScan: Boolean,
    onAnalyzeApk: (Uri) -> Unit,
    onClear: () -> Unit,
    onOpenResult: (AppAnalysis) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onAnalyzeApk(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            title = stringResource(R.string.apk_analyzer_title),
            subtitle = stringResource(R.string.apk_analyzer_subtitle)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.apk_private_local_analysis_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.apk_private_local_analysis_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    launcher.launch(
                        arrayOf(
                            "application/vnd.android.package-archive",
                            "application/octet-stream",
                            "application/zip",
                            "*/*"
                        )
                    )
                },
                enabled = !state.isLoading
            ) {
                Text(stringResource(R.string.action_select_apk))
            }
            if (state.analysis != null) {
                OutlinedButton(onClick = onClear, enabled = !state.isLoading) {
                    Text(stringResource(R.string.action_clear))
                }
            }
        }

        Text(
            text = if (deepTrackerScan) {
                stringResource(R.string.tracker_scan_enabled)
            } else {
                stringResource(R.string.tracker_scan_disabled)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.loading_analyzing_apk))
            }
        }

        if (state.errorMessage != null) {
            EmptyState(title = stringResource(R.string.apk_analysis_failed_title), message = state.errorMessage)
        }

        state.analysis?.let { analysis ->
            ApkResultCard(analysis = analysis, onOpen = { onOpenResult(analysis) })
            PrivacyScoreCard(analysis = analysis)
        }
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
                        R.string.version_permissions_summary,
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
