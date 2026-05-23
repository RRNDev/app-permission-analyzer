package id.biz.rrndev.appanalyzer.model

data class InstalledAppsUiState(
    val isLoading: Boolean = false,
    val apps: List<AppAnalysis> = emptyList(),
    val query: String = "",
    val sortMode: AppSortMode = AppSortMode.LowestScore,
    val highRiskOnly: Boolean = false,
    val errorMessage: String? = null
) {
    val visibleApps: List<AppAnalysis>
        get() {
            val normalizedQuery = query.trim().lowercase()
            return apps.asSequence()
                .filter { app ->
                    normalizedQuery.isEmpty() ||
                        app.appName.lowercase().contains(normalizedQuery) ||
                        app.packageName.lowercase().contains(normalizedQuery)
                }
                .filter { app -> !highRiskOnly || app.score.riskLevel == RiskLevel.High }
                .let { sequence ->
                    when (sortMode) {
                        AppSortMode.LowestScore -> sequence.sortedWith(
                            compareBy<AppAnalysis> { it.score.value }
                                .thenByDescending { it.permissionCount }
                                .thenBy { it.appName.lowercase() }
                        )
                        AppSortMode.PermissionCount -> sequence.sortedWith(
                            compareByDescending<AppAnalysis> { it.permissionCount }
                                .thenBy { it.score.value }
                                .thenBy { it.appName.lowercase() }
                        )
                        AppSortMode.AppName -> sequence.sortedBy { it.appName.lowercase() }
                    }
                }
                .toList()
        }

    val highRiskCount: Int get() = apps.count { it.score.riskLevel == RiskLevel.High }
    val mediumRiskCount: Int get() = apps.count { it.score.riskLevel == RiskLevel.Medium }
    val saferCount: Int get() = apps.count { it.score.riskLevel == RiskLevel.Safer }
}

data class ApkAnalyzerUiState(
    val isLoading: Boolean = false,
    val analysis: AppAnalysis? = null,
    val errorMessage: String? = null
) {
    val isAnalyzed: Boolean get() = analysis != null
}

data class SettingsUiState(
    val includeSystemApps: Boolean = false,
    val scanTrackersDeeply: Boolean = true,
    val showTechnicalPermissionNames: Boolean = false,
    val useDynamicColor: Boolean = true
)

