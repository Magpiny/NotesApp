package com.example.notesapp.presentation.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.core.TaskReminderManager
import com.example.notesapp.domain.model.*
import com.example.notesapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TaskEditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Long? = null,
    val recurrencePattern: String? = null,
    val projectId: String? = null,
    val labels: List<String> = emptyList(),
    val projects: List<Project> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface TaskEditorUiEvent {
    data object NavigateBack : TaskEditorUiEvent
    data class ShowSnackbar(val message: String) : TaskEditorUiEvent
}

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val getAllProjectsUseCase: GetAllProjectsUseCase,
    private val getSubtasksUseCase: GetSubtasksUseCase,
    private val saveSubtaskUseCase: SaveSubtaskUseCase,
    private val deleteSubtaskUseCase: DeleteSubtaskUseCase,
    private val reminderManager: TaskReminderManager
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]

    private val _uiState = MutableStateFlow(TaskEditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskEditorUiEvent>()
    val events = _events.asSharedFlow()

    init {
        loadProjects()
        loadTask()
    }

    private fun loadProjects() {
        getAllProjectsUseCase().onEach { projects ->
            _uiState.update { it.copy(projects = projects) }
        }.launchIn(viewModelScope)
    }

    private fun loadTask() {
        if (taskId != null && taskId.isNotBlank()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                getTaskByIdUseCase(taskId).onSuccess { task ->
                    _uiState.update {
                        it.copy(
                            id = task.id,
                            title = task.title,
                            description = task.description,
                            status = task.status,
                            priority = task.priority,
                            dueDate = task.dueDate,
                            recurrencePattern = task.recurrencePattern,
                            projectId = task.projectId,
                            labels = task.labels,
                            isLoading = false
                        )
                    }
                    loadSubtasks(task.id)
                }.onFailure {
                    _events.emit(TaskEditorUiEvent.ShowSnackbar("Failed to load task"))
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun loadSubtasks(taskId: String) {
        getSubtasksUseCase(taskId).onEach { subtasks ->
            _uiState.update { it.copy(subtasks = subtasks) }
        }.launchIn(viewModelScope)
    }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        val subtask = Subtask(
            id = UUID.randomUUID().toString(),
            taskId = _uiState.value.id,
            title = title,
            isCompleted = false,
            position = _uiState.value.subtasks.size
        )
        viewModelScope.launch {
            saveSubtaskUseCase(subtask)
        }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            saveSubtaskUseCase(subtask.copy(isCompleted = !subtask.isCompleted))
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            deleteSubtaskUseCase(subtask)
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun onPriorityChange(newPriority: TaskPriority) {
        _uiState.update { it.copy(priority = newPriority) }
    }

    fun onStatusChange(newStatus: TaskStatus) {
        _uiState.update { it.copy(status = newStatus) }
    }

    fun onDueDateChange(newDate: Long?) {
        _uiState.update { it.copy(dueDate = newDate) }
    }

    fun onRecurrenceChange(newPattern: String?) {
        _uiState.update { it.copy(recurrencePattern = newPattern) }
    }

    fun onProjectChange(newProjectId: String?) {
        _uiState.update { it.copy(projectId = newProjectId) }
    }

    fun saveTask() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank()) {
                _events.emit(TaskEditorUiEvent.ShowSnackbar("Title cannot be empty"))
                return@launch
            }

            val task = Task(
                id = state.id,
                title = state.title,
                description = state.description,
                status = state.status,
                priority = state.priority,
                dueDate = state.dueDate,
                projectId = state.projectId,
                position = 0, // Will be updated by reordering logic if needed
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                recurrencePattern = state.recurrencePattern,
                labels = state.labels
            )

            saveTaskUseCase(task).onSuccess {
                if (task.dueDate != null) {
                    reminderManager.scheduleReminder(task)
                } else {
                    reminderManager.cancelReminder(task.id)
                }
                _events.emit(TaskEditorUiEvent.NavigateBack)
            }.onFailure {
                _events.emit(TaskEditorUiEvent.ShowSnackbar("Failed to save task"))
            }
        }
    }
}
