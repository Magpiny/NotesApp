package com.example.notesapp.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.presentation.components.ColorPicker
import com.example.notesapp.core.calculateOnColor
import com.example.notesapp.core.dimensions
import com.example.notesapp.core.shareNote
import com.example.notesapp.core.MarkdownVisualTransformation

/**
 * Full-screen editor for creating and modifying notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLabelDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.saveAndExit()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is EditorUiEvent.NavigateBack -> onNavigateBack()
                is EditorUiEvent.ShowSnackbar -> { /* Show snackbar */ }
            }
        }
    }

    if (showLabelDialog) {
        LabelDialog(
            currentLabels = state.labels,
            onAddLabel = viewModel::addLabel,
            onRemoveLabel = viewModel::removeLabel,
            onDismiss = { showLabelDialog = false }
        )
    }

    val contentColor = Color(state.color).calculateOnColor()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveAndExit() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLabelDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = stringResource(R.string.labels), tint = contentColor)
                    }
                    IconButton(onClick = viewModel::undo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = contentColor)
                    }
                    IconButton(onClick = viewModel::redo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = contentColor)
                    }
                    IconButton(onClick = { shareNote(context, state.title, state.content) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = contentColor)
                    }
                    IconButton(onClick = viewModel::archiveNote) {
                        Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive), tint = contentColor)
                    }
                    IconButton(onClick = { viewModel.deleteNote(context) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = contentColor)
                    }
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Toggle Pin",
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentPadding = PaddingValues(horizontal = MaterialTheme.dimensions.paddingMedium),
                actions = {
                    Text(
                        text = "${state.wordCount} ${stringResource(R.string.words)} | ${state.charCount} ${stringResource(R.string.chars)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )
                },
                floatingActionButton = {
                    ColorPicker(
                        selectedColor = state.color,
                        onColorSelected = viewModel::onColorChange
                    )
                }
            )
        }
    ) { padding ->
        Surface(
            color = Color(state.color),
            contentColor = contentColor,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .fillMaxSize()
            ) {
                if (state.labels.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(state.labels) { label ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeLabel(label) },
                                label = { Text(label) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = contentColor.copy(alpha = 0.1f),
                                    selectedLabelColor = contentColor
                                )
                            )
                        }
                    }
                }

                TextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { 
                        Text(
                            stringResource(R.string.title), 
                            style = MaterialTheme.typography.headlineMedium,
                            color = contentColor.copy(alpha = 0.5f)
                        ) 
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = contentColor),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = contentColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = state.content,
                    onValueChange = viewModel::onContentChange,
                    placeholder = { 
                        Text(
                            stringResource(R.string.note_content),
                            color = contentColor.copy(alpha = 0.5f)
                        ) 
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                    visualTransformation = MarkdownVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = contentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelDialog(
    currentLabels: List<String>,
    onAddLabel: (String) -> Unit,
    onRemoveLabel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newLabel by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_labels)) },
        text = {
            Column {
                TextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    placeholder = { Text(stringResource(R.string.new_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (newLabel.isNotBlank()) {
                            onAddLabel(newLabel)
                            newLabel = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.add))
                }
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentLabels.forEach { label ->
                        InputChip(
                            selected = true,
                            onClick = { onRemoveLabel(label) },
                            label = { Text(label) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        }
    )
}
