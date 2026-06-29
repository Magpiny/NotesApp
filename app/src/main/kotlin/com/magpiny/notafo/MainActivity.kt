package com.magpiny.notafo

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magpiny.notafo.core.BiometricAuthManager
import com.magpiny.notafo.domain.repository.SettingsRepository
import com.magpiny.notafo.presentation.MainScreen
import com.magpiny.notafo.presentation.theme.NotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var biometricManager: BiometricAuthManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        checkExactAlarmPermission()

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = "System")
            val dynamicColor by settingsRepository.dynamicColor.collectAsStateWithLifecycle(initialValue = true)
            val language by settingsRepository.language.collectAsStateWithLifecycle(
                initialValue = AppCompatDelegate.getApplicationLocales().toLanguageTags(),
            )
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
                fontFamilyName = fontFamily,
            ) {
                MainScreen(biometricManager = biometricManager)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }
}
