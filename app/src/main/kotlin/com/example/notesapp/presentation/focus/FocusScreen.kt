package com.example.notesapp.presentation.focus

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onNavigateBack: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text(if (state.isBreak) "Break Finished!" else "Focus Session Complete!") },
            text = { Text(if (state.isBreak) "Ready to focus again?" else "Time to take a well-deserved break.") },
            confirmButton = {
                Button(onClick = { viewModel.startNextSession() }) {
                    Text(if (state.isBreak) "Start Focus" else "Take Break")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("Stop for Today")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.isBreak) "Take a Break" else "Time to Focus",
                style = MaterialTheme.typography.headlineMedium,
                color = if (state.isBreak) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = formatTime(state.remainingTime),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LargeIconButton(
                    icon = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    onClick = { viewModel.toggleTimer() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                
                LargeIconButton(
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.resetTimer() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "Sessions completed today: ${state.sessionCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LargeIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    containerColor: Color
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
