package com.magpiny.notafo.presentation.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import android.content.ContextWrapper
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magpiny.notafo.R
import com.magpiny.notafo.core.BiometricAuthManager
import com.magpiny.notafo.core.dimensions
import com.magpiny.notafo.presentation.home.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String?) -> Unit,
    biometricManager: BiometricAuthManager,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) break
            ctx = ctx.baseContext
        }
        ctx as? FragmentActivity
    }

    LaunchedEffect(Unit) {
        if (!state.isAuthenticated && activity != null) {
            if (biometricManager.isBiometricAvailable()) {
                biometricManager.showBiometricPrompt(
                    activity = activity,
                    title = "Secret Vault",
                    subtitle = "Authenticate to view private notes",
                    onSuccess = { viewModel.onAuthSuccess() },
                    onError = { onNavigateBack() }
                )
            } else {
                // Deny access if biometrics/device security is not configured
                // Or show a message. For now, let's navigate back with a snackbar or just prevent success.
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secret Vault") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (!state.isAuthenticated) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.notes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your secret notes will appear here.")
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(MaterialTheme.dimensions.paddingMedium),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.paddingSmall),
                verticalItemSpacing = MaterialTheme.dimensions.paddingSmall,
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                items(state.notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNavigateToEditor(note.id) },
                        onCopy = { },
                        onArchive = { },
                        onDelete = { }
                    )
                }
            }
        }
    }
}
