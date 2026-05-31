package com.example.notesapp.presentation.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.core.formatToReadableDate
import com.example.notesapp.core.calculateOnColor
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskEditorUiEvent.NavigateBack -> onNavigateBack()
                is TaskEditorUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val topBarColor = MaterialTheme.colorScheme.surface
    val contentColor = topBarColor.calculateOnColor()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.isLoading) "" else stringResource(R.string.tasks)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::saveTask) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = stringResource(R.string.done),
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = contentColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                minLines = 3
            )

            Text(text = stringResource(R.string.priority), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = state.priority == priority,
                        onClick = { viewModel.onPriorityChange(priority) },
                        label = { Text(priority.name) }
                    )
                }
            }

            Text(text = "Status", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.example.notesapp.domain.model.TaskStatus.entries.filter { it != com.example.notesapp.domain.model.TaskStatus.CANCELLED }.forEach { status ->
                    FilterChip(
                        selected = state.status == status,
                        onClick = { viewModel.onStatusChange(status) },
                        label = { Text(status.name.replace("_", " ")) }
                    )
                }
            }

            Text(text = stringResource(R.string.due_date), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = state.dueDate?.formatToReadableDate() ?: stringResource(R.string.none),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.select_date))
                }
                if (state.dueDate != null) {
                    TextButton(onClick = { viewModel.onDueDateChange(null) }) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }

            Text(text = stringResource(R.string.recurrence), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val patterns = listOf(
                    null to stringResource(R.string.none),
                    "DAILY" to stringResource(R.string.daily),
                    "WEEKLY" to stringResource(R.string.weekly),
                    "MONTHLY" to stringResource(R.string.monthly)
                )
                patterns.forEach { (pattern, label) ->
                    FilterChip(
                        selected = state.recurrencePattern == pattern,
                        onClick = { viewModel.onRecurrenceChange(pattern) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Subtasks", style = MaterialTheme.typography.titleMedium)
            
            var subtaskText by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = subtaskText,
                    onValueChange = { subtaskText = it },
                    placeholder = { Text("Add subtask...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = {
                    if (subtaskText.isNotBlank()) {
                        viewModel.addSubtask(subtaskText)
                        subtaskText = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subtask")
                }
            }

            state.subtasks.forEach { subtask ->
                ListItem(
                    headlineContent = { 
                        Text(
                            text = subtask.title,
                            style = if (subtask.isCompleted) 
                                MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) 
                            else MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Checkbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = { viewModel.toggleSubtask(subtask) }
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteSubtask(subtask) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Subtask")
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val calendar = Calendar.getInstance()
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = dateMillis
                        }
                        calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                        calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                        calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendar.set(Calendar.MINUTE, timePickerState.minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    viewModel.onDueDateChange(calendar.timeInMillis)
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
