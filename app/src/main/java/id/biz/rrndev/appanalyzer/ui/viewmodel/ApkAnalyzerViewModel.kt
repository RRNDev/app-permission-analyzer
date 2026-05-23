package id.biz.rrndev.appanalyzer.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.data.ApkAnalyzerRepository
import id.biz.rrndev.appanalyzer.model.ApkAnalyzerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApkAnalyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ApkAnalyzerRepository(application)
    private val _uiState = MutableStateFlow(ApkAnalyzerUiState())
    val uiState: StateFlow<ApkAnalyzerUiState> = _uiState.asStateFlow()

    fun analyze(uri: Uri, deepTrackerScan: Boolean) {
        _uiState.update { it.copy(isLoading = true, analysis = null, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                repository.analyzeApk(uri, deepTrackerScan)
            }.onSuccess { analysis ->
                _uiState.update { it.copy(isLoading = false, analysis = analysis, errorMessage = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: getApplication<Application>().getString(R.string.error_analyze_selected_apk)
                    )
                }
            }
        }
    }

    fun clearResult() {
        repository.clearCachedApks()
        _uiState.value = ApkAnalyzerUiState()
    }
}
