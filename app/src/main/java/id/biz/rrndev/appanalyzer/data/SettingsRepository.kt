package id.biz.rrndev.appanalyzer.data

import android.content.Context
import id.biz.rrndev.appanalyzer.model.SettingsUiState

class SettingsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("privacy_settings", Context.MODE_PRIVATE)

    fun load(): SettingsUiState {
        return SettingsUiState(
            includeSystemApps = preferences.getBoolean(KEY_INCLUDE_SYSTEM_APPS, false),
            scanTrackersDeeply = preferences.getBoolean(KEY_SCAN_TRACKERS_DEEPLY, true),
            showTechnicalPermissionNames = preferences.getBoolean(KEY_SHOW_TECHNICAL_NAMES, false),
            useDynamicColor = preferences.getBoolean(KEY_DYNAMIC_COLOR, false)
        )
    }

    fun save(state: SettingsUiState) {
        preferences.edit()
            .putBoolean(KEY_INCLUDE_SYSTEM_APPS, state.includeSystemApps)
            .putBoolean(KEY_SCAN_TRACKERS_DEEPLY, state.scanTrackersDeeply)
            .putBoolean(KEY_SHOW_TECHNICAL_NAMES, state.showTechnicalPermissionNames)
            .putBoolean(KEY_DYNAMIC_COLOR, state.useDynamicColor)
            .apply()
    }

    private companion object {
        const val KEY_INCLUDE_SYSTEM_APPS = "include_system_apps"
        const val KEY_SCAN_TRACKERS_DEEPLY = "scan_trackers_deeply"
        const val KEY_SHOW_TECHNICAL_NAMES = "show_technical_names"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }
}
