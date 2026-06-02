package com.example.notesapp.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.example.notesapp.core.SoundUtils
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.m3.markdownColor
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Full-screen editor for creating and modifying notes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val (showLabelDialog, setShowLabelDialog) = remember { mutableStateOf(false) }
    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }
    var isPreviewMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
            onDismiss = { setShowLabelDialog(false) }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { setShowDeleteDialog(false) },
            title = { Text(stringResource(R.string.delete_note)) },
            text = { Text(stringResource(R.string.delete_confirmation_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        setShowDeleteDialog(false)
                        viewModel.deleteNote()
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowDeleteDialog(false) }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    val backgroundColor = Color(state.color.toInt())
    val contentColor = backgroundColor.calculateOnColor()
    // Dynamic icon tint based on background luminance for visibility on White backgrounds.
    val iconTint = if (backgroundColor.calculateOnColor() == Color.White) Color.White else Color.Black

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveAndExit() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = iconTint
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                        Icon(
                            imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = if (isPreviewMode) "Edit" else "Preview",
                            tint = iconTint
                        )
                    }
                    IconButton(onClick = { setShowLabelDialog(true) }) {
                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = stringResource(R.string.labels), tint = iconTint)
                    }
                    IconButton(onClick = viewModel::undo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = iconTint)
                    }
                    IconButton(onClick = viewModel::redo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = iconTint)
                    }
                    IconButton(onClick = { shareNote(context, state.title, state.content) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = iconTint)
                    }
                    IconButton(onClick = viewModel::archiveNote) {
                        Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive), tint = iconTint)
                    }
                    IconButton(onClick = { 
                        SoundUtils.playDeletionSound(context)
                        setShowDeleteDialog(true) 
                    }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = iconTint)
                    }
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Toggle Pin",
                            tint = iconTint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = iconTint,
                    navigationIconContentColor = iconTint,
                    actionIconContentColor = iconTint
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentColor = iconTint,
                contentPadding = PaddingValues(horizontal = MaterialTheme.dimensions.paddingSmall),
                actions = {
                    Text(
                        text = "${state.wordCount} ${stringResource(R.string.words)} | ${state.charCount} ${stringResource(R.string.chars)}",
                        style = MaterialTheme.typography.labelSmall,

                        color = iconTint
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
        Column(
            modifier = Modifier
                .padding(padding)
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
                readOnly = isPreviewMode,
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

            if (isPreviewMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Markdown(
                        content = state.content,
                        modifier = Modifier.fillMaxWidth(),
                        colors = markdownColor(text = contentColor),
                        typography = markdownTypography(
                            paragraph = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                            h1 = MaterialTheme.typography.headlineMedium.copy(color = contentColor),
                            h2 = MaterialTheme.typography.titleLarge.copy(color = contentColor),
                            h3 = MaterialTheme.typography.titleMedium.copy(color = contentColor),
                            ordered = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                            bullet = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                        )
                    )
                }
            } else {
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
