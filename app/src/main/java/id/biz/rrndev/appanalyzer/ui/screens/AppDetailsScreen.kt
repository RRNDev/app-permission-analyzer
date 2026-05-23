package id.biz.rrndev.appanalyzer.ui.screens

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import id.biz.rrndev.appanalyzer.BuildConfig
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.PermissionCategory
import id.biz.rrndev.appanalyzer.model.PermissionItem
import id.biz.rrndev.appanalyzer.model.TrackerScanStatus
import id.biz.rrndev.appanalyzer.ui.components.EmptyState
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.PackageAppIcon
import id.biz.rrndev.appanalyzer.ui.components.PrivacyScoreCard
import id.biz.rrndev.appanalyzer.ui.components.SectionTitle
import id.biz.rrndev.appanalyzer.ui.components.WarningPill
import id.biz.rrndev.appanalyzer.util.AnalysisFormatter
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    analysis: AppAnalysis,
    showTechnicalNames: Boolean,
    onOpenPermission: (PermissionItem) -> Unit,
    onOpenScore: (AppAnalysis) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shareText = AnalysisFormatter.toShareText(context, analysis)
    val versionSummary = stringResource(
        R.string.app_version_summary,
        analysis.versionName,
        analysis.versionCode
    )
    val clipboardLabel = stringResource(R.string.clipboard_label_app_analysis)
    val reportCopiedMessage = stringResource(R.string.toast_report_copied)
    val shareChooserTitle = stringResource(R.string.chooser_share_analysis)
    val shareFailedMessage = stringResource(R.string.toast_share_failed)

    var showShareSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 7 blok konten, stagger 70ms per blok
    val blockCount = 7
    val visible = remember(analysis.packageName) { Array(blockCount) { mutableStateOf(false) } }
    LaunchedEffect(analysis.packageName) {
        visible.forEachIndexed { i, state ->
            delay(i * 70L)
            state.value = true
        }
    }

    @Composable
    fun StaggerBlock(index: Int, content: @Composable () -> Unit) {
        val alpha by animateFloatAsState(
            targetValue = if (visible[index].value) 1f else 0f,
            animationSpec = tween(300),
            label = "stagger_alpha_$index"
        )
        val offsetY by animateDpAsState(
            targetValue = if (visible[index].value) 0.dp else 24.dp,
            animationSpec = tween(300),
            label = "stagger_offset_$index"
        )
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .graphicsLayer { this.alpha = alpha }
        ) {
            content()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StaggerBlock(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PackageAppIcon(
                    packageName = analysis.packageName,
                    bitmap = analysis.icon,
                    fallbackText = analysis.appName,
                    modifier = Modifier.size(62.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = analysis.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = analysis.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = versionSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        StaggerBlock(1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onOpenScore(analysis) }) {
                    Text(stringResource(R.string.action_score_details))
                }
                if (analysis.installableApkPath != null) {
                    Button(onClick = { installApk(context, analysis) }) {
                        Text(stringResource(R.string.action_install_apk))
                    }
                }
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        clipboardLabel,
                                        shareText
                                    )
                                )
                            )
                            Toast.makeText(
                                context,
                                reportCopiedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_copy_report))
                }
                OutlinedButton(onClick = { showShareSheet = true }) {
                    Text(stringResource(R.string.action_share))
                }
            }
        }

        StaggerBlock(2) { PrivacyScoreCard(analysis = analysis) }
        StaggerBlock(3) { AnalysisSummary(analysis) }
        StaggerBlock(4) { WarningSection(analysis) }
        StaggerBlock(5) { TrackerSection(analysis) }
        StaggerBlock(6) {
            PermissionSection(
                analysis = analysis,
                showTechnicalNames = showTechnicalNames,
                onOpenPermission = onOpenPermission
            )
        }

        Spacer(Modifier.height(8.dp))
    }

    // Share Options BottomSheet
    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.share_options_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.share_options_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                // Share as Text
                ShareOptionRow(
                    icon = Icons.Outlined.TextFields,
                    label = stringResource(R.string.share_as_text),
                    description = stringResource(R.string.share_as_text_desc),
                    onClick = {
                        showShareSheet = false
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, shareChooserTitle))
                    }
                )

                // Share as TXT file
                ShareOptionRow(
                    icon = Icons.Outlined.Description,
                    label = stringResource(R.string.share_as_txt),
                    description = stringResource(R.string.share_as_txt_desc),
                    onClick = {
                        showShareSheet = false
                        coroutineScope.launch {
                            runCatching {
                                val file = writeReportFile(
                                    context = context,
                                    packageName = analysis.packageName,
                                    extension = "txt",
                                    content = shareText.toByteArray()
                                )
                                shareReportFile(context, file, "text/plain", context.getString(R.string.chooser_share_txt))
                            }.onFailure {
                                Toast.makeText(context, shareFailedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                // Share as PDF
                ShareOptionRow(
                    icon = Icons.Outlined.PictureAsPdf,
                    label = stringResource(R.string.share_as_pdf),
                    description = stringResource(R.string.share_as_pdf_desc),
                    onClick = {
                        showShareSheet = false
                        coroutineScope.launch {
                            runCatching {
                                val pdfBytes = withContext(Dispatchers.Default) {
                                    AnalysisFormatter.toPdfBytes(context, analysis)
                                }
                                val file = writeReportFile(
                                    context = context,
                                    packageName = analysis.packageName,
                                    extension = "pdf",
                                    content = pdfBytes
                                )
                                shareReportFile(context, file, "application/pdf", context.getString(R.string.chooser_share_pdf))
                            }.onFailure {
                                Toast.makeText(context, shareFailedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                // Share as Markdown
                ShareOptionRow(
                    icon = Icons.Outlined.Code,
                    label = stringResource(R.string.share_as_markdown),
                    description = stringResource(R.string.share_as_markdown_desc),
                    onClick = {
                        showShareSheet = false
                        coroutineScope.launch {
                            runCatching {
                                val mdText = withContext(Dispatchers.Default) {
                                    AnalysisFormatter.toMarkdownText(context, analysis)
                                }
                                val file = writeReportFile(
                                    context = context,
                                    packageName = analysis.packageName,
                                    extension = "md",
                                    content = mdText.toByteArray()
                                )
                                shareReportFile(context, file, "text/markdown", context.getString(R.string.chooser_share_markdown))
                            }.onFailure {
                                Toast.makeText(context, shareFailedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ShareOptionRow(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun writeReportFile(
    context: Context,
    packageName: String,
    extension: String,
    content: ByteArray
): File {
    val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
    val safePackage = packageName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    return File(reportsDir, "report_${safePackage}.$extension").also {
        it.writeBytes(content)
    }
}

private fun shareReportFile(
    context: Context,
    file: File,
    mimeType: String,
    chooserTitle: String
) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

private fun installApk(context: Context, analysis: AppAnalysis) {
    val apkPath = analysis.installableApkPath ?: return
    val apkFile = File(apkPath)
    if (!apkFile.exists()) {
        Toast.makeText(
            context,
            context.getString(R.string.toast_apk_file_unavailable),
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val apkUri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        apkFile
    )
    @Suppress("DEPRECATION")
    val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
        setDataAndType(apkUri, APK_MIME_TYPE)
        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    runCatching {
        context.startActivity(installIntent)
    }.onFailure {
        Toast.makeText(
            context,
            context.getString(R.string.toast_no_apk_installer),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun AnalysisSummary(analysis: AppAnalysis) {
    val permissionText = pluralStringResource(
        R.plurals.requested_permissions_count,
        analysis.permissionCount,
        analysis.permissionCount
    )
    val trackerText = if (analysis.trackerScanStatus == TrackerScanStatus.Scanned) {
        pluralStringResource(
            R.plurals.tracker_sdks_count,
            analysis.trackerCount,
            analysis.trackerCount
        )
    } else {
        stringResource(R.string.analysis_summary_tracker_scan_running)
    }
    val warningText = pluralStringResource(
        R.plurals.warning_signals_count,
        analysis.warnings.size,
        analysis.warnings.size
    )
    val summaryCounts = stringResource(
        R.string.analysis_summary_counts_format,
        permissionText,
        trackerText,
        warningText
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(title = stringResource(R.string.privacy_summary_title))
            Text(
                text = analysis.score.summary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summaryCounts,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WarningSection(analysis: AppAnalysis) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(
            title = stringResource(R.string.risk_analysis_title),
            subtitle = stringResource(R.string.risk_analysis_subtitle)
        )
        if (analysis.warnings.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_risky_combinations_title),
                message = stringResource(R.string.no_risky_combinations_message)
            )
        } else {
            analysis.warnings.forEach { warning ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = warning.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            WarningPill(level = warning.level)
                        }
                        Text(
                            text = warning.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackerSection(analysis: AppAnalysis) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(
            title = stringResource(R.string.trackers_title),
            subtitle = stringResource(R.string.trackers_subtitle)
        )
        if (analysis.trackers.isEmpty()) {
            if (analysis.trackerScanStatus == TrackerScanStatus.Scanned) {
                EmptyState(
                    title = stringResource(R.string.no_common_trackers_title),
                    message = stringResource(R.string.no_common_trackers_message)
                )
            } else {
                EmptyState(
                    title = stringResource(R.string.tracker_scan_running_title),
                    message = stringResource(R.string.tracker_scan_running_message)
                )
            }
        } else {
            analysis.trackers.forEach { tracker ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(text = tracker.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = tracker.privacyImpact,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionSection(
    analysis: AppAnalysis,
    showTechnicalNames: Boolean,
    onOpenPermission: (PermissionItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(
            title = stringResource(R.string.permissions_title),
            subtitle = stringResource(R.string.permissions_subtitle)
        )
        if (analysis.permissions.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_requested_permissions_title),
                message = stringResource(R.string.no_requested_permissions_message)
            )
        } else {
            PermissionCategory.entries.forEach { category ->
                val items = analysis.permissions.filter { it.category == category }
                if (items.isNotEmpty()) {
                    PermissionGroup(
                        category = category,
                        permissions = items,
                        showTechnicalNames = showTechnicalNames,
                        onOpenPermission = onOpenPermission
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionGroup(
    category: PermissionCategory,
    permissions: List<PermissionItem>,
    showTechnicalNames: Boolean,
    onOpenPermission: (PermissionItem) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = permissions.size.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(category.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            permissions.forEach { permission ->
                PermissionRow(
                    permission = permission,
                    showTechnicalName = showTechnicalNames,
                    onClick = { onOpenPermission(permission) }
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    permission: PermissionItem,
    showTechnicalName: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = permission.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (showTechnicalName) permission.name else permission.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        WarningPill(level = permission.warningLevel)
    }
}

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
