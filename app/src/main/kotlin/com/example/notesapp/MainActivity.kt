package com.example.notesapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.core.BiometricAuthManager
import com.example.notesapp.domain.repository.SettingsRepository
import com.example.notesapp.presentation.MainScreen
import com.example.notesapp.presentation.theme.NotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var biometricManager: BiometricAuthManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = "System")
            val dynamicColor by settingsRepository.dynamicColor.collectAsStateWithLifecycle(initialValue = true)
            val language by settingsRepository.language.collectAsStateWithLifecycle(initialValue = "en")
            val fontFamily by settingsRepository.fontFamily.collectAsStateWithLifecycle(initialValue = "Sans")

            // Guarded locale update to prevent infinite recreation loop
            LaunchedEffect(language) {
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                if (currentLocales.toLanguageTags() != language) {
                    val appLocales = LocaleListCompat.forLanguageTags(language)
                    AppCompatDelegate.setApplicationLocales(appLocales)
                }
            }

            val useDarkTheme = when (themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            NotesAppTheme(
                useDarkTheme = useDarkTheme,
                useDynamicColor = dynamicColor,
                fontFamilyName = fontFamily
            ) {
                MainScreen(biometricManager = biometricManager)
            }
        }
    }
}
