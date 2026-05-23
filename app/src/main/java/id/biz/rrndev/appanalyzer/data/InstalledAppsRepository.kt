package id.biz.rrndev.appanalyzer.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import id.biz.rrndev.appanalyzer.model.AnalysisSource
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.util.PackageManagerCompat.getPackageArchiveInfoCompat
import id.biz.rrndev.appanalyzer.util.PackageManagerCompat.getInstalledPackagesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InstalledAppsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val builder = AppAnalysisBuilder(appContext)

    suspend fun scanInstalledApps(
        includeSystemApps: Boolean
    ): List<AppAnalysis> = withContext(Dispatchers.IO) {
        val flags = PackageManager.GET_PERMISSIONS or
            PackageManager.GET_SERVICES

        packageManager.getInstalledPackagesCompat(flags)
            .asSequence()
            .filter { packageInfo ->
                val appInfo = packageInfo.applicationInfo
                includeSystemApps || appInfo == null ||
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .mapNotNull { packageInfo ->
                builder.build(
                    packageInfo = packageInfo,
                    source = AnalysisSource.InstalledApp,
                    apkFile = null,
                    scanTrackers = false,
                    deepTrackerScan = false,
                    loadIcon = false
                )
            }
            .sortedBy { it.appName.lowercase() }
            .toList()
    }

    suspend fun enrichInstalledApp(
        packageName: String,
        deepTrackerScan: Boolean
    ): AppAnalysis? = withContext(Dispatchers.IO) {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val sourceFile = File(applicationInfo.sourceDir)
        val flags = PackageManager.GET_PERMISSIONS or
            PackageManager.GET_SERVICES or
            PackageManager.GET_META_DATA
        val packageInfo = packageManager.getPackageArchiveInfoCompat(sourceFile.absolutePath, flags)
            ?: return@withContext null
        packageInfo.applicationInfo = applicationInfo
        builder.build(
            packageInfo = packageInfo,
            source = AnalysisSource.InstalledApp,
            apkFile = sourceFile,
            scanTrackers = true,
            deepTrackerScan = deepTrackerScan,
            loadIcon = false
        )
    }
}
