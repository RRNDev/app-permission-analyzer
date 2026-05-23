package id.biz.rrndev.appanalyzer.model

import android.graphics.Bitmap
import androidx.annotation.StringRes
import id.biz.rrndev.appanalyzer.R

enum class PermissionCategory(@param:StringRes val labelRes: Int) {
    Privacy(R.string.permission_category_privacy),
    DeviceAccess(R.string.permission_category_device_access),
    Communication(R.string.permission_category_communication),
    Storage(R.string.permission_category_storage),
    Tracking(R.string.permission_category_tracking),
    BackgroundAccess(R.string.permission_category_background_access),
    Other(R.string.permission_category_other)
}

enum class WarningLevel(@param:StringRes val labelRes: Int, val weight: Int) {
    Low(R.string.warning_level_low, 2),
    Medium(R.string.warning_level_medium, 6),
    High(R.string.warning_level_high, 11),
    Critical(R.string.warning_level_critical, 16)
}

enum class RiskLevel(@param:StringRes val labelRes: Int) {
    High(R.string.risk_level_high),
    Medium(R.string.risk_level_medium),
    Safer(R.string.risk_level_safer)
}

enum class AnalysisSource {
    InstalledApp,
    ApkFile
}

enum class TrackerScanStatus {
    NotScanned,
    Scanned
}

data class PermissionItem(
    val name: String,
    val displayName: String,
    val category: PermissionCategory,
    val explanation: String,
    val privacyImpact: String,
    val legitimateUseCase: String,
    val warningLevel: WarningLevel
)

data class TrackerInfo(
    val name: String,
    val packageHints: List<String>,
    val privacyImpact: String
)

data class RiskWarning(
    val title: String,
    val description: String,
    val level: WarningLevel
)

data class PrivacyScore(
    val value: Int,
    val riskLevel: RiskLevel,
    val summary: String,
    val factors: List<String>
)

data class AppAnalysis(
    val id: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Bitmap?,
    val source: AnalysisSource,
    val permissions: List<PermissionItem>,
    val trackers: List<TrackerInfo>,
    val trackerScanStatus: TrackerScanStatus,
    val warnings: List<RiskWarning>,
    val score: PrivacyScore,
    val isSystemApp: Boolean = false,
    val installableApkPath: String? = null
) {
    val permissionCount: Int get() = permissions.size
    val trackerCount: Int get() = trackers.size
}

enum class AppSortMode(@param:StringRes val labelRes: Int) {
    LowestScore(R.string.sort_mode_lowest_score),
    PermissionCount(R.string.sort_mode_permission_count),
    AppName(R.string.sort_mode_app_name)
}
