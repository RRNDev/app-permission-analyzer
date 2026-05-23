package id.biz.rrndev.appanalyzer.analysis

import android.Manifest
import android.content.Context
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.PermissionItem
import id.biz.rrndev.appanalyzer.model.RiskWarning
import id.biz.rrndev.appanalyzer.model.TrackerInfo
import id.biz.rrndev.appanalyzer.model.WarningLevel

object DangerousCombinationDetector {
    fun detect(
        context: Context,
        appName: String,
        permissions: List<PermissionItem>,
        trackers: List<TrackerInfo>,
        usesAccessibilityService: Boolean
    ): List<RiskWarning> {
        val names = permissions.map { it.name }.toSet()
        val warnings = mutableListOf<RiskWarning>()

        if (usesAccessibilityService && Manifest.permission.SYSTEM_ALERT_WINDOW in names) {
            warnings += RiskWarning(
                title = context.getString(R.string.warning_accessibility_overlay_title),
                description = context.getString(R.string.warning_accessibility_overlay_description),
                level = WarningLevel.Critical
            )
        }

        if (names.any { it.contains("SMS", ignoreCase = true) } &&
            names.any { it.contains("CONTACTS", ignoreCase = true) }
        ) {
            warnings += RiskWarning(
                title = context.getString(R.string.warning_sms_contacts_title),
                description = context.getString(R.string.warning_sms_contacts_description),
                level = WarningLevel.Critical
            )
        }

        if (Manifest.permission.ACCESS_BACKGROUND_LOCATION in names && trackers.isNotEmpty()) {
            warnings += RiskWarning(
                title = context.getString(R.string.warning_background_location_trackers_title),
                description = context.getString(R.string.warning_background_location_trackers_description),
                level = WarningLevel.Critical
            )
        }

        if (Manifest.permission.SYSTEM_ALERT_WINDOW in names &&
            names.any { it.contains("SMS", ignoreCase = true) || it.contains("CALL_LOG", ignoreCase = true) }
        ) {
            warnings += RiskWarning(
                title = context.getString(R.string.warning_overlay_sensitive_communication_title),
                description = context.getString(R.string.warning_overlay_sensitive_communication_description),
                level = WarningLevel.High
            )
        }

        if (isSimpleUtilityName(appName) && hasClearlyUnrelatedSensitivePermission(names)) {
            warnings += RiskWarning(
                title = context.getString(R.string.warning_permissions_mismatch_title),
                description = context.getString(R.string.warning_permissions_mismatch_description),
                level = WarningLevel.High
            )
        }

        val sensitiveCount = permissions.count { it.warningLevel == WarningLevel.High || it.warningLevel == WarningLevel.Critical }
        if (sensitiveCount >= 5) {
            warnings += RiskWarning(
                title = context.getString(R.string.warning_many_sensitive_permissions_title),
                description = context.getString(R.string.warning_many_sensitive_permissions_description),
                level = WarningLevel.High
            )
        }

        return warnings.distinctBy { it.title }
    }

    private fun isSimpleUtilityName(appName: String): Boolean {
        val normalized = appName.lowercase()
        return listOf("flashlight", "torch", "calculator", "timer", "compass", "notepad", "ruler")
            .any { normalized.contains(it) }
    }

    private fun hasClearlyUnrelatedSensitivePermission(names: Set<String>): Boolean {
        return names.any { permission ->
            permission.contains("CONTACTS", ignoreCase = true) ||
                permission.contains("SMS", ignoreCase = true) ||
                permission.contains("CALL_LOG", ignoreCase = true) ||
                permission == Manifest.permission.ACCESS_FINE_LOCATION ||
                permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
    }
}
