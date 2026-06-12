package com.example.notesapp.presentation.task

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.notesapp.domain.model.Task
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.domain.model.TaskStatus
import com.example.notesapp.core.formatToReadableDate
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.m3.markdownColor

import androidx.compose.ui.res.stringResource
import com.example.notesapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    onNavigateToTaskEditor: (String?) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var quickAddText by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasks)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.Insights, contentDescription = stringResource(R.string.productivity_insights))
                    }
                    IconButton(onClick = viewModel::toggleView) {
                        Icon(
                            imageVector = if (state.isKanbanView) Icons.AutoMirrored.Filled.List else Icons.Default.ViewColumn,
                            contentDescription = stringResource(R.string.default_view)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToTaskEditor(null) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_task))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Quick Add Field
            TextField(
                value = quickAddText,
                onValueChange = { quickAddText = it },
                placeholder = { Text(stringResource(R.string.quick_add_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                trailingIcon = {
                    if (quickAddText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.quickAddTask(quickAddText)
                                quickAddText = ""
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.add))
                        }
                    }
                },
                keyboardActions = androidx.compose.foundation.text.KeyboardActions {
                    if (quickAddText.isNotBlank()) {
                        viewModel.quickAddTask(quickAddText)
                        quickAddText = ""
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.tasks.isEmpty() && quickAddText.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_tasks))
                }
            } else {
                if (state.isKanbanView) {
                    KanbanBoard(
                        tasks = state.tasks,
                        onStatusChange = viewModel::updateTaskStatus,
                        onDelete = { viewModel.deleteTask(it, context) },
                        onClick = onNavigateToTaskEditor,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.tasks, key = { _, task -> task.id }) { _, task ->
                            SwipeableTaskItem(
                                onDelete = { viewModel.deleteTask(task, context) },
                                content = {
                                    TaskItem(
                                        task = task,
                                        onToggle = { viewModel.toggleTaskCompletion(task) },
                                        onDelete = { viewModel.deleteTask(task, context) },
                                        onClick = { onNavigateToTaskEditor(task.id) }
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
fun SwipeableTaskItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) MaterialTheme.colorScheme.errorContainer
                else Color.Transparent, label = "Color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, MaterialTheme.shapes.medium)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanBoard(
    tasks: List<Task>,
    onStatusChange: (Task, TaskStatus) -> Unit,
    onDelete: (Task) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statuses = TaskStatus.entries.filter { it != TaskStatus.CANCELLED }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            statuses.forEachIndexed { index, status ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = status.name.replace("_", " "),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        val currentStatus = statuses[selectedTabIndex]
        KanbanColumn(
            status = currentStatus,
            tasks = tasks.filter { it.status == currentStatus },
            onStatusChange = onStatusChange,
            onDelete = onDelete,
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Composable
fun KanbanColumn(
    status: TaskStatus,
    tasks: List<Task>,
    onStatusChange: (Task, TaskStatus) -> Unit,
    onDelete: (Task) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.outline
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        TaskStatus.COMPLETED -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    val nextStatus = when (status) {
        TaskStatus.TODO -> TaskStatus.IN_PROGRESS
        TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
        else -> null
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            StatusChip(status = status.name, color = color)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tasks.size.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onDelete = { onDelete(task) },
                    onClick = { onClick(task.id) },
                    onNextStatus = nextStatus?.let { { onStatusChange(task, it) } }
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onNextStatus: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Markdown(
                    content = task.title,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
                    typography = markdownTypography(
                        paragraph = MaterialTheme.typography.titleSmall,
                        ordered = MaterialTheme.typography.titleSmall,
                        bullet = MaterialTheme.typography.titleSmall,
                    )
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (task.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if ((task.dueDate < System.currentTimeMillis()) && task.status != TaskStatus.COMPLETED) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dueDate.formatToReadableDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if ((task.dueDate < System.currentTimeMillis()) && task.status != TaskStatus.COMPLETED) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(priority = task.priority)
                
                onNextStatus?.let { onNext ->
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Status",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(value = false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "Scale")

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
                    }
                )
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle() 
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                val textColor = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                Markdown(
                    content = task.title,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = markdownColor(text = textColor),
                    typography = markdownTypography(
                        paragraph = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        ),
                        ordered = MaterialTheme.typography.bodyLarge,
                        bullet = MaterialTheme.typography.bodyLarge,
                    )
                )
                
                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (task.dueDate < System.currentTimeMillis() && !isCompleted) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.dueDate.formatToReadableDate(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.dueDate < System.currentTimeMillis() && !isCompleted) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            PriorityChip(priority = task.priority)

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
