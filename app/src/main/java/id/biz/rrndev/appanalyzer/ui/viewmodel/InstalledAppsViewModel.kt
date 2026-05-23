package id.biz.rrndev.appanalyzer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.data.InstalledAppsRepository
import id.biz.rrndev.appanalyzer.model.AppSortMode
import id.biz.rrndev.appanalyzer.model.InstalledAppsUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstalledAppsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InstalledAppsRepository(application)
    private val _uiState = MutableStateFlow(InstalledAppsUiState(isLoading = true))
    val uiState: StateFlow<InstalledAppsUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null
    private var enrichJob: Job? = null
    private var lastIncludeSystemApps = false
    private var hasScanned = false

    fun refresh(includeSystemApps: Boolean = lastIncludeSystemApps) {
        if (hasScanned && includeSystemApps == lastIncludeSystemApps && _uiState.value.apps.isNotEmpty()) return
        lastIncludeSystemApps = includeSystemApps
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.scanInstalledApps(includeSystemApps = includeSystemApps)
            }.onSuccess { apps ->
                hasScanned = true
                _uiState.update { it.copy(isLoading = false, apps = apps, errorMessage = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: getApplication<Application>().getString(R.string.error_scan_installed_apps)
                    )
                }
            }
        }
    }

    fun forceRefresh(includeSystemApps: Boolean = lastIncludeSystemApps) {
        hasScanned = false
        refresh(includeSystemApps = includeSystemApps)
    }

    fun enrichApp(packageName: String, deepTrackerScan: Boolean) {
        enrichJob?.cancel()
        enrichJob = viewModelScope.launch {
            runCatching {
                repository.enrichInstalledApp(packageName, deepTrackerScan)
            }.getOrNull()?.let { enriched ->
                _uiState.update { state ->
                    state.copy(
                        apps = state.apps.map { current ->
                            if (current.packageName == enriched.packageName) enriched else current
                        }
                    )
                }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun updateSortMode(sortMode: AppSortMode) {
        _uiState.update { it.copy(sortMode = sortMode) }
    }

    fun updateHighRiskOnly(enabled: Boolean) {
        _uiState.update { it.copy(highRiskOnly = enabled) }
    }
}
