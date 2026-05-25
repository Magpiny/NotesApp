package com.example.notesapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "System",
    val dynamicColor: Boolean = true,
    val isGridView: Boolean = true,
    val language: String = "en",
    val fontFamily: String = "Sans"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.themeMode,
        settingsRepository.dynamicColor,
        settingsRepository.isGridView,
        settingsRepository.language,
        settingsRepository.fontFamily
    ) { theme, dynamic, grid, lang, font ->
        SettingsUiState(theme, dynamic, grid, lang, font)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColor(enabled)
        }
    }

    fun setGridView(isGrid: Boolean) {
        viewModelScope.launch {
            settingsRepository.setGridView(isGrid)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(languageCode)
        }
    }

    fun setFontFamily(fontFamily: String) {
        viewModelScope.launch {
            settingsRepository.setFontFamily(fontFamily)
        }
    }
}
