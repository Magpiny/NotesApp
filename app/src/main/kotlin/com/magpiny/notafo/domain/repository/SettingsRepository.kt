package com.magpiny.notafo.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface defining user preferences repository operations.
 */
interface SettingsRepository {
    /** Flow representing the current user theme preference (e.g., System, Light, Dark). */
    val themeMode: Flow<String>

    /** Flow representing whether Material You dynamic colors are enabled. */
    val dynamicColor: Flow<Boolean>

    /** Flow representing the layout style preference: true for Grid, false for List. */
    val isGridView: Flow<Boolean>

    /** Flow representing the current app language (e.g., "en", "es"). */
    val language: Flow<String>

    /** Flow representing the current font family (e.g., "Sans", "Serif", "Mono"). */
    val fontFamily: Flow<String>

    /** Focus timer durations in minutes. */
    val focusDuration: Flow<Int>
    val shortBreakDuration: Flow<Int>
    val longBreakDuration: Flow<Int>

    /** Updates the theme mode. */
    suspend fun setThemeMode(mode: String)

    /** Toggles the dynamic color preference. */
    suspend fun setDynamicColor(enabled: Boolean)

    /** Toggles the layout view preference. */
    suspend fun setGridView(isGrid: Boolean)

    /** Updates the app language. */
    suspend fun setLanguage(languageCode: String)

    /** Updates the font family. */
    suspend fun setFontFamily(fontFamily: String)

    /** Updates focus durations. */
    suspend fun setFocusDuration(minutes: Int)
    suspend fun setShortBreakDuration(minutes: Int)
    suspend fun setLongBreakDuration(minutes: Int)
}
