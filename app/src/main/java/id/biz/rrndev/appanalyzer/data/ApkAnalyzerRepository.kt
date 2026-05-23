package id.biz.rrndev.appanalyzer.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.AnalysisSource
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.util.PackageManagerCompat.getPackageArchiveInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ApkAnalyzerRepository(context: Context) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val builder = AppAnalysisBuilder(appContext)

    suspend fun analyzeApk(uri: Uri, deepTrackerScan: Boolean): AppAnalysis = withContext(Dispatchers.IO) {
        val apkFile = copyApkToCache(uri)
        val flags = PackageManager.GET_PERMISSIONS or
            PackageManager.GET_SERVICES or
            PackageManager.GET_META_DATA

        val packageInfo = packageManager.getPackageArchiveInfoCompat(apkFile.absolutePath, flags)
            ?: run {
                apkFile.delete()
                error(appContext.getString(R.string.error_invalid_apk_file))
            }

        builder.build(
            packageInfo = packageInfo,
            source = AnalysisSource.ApkFile,
            apkFile = apkFile,
            scanTrackers = true,
            deepTrackerScan = deepTrackerScan,
            loadIcon = true
        ) ?: run {
            apkFile.delete()
            error(appContext.getString(R.string.error_apk_manifest_unparsed))
        }
    }

    fun clearCachedApks() {
        File(appContext.cacheDir, APK_CACHE_DIRECTORY).deleteRecursively()
    }

    private fun copyApkToCache(uri: Uri): File {
        val directory = File(appContext.cacheDir, APK_CACHE_DIRECTORY).apply {
            deleteRecursively()
            mkdirs()
        }
        val displayName = queryDisplayName(uri)
            ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
            ?.take(80)
            ?: "selected.apk"
        val target = File(directory, "${System.currentTimeMillis()}-$displayName")

        // Storage Access Framework gives us a stream, not a filesystem path. Copying to cache lets PackageManager parse the manifest locally.
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var copied = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    copied += read
                    if (copied > MAX_APK_SIZE_BYTES) {
                        target.delete()
                        error(appContext.getString(R.string.error_apk_too_large))
                    }
                    output.write(buffer, 0, read)
                }
            }
        } ?: error(appContext.getString(R.string.error_open_selected_apk))

        return target
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    } else {
                        null
                    }
                }
        }.getOrNull()
    }

    private companion object {
        const val APK_CACHE_DIRECTORY = "apk-analysis"
        const val MAX_APK_SIZE_BYTES = 500L * 1024L * 1024L
    }
}
