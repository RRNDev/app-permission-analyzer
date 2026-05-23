package id.biz.rrndev.appanalyzer.data

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.analysis.DangerousCombinationDetector
import id.biz.rrndev.appanalyzer.analysis.PermissionCatalog
import id.biz.rrndev.appanalyzer.analysis.PrivacyScoringEngine
import id.biz.rrndev.appanalyzer.analysis.TrackerDetector
import id.biz.rrndev.appanalyzer.model.AnalysisSource
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.TrackerScanStatus
import id.biz.rrndev.appanalyzer.util.DrawableTools.toAppIconBitmap
import id.biz.rrndev.appanalyzer.util.PackageManagerCompat.versionCodeCompat
import java.io.File

class AppAnalysisBuilder(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    private val iconDensity: Float = context.resources.displayMetrics.density

    fun build(
        packageInfo: PackageInfo,
        source: AnalysisSource,
        apkFile: File?,
        scanTrackers: Boolean,
        deepTrackerScan: Boolean,
        loadIcon: Boolean
    ): AppAnalysis? {
        val applicationInfo = packageInfo.applicationInfo ?: return null
        if (source == AnalysisSource.ApkFile && apkFile != null) {
            applicationInfo.sourceDir = apkFile.absolutePath
            applicationInfo.publicSourceDir = apkFile.absolutePath
        }

        val appName = loadAppName(applicationInfo)
        val permissions = packageInfo.requestedPermissions
            ?.distinct()
            ?.sorted()
            ?.map { PermissionCatalog.describe(context, it) }
            .orEmpty()

        val usesAccessibilityService = packageInfo.services?.any {
            it.permission == Manifest.permission.BIND_ACCESSIBILITY_SERVICE
        } == true

        val trackers = apkFile?.takeIf { scanTrackers && it.exists() }
            ?.let { TrackerDetector.detectFromApk(it, deepTrackerScan, context) }
            .orEmpty()

        val warnings = DangerousCombinationDetector.detect(
            context = context,
            appName = appName,
            permissions = permissions,
            trackers = trackers,
            usesAccessibilityService = usesAccessibilityService
        )
        val score = PrivacyScoringEngine.score(
            context = context,
            permissions = permissions,
            trackers = trackers,
            warnings = warnings,
            usesAccessibilityService = usesAccessibilityService
        )

        val isSystem = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        return AppAnalysis(
            id = "${source.name}:${packageInfo.packageName}",
            appName = appName,
            packageName = packageInfo.packageName ?: context.getString(R.string.unknown_package_name),
            versionName = packageInfo.versionName ?: context.getString(R.string.unknown_version),
            versionCode = packageInfo.versionCodeCompat(),
            icon = if (loadIcon) {
                runCatching { applicationInfo.loadIcon(packageManager).toAppIconBitmap(iconDensity) }.getOrNull()
            } else {
                null
            },
            source = source,
            permissions = permissions,
            trackers = trackers,
            trackerScanStatus = if (scanTrackers) {
                TrackerScanStatus.Scanned
            } else {
                TrackerScanStatus.NotScanned
            },
            warnings = warnings,
            score = score,
            isSystemApp = isSystem,
            installableApkPath = if (source == AnalysisSource.ApkFile) {
                apkFile?.absolutePath
            } else {
                null
            }
        )
    }

    private fun loadAppName(applicationInfo: ApplicationInfo): String {
        return runCatching { applicationInfo.loadLabel(packageManager).toString() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: applicationInfo.packageName
            ?: context.getString(R.string.unknown_app_name)
    }
}
