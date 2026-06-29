package com.magpiny.notafo.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magpiny.notafo.core.dimensions
import com.magpiny.notafo.core.formatToReadableDate
import com.magpiny.notafo.core.shareNote
import com.magpiny.notafo.domain.model.Note
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig

import com.magpiny.notafo.core.calculateOnColor
import androidx.compose.ui.res.stringResource
import com.magpiny.notafo.R

/**
 * Main Home Screen displaying active notes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSelectionMode = state.selectedNoteIds.isNotEmpty()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${state.selectedNoteIds.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::bulkLock) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Selected")
                        }
                        IconButton(onClick = viewModel::bulkArchive) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive Selected")
                        }
                        IconButton(onClick = viewModel::bulkDelete) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Selected")
                        }
                    }
                )
            } else {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.home)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToTrash) {
                            Icon(Icons.Default.Delete, contentDescription = "Trash")
                        }
                        IconButton(onClick = onNavigateToArchive) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                        }
                        IconButton(onClick = viewModel::toggleLayout) {
                            Icon(
                                imageVector = if (state.isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                contentDescription = stringResource(R.string.default_view)
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { onNavigateToEditor(null) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_note))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.labels.isNotEmpty() && !isSelectionMode) {
                LabelFilter(
                    labels = state.labels,
                    selectedLabel = state.selectedLabel,
                    onLabelSelected = viewModel::onLabelSelected
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("${stringResource(id = R.string.back)}: ${state.error}")
                }
            } else {
                if (state.isGridView) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(MaterialTheme.dimensions.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.paddingSmall),
                        verticalItemSpacing = MaterialTheme.dimensions.paddingSmall,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { 
                                    if (isSelectionMode) viewModel.toggleNoteSelection(note.id)
                                    else onNavigateToEditor(note.id) 
                                },
                                onCopy = { viewModel.copyNote(note) },
                                onLock = { viewModel.lockNote(note) },
                                onArchive = { viewModel.archiveNote(note) },
                                onDelete = { viewModel.deleteNote(note) },
                                isSelected = state.selectedNoteIds.contains(note.id)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(MaterialTheme.dimensions.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.paddingSmall),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.notes, key = { _, it -> it.id }) { index, note ->
                            SwipeableNoteCard(
                                onArchive = { viewModel.archiveNote(note) },
                                onDelete = { viewModel.deleteNote(note) },
                                content = {
                                    NoteCard(
                                        note = note,
                                        onClick = { 
                                            if (isSelectionMode) viewModel.toggleNoteSelection(note.id)
                                            else onNavigateToEditor(note.id) 
                                        },
                                        onCopy = { viewModel.copyNote(note) },
                                        onLock = { viewModel.lockNote(note) },
                                        onArchive = { viewModel.archiveNote(note) },
                                        onDelete = { viewModel.deleteNote(note) },
                                        isSelected = state.selectedNoteIds.contains(note.id),
                                        onMoveUp = if (index > 0 && !isSelectionMode) { { viewModel.onMove(index, index - 1) } } else null,
                                        onMoveDown = if (index < (state.notes.size - 1) && !isSelectionMode) { { viewModel.onMove(index, index + 1) } } else null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNoteCard(
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    
    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            StartToEnd -> {
                onDelete()
                dismissState.snapTo(Settled)
            }
            EndToStart -> {
                onArchive()
                dismissState.snapTo(Settled)
            }
            else -> {}
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    Settled -> Color.Transparent
                    StartToEnd -> MaterialTheme.colorScheme.errorContainer
                    EndToStart -> MaterialTheme.colorScheme.secondaryContainer
                }, label = "Color"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, MaterialTheme.shapes.large)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (direction == StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == StartToEnd) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                } else if (direction == EndToStart) {
                    Icon(Icons.Default.Archive, contentDescription = "Archive", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelFilter(
    labels: List<String>,
    selectedLabel: String?,
    onLabelSelected: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedLabel == null,
                onClick = { onLabelSelected(null) },
                label = { Text(stringResource(R.string.all)) }
            )
        }
        items(labels) { label ->
            FilterChip(
                selected = selectedLabel == label,
                onClick = { onLabelSelected(label) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onLock: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null,
    isSelected: Boolean = false,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    var showSheet by remember { mutableStateOf(value = false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "Scale")

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(note.color.toInt())
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else containerColor.calculateOnColor()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.copy)) },
                    leadingContent = { Icon(Icons.Default.ContentCopy, null) },
                    modifier = Modifier.clickable {
                        onCopy()
                        showSheet = false
                    }
                )
                if (onArchive != null) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.archive)) },
                        leadingContent = { Icon(Icons.Default.Archive, null) },
                        modifier = Modifier.clickable {
                            onArchive()
                            showSheet = false
                        }
                    )
                }
                if (onDelete != null) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            onDelete()
                            showSheet = false
                        }
                    )
                }
                if (onLock != null) {
                    ListItem(
                        headlineContent = { Text(if (note.isLocked) "Unlock" else "Lock") },
                        leadingContent = { Icon(if (note.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, null) },
                        modifier = Modifier.clickable {
                            onLock()
                            showSheet = false
                        }
                    )
                }
                if (onMoveUp != null) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.move_up)) },
                        leadingContent = { Icon(Icons.Default.ArrowUpward, null) },
                        modifier = Modifier.clickable {
                            onMoveUp()
                            showSheet = false
                        }
                    )
                }
                if (onMoveDown != null) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.move_down)) },
                        leadingContent = { Icon(Icons.Default.ArrowDownward, null) },
                        modifier = Modifier.clickable {
                            onMoveDown()
                            showSheet = false
                        }
                    )
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                    onTap = { onClick() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showSheet = true
                    }
                )
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (note.content.isNotBlank()) {
                Box(modifier = Modifier.heightIn(max = 120.dp).padding(horizontal = 4.dp)) {
                    Markdown(
                        content = note.content,
                        colors = markdownColor(text = contentColor),
                        typography = markdownTypography(
                            paragraph = MaterialTheme.typography.bodyMedium,
                            h1 = MaterialTheme.typography.titleMedium,
                            h2 = MaterialTheme.typography.titleSmall,
                            h3 = MaterialTheme.typography.titleSmall,
                            ordered = MaterialTheme.typography.bodyMedium,
                            bullet = MaterialTheme.typography.bodyMedium,
                        ),
                        annotator = markdownAnnotator(
                            config = markdownAnnotatorConfig(eolAsNewLine = true)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = contentColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = note.createdAt.formatToReadableDate(),
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(
                    onClick = { shareNote(context, note.title, note.content) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
