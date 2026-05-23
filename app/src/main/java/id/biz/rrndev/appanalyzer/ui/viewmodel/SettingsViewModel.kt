package id.biz.rrndev.appanalyzer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import id.biz.rrndev.appanalyzer.data.SettingsRepository
import id.biz.rrndev.appanalyzer.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val _uiState = MutableStateFlow(repository.load())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setIncludeSystemApps(enabled: Boolean) = update { it.copy(includeSystemApps = enabled) }

    fun setDeepTrackerScan(enabled: Boolean) = update { it.copy(scanTrackersDeeply = enabled) }

    fun setShowTechnicalPermissionNames(enabled: Boolean) = update {
        it.copy(showTechnicalPermissionNames = enabled)
    }

    fun setUseDynamicColor(enabled: Boolean) = update { it.copy(useDynamicColor = enabled) }

    private fun update(transform: (SettingsUiState) -> SettingsUiState) {
        _uiState.update { current ->
            transform(current).also(repository::save)
        }
    }
}

