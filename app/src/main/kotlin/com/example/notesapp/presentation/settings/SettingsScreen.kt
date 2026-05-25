package com.example.notesapp.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.ui.res.stringResource
import com.example.notesapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
            
            ThemeSelector(
                selectedMode = state.themeMode,
                onModeSelected = viewModel::setThemeMode
            )

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.dynamic_color), style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.use_system_accent), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor
                )
            }

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.default_view), style = MaterialTheme.typography.titleMedium)
                    Text(text = if (state.isGridView) stringResource(R.string.grid_view) else stringResource(R.string.list_view), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.isGridView,
                    onCheckedChange = viewModel::setGridView
                )
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            LanguageSelector(
                selectedLanguage = state.language,
                onLanguageSelected = viewModel::setLanguage
            )

            HorizontalDivider()

            Text(text = "Font Style", style = MaterialTheme.typography.titleMedium)
            FontSelector(
                selectedFont = state.fontFamily,
                onFontSelected = viewModel::setFontFamily
            )
        }
    }
}

@Composable
fun FontSelector(
    selectedFont: String,
    onFontSelected: (String) -> Unit
) {
    val options = listOf("Sans", "Serif", "Mono")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { font ->
            FilterChip(
                selected = selectedFont == font,
                onClick = { onFontSelected(font) },
                label = { Text(font) }
            )
        }
    }
}

@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val options = listOf("en" to "English", "es" to "Español")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (code, name) ->
            FilterChip(
                selected = selectedLanguage == code,
                onClick = { onLanguageSelected(code) },
                label = { Text(name) }
            )
        }
    }
}

@Composable
fun ThemeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val options = listOf("System", "Light", "Dark")
    Column(Modifier.selectableGroup()) {
        options.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedMode),
                        onClick = { onModeSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedMode),
                    onClick = null
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
