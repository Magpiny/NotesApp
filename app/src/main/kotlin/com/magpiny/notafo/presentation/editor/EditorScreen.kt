package com.magpiny.notafo.presentation.editor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magpiny.notafo.R
import com.magpiny.notafo.core.MarkdownVisualTransformation
import com.magpiny.notafo.core.SoundUtils
import com.magpiny.notafo.core.calculateOnColor
import com.magpiny.notafo.core.dimensions
import com.magpiny.notafo.core.shareNote
import com.magpiny.notafo.domain.model.AttachmentType
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.highlightedCodeBlock
import com.mikepenz.markdown.compose.elements.highlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.magpiny.notafo.presentation.components.ColorPicker
import com.magpiny.notafo.presentation.components.WaveformVisualizer
import coil3.compose.AsyncImage

/**
 * Full-screen editor for creating and modifying notes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val (showLabelDialog, setShowLabelDialog) = remember { mutableStateOf(value = false) }
    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(value = false) }
    
    // Default to Preview mode for existing notes, Edit mode for new ones
    var isPreviewMode by remember { 
        mutableStateOf<Boolean>(viewModel.isExistingNote)
    }
    
    var showMenu by remember { mutableStateOf(value = false) }
    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addAttachment(it.toString(), AttachmentType.IMAGE) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    var contentFieldValue by remember { mutableStateOf(TextFieldValue(state.content)) }

    // Sync state content to field value only when state changes externally (e.g. undo/redo)
    LaunchedEffect(state.content) {
        if (contentFieldValue.text != state.content) {
            contentFieldValue = contentFieldValue.copy(text = state.content)
        }
    }

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
            onDismiss = { setShowLabelDialog(false) },
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
                    if (state.isSpeaking) {
                        Slider(
                            value = state.ttsSpeed,
                            onValueChange = viewModel::onTtsSpeedChange,
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.width(100.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = iconTint,
                                activeTrackColor = iconTint
                            )
                        )
                    }
                    IconButton(onClick = viewModel::toggleReadAloud) {
                        Icon(
                            imageVector = if (state.isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Read Aloud",
                            tint = iconTint
                        )
                    }
                    IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                        Icon(
                            imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = if (isPreviewMode) "Edit" else "Preview",
                            tint = iconTint
                        )
                    }
                    if (!isPreviewMode) {
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Image", tint = iconTint)
                        }
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
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Toggle Pin",
                            tint = iconTint
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = iconTint)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share)) },
                                onClick = {
                                    showMenu = false
                                    shareNote(context, state.title, state.content)
                                },
                                leadingIcon = { Icon(Icons.Default.Share, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.archive)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.archiveNote()
                                },
                                leadingIcon = { Icon(Icons.Default.Archive, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    SoundUtils.playDeletionSound(context)
                                    setShowDeleteDialog(true)
                                },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
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
                    IconButton(
                        onClick = {
                            if (state.isRecording) {
                                viewModel.stopRecording()
                            } else if (state.isAudioPlaying) {
                                viewModel.stopPlayback()
                            } else {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) -> {
                                        viewModel.startRecording()
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when {
                                state.isRecording -> Icons.Default.StopCircle
                                state.isAudioPlaying -> Icons.Default.Stop
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Audio Action",
                            tint = if (state.isRecording || state.isAudioPlaying) MaterialTheme.colorScheme.error else iconTint
                        )
                    }
                    
                    if (state.isRecording || state.isAudioPlaying) {
                        WaveformVisualizer(
                            amplitudes = state.recordingWaveform,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .padding(horizontal = 8.dp),
                            color = iconTint
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.wordCount} ${stringResource(R.string.words)} | ${state.charCount} ${stringResource(R.string.chars)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = iconTint
                        )
                    }
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
            if (state.attachments.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(state.attachments, key = { it.id }) { attachment ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (attachment.type == AttachmentType.IMAGE) {
                                AsyncImage(
                                    model = attachment.localPath,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else if (attachment.type == AttachmentType.AUDIO) {
                                IconButton(
                                    onClick = { viewModel.playAudio(attachment) },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Play Audio", modifier = Modifier.size(48.dp))
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteAttachment(attachment) },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close, 
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

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
                        ),
                        components = markdownComponents(
                            codeBlock = highlightedCodeBlock,
                            codeFence = highlightedCodeFence
                        ),
                        annotator = markdownAnnotator(
                            config = markdownAnnotatorConfig(eolAsNewLine = true)
                        )
                    )
                }
            } else {
                TextField(
                    value = contentFieldValue,
                    onValueChange = {
                        contentFieldValue = it
                        viewModel.onContentChange(it.text)
                    },
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
                        .onKeyEvent { keyEvent ->
                            if ((keyEvent.type == KeyEventType.KeyDown) && (keyEvent.key == Key.Enter)) {
                                val text = contentFieldValue.text
                                val selection = contentFieldValue.selection
                                if (selection.collapsed) {
                                    val cursorPosition = selection.start
                                    val lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1
                                    val currentLine = text.substring(lineStart, cursorPosition)
                                    
                                    val unorderedMatch = Regex("^([*+\\-]) (.*)").find(currentLine)
                                    val orderedMatch = Regex("^(\\d+)\\. (.*)").find(currentLine)
                                    
                                    if (unorderedMatch != null) {
                                        val marker = unorderedMatch.groupValues[1]
                                        val content = unorderedMatch.groupValues[2]
                                        if (content.isBlank()) {
                                            // Empty list item, clear the line
                                            val newText = text.removeRange(lineStart, cursorPosition)
                                            contentFieldValue = contentFieldValue.copy(
                                                text = newText,
                                                selection = TextRange(lineStart)
                                            )
                                            viewModel.onContentChange(newText)
                                            return@onKeyEvent true
                                        } else {
                                            val insertText = "\n$marker "
                                            val newText = text.replaceRange(cursorPosition, cursorPosition, insertText)
                                            contentFieldValue = contentFieldValue.copy(
                                                text = newText,
                                                selection = TextRange(cursorPosition + insertText.length)
                                            )
                                            viewModel.onContentChange(newText)
                                            return@onKeyEvent true
                                        }
                                    } else if (orderedMatch != null) {
                                        val number = orderedMatch.groupValues[1].toIntOrNull() ?: 1
                                        val content = orderedMatch.groupValues[2]
                                        if (content.isBlank()) {
                                            // Empty list item, clear the line
                                            val newText = text.removeRange(lineStart, cursorPosition)
                                            contentFieldValue = contentFieldValue.copy(
                                                text = newText,
                                                selection = TextRange(lineStart)
                                            )
                                            viewModel.onContentChange(newText)
                                            return@onKeyEvent true
                                        } else {
                                            val insertText = "\n${number + 1}. "
                                            val newText = text.replaceRange(cursorPosition, cursorPosition, insertText)
                                            contentFieldValue = contentFieldValue.copy(
                                                text = newText,
                                                selection = TextRange(cursorPosition + insertText.length)
                                            )
                                            viewModel.onContentChange(newText)
                                            return@onKeyEvent true
                                        }
                                    }
                                }
                            }
                            false
                        }
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
