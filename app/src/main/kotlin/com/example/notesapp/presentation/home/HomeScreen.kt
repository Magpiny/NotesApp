package com.example.notesapp.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.core.dimensions
import com.example.notesapp.core.formatToReadableDate
import com.example.notesapp.core.parseMarkdown
import com.example.notesapp.core.shareNote
import com.example.notesapp.domain.model.Note

import com.example.notesapp.core.calculateOnColor
import androidx.compose.ui.res.stringResource
import com.example.notesapp.R

/**
 * Main Home Screen displaying active notes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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
                        IconButton(onClick = onNavigateToArchive) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive))
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
            
            // ... rest of the list logic

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
                            NoteCard(
                                note = note,
                                onClick = { 
                                    if (isSelectionMode) viewModel.toggleNoteSelection(note.id)
                                    else onNavigateToEditor(note.id) 
                                },
                                onCopy = { viewModel.copyNote(note) },
                                onLock = { viewModel.lockNote(note) },
                                isSelected = state.selectedNoteIds.contains(note.id),
                                onMoveUp = if (index > 0 && !isSelectionMode) { { viewModel.onMove(index, index - 1) } } else null,
                                onMoveDown = if (index < state.notes.size - 1 && !isSelectionMode) { { viewModel.onMove(index, index + 1) } } else null
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onLock: (() -> Unit)? = null,
    isSelected: Boolean = false,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(note.color.toInt())
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else containerColor.calculateOnColor()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
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
                val styledContent = remember(note.content) {
                    parseMarkdown(note.content, stripMarkers = true).annotatedString
                }
                Text(
                    text = styledContent,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor.copy(alpha = 0.85f)
                )
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

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.copy)) },
                onClick = {
                    onCopy()
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
            )
            if (onMoveUp != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_up)) },
                    onClick = {
                        onMoveUp()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                )
            }
            if (onMoveDown != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_down)) },
                    onClick = {
                        onMoveDown()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                )
            }
            if (onLock != null) {
                DropdownMenuItem(
                    text = { Text(if (note.isLocked) "Unlock" else "Lock") },
                    onClick = {
                        onLock()
                        showMenu = false
                    },
                    leadingIcon = { Icon(if (note.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null) }
                )
            }
        }
    }
}
