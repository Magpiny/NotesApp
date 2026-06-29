package com.magpiny.notafo.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magpiny.notafo.domain.repository.SettingsRepository
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
    val fontFamily: String = "Sans",
    val focusDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            settingsRepository.themeMode,
            settingsRepository.dynamicColor,
            settingsRepository.isGridView,
            settingsRepository.language,
            settingsRepository.fontFamily
        ) { theme, dynamic, grid, lang, font ->
            listOf(theme, dynamic, grid, lang, font)
        },
        settingsRepository.focusDuration,
        settingsRepository.shortBreakDuration,
        settingsRepository.longBreakDuration
    ) { basic, focus, short, long ->
        SettingsUiState(
            themeMode = basic[0] as String,
            dynamicColor = basic[1] as Boolean,
            isGridView = basic[2] as Boolean,
            language = basic[3] as String,
            fontFamily = basic[4] as String,
            focusDuration = focus,
            shortBreakDuration = short,
            longBreakDuration = long
        )
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
            val appLocales = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocales)
        }
    }

    fun setFontFamily(fontFamily: String) {
        viewModelScope.launch {
            settingsRepository.setFontFamily(fontFamily)
        }
    }

    fun setFocusDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setFocusDuration(minutes)
        }
    }

    fun setShortBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setShortBreakDuration(minutes)
        }
    }

    fun setLongBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setLongBreakDuration(minutes)
        }
    }
}
