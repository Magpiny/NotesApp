package com.example.notesapp.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.core.dimensions
import com.example.notesapp.presentation.home.NoteCard
import com.example.notesapp.presentation.task.TaskItem

/**
 * Screen dedicated to searching across notes and tasks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToTaskEditor: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var active by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize()) {
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = { active = false },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search notes & tasks...") },
            leadingIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = if (active) 0.dp else 16.dp)
        ) {
            SearchResultsList(
                state = state,
                query = query,
                onNavigateToEditor = onNavigateToEditor,
                onNavigateToTaskEditor = onNavigateToTaskEditor
            )
        }
        
        if (!active) {
            SearchResultsList(
                state = state,
                query = query,
                onNavigateToEditor = onNavigateToEditor,
                onNavigateToTaskEditor = onNavigateToTaskEditor,
                modifier = Modifier.padding(top = 72.dp)
            )
        }
    }
}

@Composable
fun SearchResultsList(
    state: SearchUiState,
    query: String,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToTaskEditor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        if (state.notes.isNotEmpty()) {
            item {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(state.notes, key = { "note_${it.id}" }) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNavigateToEditor(note.id) },
                    onCopy = { },
                    onArchive = { },
                    onDelete = { }
                )
            }
        }

        if (state.tasks.isNotEmpty()) {
            item {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(state.tasks, key = { "task_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    onToggle = { },
                    onDelete = { },
                    onClick = { onNavigateToTaskEditor(task.id) }
                )
            }
        }
        
        if (query.isNotBlank() && state.notes.isEmpty() && state.tasks.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found for \"$query\"")
                }
            }
        }
    }
}
