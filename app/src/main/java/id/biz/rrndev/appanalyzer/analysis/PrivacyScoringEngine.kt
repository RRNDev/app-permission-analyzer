package id.biz.rrndev.appanalyzer.analysis

import android.Manifest
import android.content.Context
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.PermissionCategory
import id.biz.rrndev.appanalyzer.model.PermissionItem
import id.biz.rrndev.appanalyzer.model.PrivacyScore
import id.biz.rrndev.appanalyzer.model.RiskLevel
import id.biz.rrndev.appanalyzer.model.RiskWarning
import id.biz.rrndev.appanalyzer.model.TrackerInfo
import id.biz.rrndev.appanalyzer.model.WarningLevel
import kotlin.math.min

object PrivacyScoringEngine {
    fun score(
        context: Context,
        permissions: List<PermissionItem>,
        trackers: List<TrackerInfo>,
        warnings: List<RiskWarning>,
        usesAccessibilityService: Boolean
    ): PrivacyScore {
        var score = 100
        val factors = mutableListOf<String>()

        val sensitivePermissions = permissions.filter {
            it.warningLevel == WarningLevel.High || it.warningLevel == WarningLevel.Critical
        }
        val permissionPenalty = sensitivePermissions.sumOf { it.warningLevel.weight }.coerceAtMost(48)
        if (permissionPenalty > 0) {
            score -= permissionPenalty
            factors += context.resources.getQuantityString(
                R.plurals.score_factor_sensitive_permissions,
                sensitivePermissions.size,
                sensitivePermissions.size
            )
        }

        val backgroundPenalty = permissions.count { it.category == PermissionCategory.BackgroundAccess }
            .let { min(it * 5, 14) }
        if (backgroundPenalty > 0) {
            score -= backgroundPenalty
            factors += context.getString(R.string.score_factor_background_access)
        }

        val trackerPenalty = min(trackers.size * 7, 28)
        if (trackerPenalty > 0) {
            score -= trackerPenalty
            factors += context.resources.getQuantityString(
                R.plurals.score_factor_trackers,
                trackers.size,
                trackers.size
            )
        }

        if (usesAccessibilityService) {
            score -= 18
            factors += context.getString(R.string.score_factor_accessibility_service)
        }

        if (permissions.any { it.name == Manifest.permission.SYSTEM_ALERT_WINDOW }) {
            score -= 14
            factors += context.getString(R.string.score_factor_overlay_access)
        }

        val communicationPenalty = permissions.count {
            it.name.contains("SMS", ignoreCase = true) ||
                it.name.contains("CALL_LOG", ignoreCase = true) ||
                it.name == Manifest.permission.CALL_PHONE
        }.let { min(it * 8, 24) }
        if (communicationPenalty > 0) {
            score -= communicationPenalty
            factors += context.getString(R.string.score_factor_communication_access)
        }

        val warningPenalty = warnings.sumOf { warning ->
            when (warning.level) {
                WarningLevel.Critical -> 12
                WarningLevel.High -> 8
                WarningLevel.Medium -> 4
                WarningLevel.Low -> 2
            }
        }.coerceAtMost(24)
        if (warningPenalty > 0) {
            score -= warningPenalty
            factors += context.getString(R.string.score_factor_suspicious_combinations)
        }

        score = score.coerceIn(0, 100)
        val riskLevel = when (score) {
            in 0..30 -> RiskLevel.High
            in 31..70 -> RiskLevel.Medium
            else -> RiskLevel.Safer
        }

        val summary = buildSummary(context, score, permissions, trackers, warnings)
        val finalFactors = if (factors.isEmpty()) {
            listOf(context.getString(R.string.score_factor_no_risks))
        } else {
            factors
        }

        return PrivacyScore(
            value = score,
            riskLevel = riskLevel,
            summary = summary,
            factors = finalFactors
        )
    }

    private fun buildSummary(
        context: Context,
        score: Int,
        permissions: List<PermissionItem>,
        trackers: List<TrackerInfo>,
        warnings: List<RiskWarning>
    ): String {
        val sensitiveCount = permissions.count {
            it.warningLevel == WarningLevel.High || it.warningLevel == WarningLevel.Critical
        }
        return when {
            score <= 30 -> context.getString(R.string.score_summary_high_risk)
            warnings.isNotEmpty() -> context.getString(R.string.score_summary_potential_concern)
            trackers.size >= 2 -> context.getString(R.string.score_summary_multiple_trackers)
            sensitiveCount == 0 && trackers.isEmpty() -> context.getString(R.string.score_summary_no_sensitive_permissions)
            sensitiveCount <= 1 && trackers.isEmpty() -> context.getString(R.string.score_summary_minimal_permissions)
            else -> context.getString(R.string.score_summary_review_sensitive_permissions)
        }
    }
}
