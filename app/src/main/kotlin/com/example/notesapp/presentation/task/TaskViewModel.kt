package com.example.notesapp.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.core.NLPUtils
import com.example.notesapp.core.TaskReminderManager
import com.example.notesapp.domain.model.Task
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.domain.model.TaskStatus
import com.example.notesapp.domain.usecase.DeleteTaskUseCase
import com.example.notesapp.domain.usecase.GetAllTasksUseCase
import com.example.notesapp.domain.usecase.SaveTaskUseCase
import com.example.notesapp.domain.usecase.UpdateTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

data class TaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val isKanbanView: Boolean = false,
    val completionRate: Float = 0f,
    val completedLast7Days: Int = 0,
    val error: String? = null
)

sealed interface TaskUiEvent {
    data class ShowSnackbar(val message: String) : TaskUiEvent
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    getAllTasksUseCase: GetAllTasksUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTasksUseCase: UpdateTasksUseCase,
    private val reminderManager: TaskReminderManager
) : ViewModel() {

    private val _events = MutableSharedFlow<TaskUiEvent>()
    val events = _events.asSharedFlow()

    private val _isKanbanView = MutableStateFlow(false)

    val uiState: StateFlow<TaskUiState> = combine(
        getAllTasksUseCase().onStart { emit(emptyList()) },
        _isKanbanView
    ) { tasks, isKanban ->
        val total = tasks.size
        val completed = tasks.count { it.status == TaskStatus.COMPLETED }
        val rate = if (total > 0) completed.toFloat() / total else 0f
        
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        val last7Days = tasks.count { 
            it.status == TaskStatus.COMPLETED && it.updatedAt > sevenDaysAgo 
        }

        TaskUiState(
            tasks = tasks,
            isKanbanView = isKanban,
            completionRate = rate,
            completedLast7Days = last7Days,
            isLoading = false
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState(isLoading = true)
    )

    fun toggleView() {
        _isKanbanView.value = !_isKanbanView.value
    }

    fun toggleTaskCompletion(task: Task) {
        val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.TODO else TaskStatus.COMPLETED
        updateTaskStatus(task, newStatus)
    }

    fun updateTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            saveTaskUseCase(updatedTask).onSuccess {
                if (newStatus == TaskStatus.COMPLETED) {
                    reminderManager.cancelReminder(task.id)
                    if (task.recurrencePattern != null) {
                        handleRecurrence(task)
                    }
                } else if (task.dueDate != null) {
                    reminderManager.scheduleReminder(updatedTask)
                }
            }.onFailure {
                _events.emit(TaskUiEvent.ShowSnackbar("Failed to update task"))
            }
        }
    }

    fun quickAddTask(input: String) {
        viewModelScope.launch {
            val result = NLPUtils.parseQuickAdd(input)
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = result.title,
                description = "",
                status = TaskStatus.TODO,
                priority = result.priority,
                dueDate = result.dueDate,
                position = 0,
                projectId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                labels = result.labels
            )
            saveTaskUseCase(task).onSuccess {
                if (task.dueDate != null) {
                    reminderManager.scheduleReminder(task)
                }
            }.onFailure {
                _events.emit(TaskUiEvent.ShowSnackbar("Failed to quick add task"))
            }
        }
    }

    private suspend fun handleRecurrence(task: Task) {
        val pattern = task.recurrencePattern ?: return
        val currentDueDate = task.dueDate ?: System.currentTimeMillis()
        
        val nextDueDate = when (pattern) {
            "DAILY" -> currentDueDate + 86400000L
            "WEEKLY" -> currentDueDate + 604800000L
            "MONTHLY" -> {
                // Simplified monthly: 30 days
                currentDueDate + 2592000000L
            }
            else -> null
        } ?: return

        val nextTask = task.copy(
            id = UUID.randomUUID().toString(),
            status = TaskStatus.TODO,
            dueDate = nextDueDate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        saveTaskUseCase(nextTask)
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task).onFailure {
                _events.emit(TaskUiEvent.ShowSnackbar("Failed to delete task"))
            }
        }
    }

    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        val currentTasks = uiState.value.tasks.toMutableList()
        if (fromIndex !in currentTasks.indices || toIndex !in currentTasks.indices) return

        val movedTask = currentTasks.removeAt(fromIndex)
        currentTasks.add(toIndex, movedTask)

        val updatedTasks = currentTasks.mapIndexed { index, task ->
            task.copy(position = index)
        }

        viewModelScope.launch {
            updateTasksUseCase(updatedTasks)
        }
    }
}
