package com.example.notesapp.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.core.Constants
import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.usecase.GetNoteByIdUseCase
import com.example.notesapp.domain.usecase.MoveNoteToTrashUseCase
import com.example.notesapp.domain.usecase.SaveNoteUseCase
import com.example.notesapp.domain.usecase.SaveTaskUseCase
import com.example.notesapp.domain.usecase.LinkTaskToNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.notesapp.domain.model.Task
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.domain.model.TaskStatus
import java.util.UUID
import javax.inject.Inject

data class EditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val color: Long = 0xFFFFFFFFL,
    val isPinned: Boolean = false,
    val labels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val wordCount: Int = 0,
    val charCount: Int = 0
)

sealed interface EditorUiEvent {
    data object NavigateBack : EditorUiEvent
    data class ShowSnackbar(val message: String) : EditorUiEvent
}

data class EditSession(val title: String, val content: String)

/**
 * ViewModel managing the Note Editor, auto-saving, and undo/redo stacks.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val linkTaskToNoteUseCase: LinkTaskToNoteUseCase
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorUiEvent>()
    val events = _events.asSharedFlow()

    private val undoStack = ArrayDeque<EditSession>()
    private val redoStack = ArrayDeque<EditSession>()

    private var lastPushedSession: EditSession? = null
    private var isUndoingRedoing = false

    init {
        loadNote()
        setupAutoSave()
        setupUndoRedo()
    }

    private fun loadNote() {
        if (noteId != null && noteId.isNotBlank()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                getNoteByIdUseCase(noteId).onSuccess { note ->
                    _uiState.update {
                        it.copy(
                            id = note.id,
                            title = note.title,
                            content = note.content,
                            color = note.color,
                            isPinned = note.isPinned,
                            labels = note.labels,
                            isLoading = false
                        )
                    }
                    pushUndoState(note.title, note.content)
                }.onFailure {
                    _events.emit(EditorUiEvent.ShowSnackbar("Failed to load note"))
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        } else {
            // New note - ensure we have a fresh undo state
            pushUndoState("", "")
        }
    }

    private fun setupAutoSave() {
        viewModelScope.launch {
            _uiState
                .debounce(Constants.DEBOUNCE_AUTO_SAVE_MS)
                .collect { state ->
                    if (state.title.isNotBlank() || state.content.isNotBlank()) {
                        saveCurrentNote(state)
                    }
                }
        }
    }

    private fun setupUndoRedo() {
        viewModelScope.launch {
            _uiState
                .debounce(1000L) // Push to undo stack after 1 second of inactivity
                .collect { state ->
                    if (!isUndoingRedoing) {
                        pushUndoState(state.title, state.content)
                    }
                }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onContentChange(newContent: String) {
        val oldContent = _uiState.value.content
        var finalContent = newContent

        // Auto-listing logic (basic implementation for typing at the end)
        if (newContent.length > oldContent.length && newContent.endsWith("\n")) {
            val lines = oldContent.split("\n")
            val lastLine = lines.lastOrNull() ?: ""

            when {
                // Empty list item - remove the marker and end the list
                lastLine == "- " || lastLine == "* " -> {
                    finalContent = oldContent.dropLast(2) + "\n"
                }
                // Bullet list continuation
                lastLine.startsWith("- ") -> {
                    finalContent = newContent + "- "
                }
                lastLine.startsWith("* ") -> {
                    finalContent = newContent + "* "
                }
                // Numbered list continuation
                else -> {
                    val match = Regex("^(\\d+)\\. ").find(lastLine)
                    if (match != null) {
                        if (lastLine == match.value) {
                            // Empty numbered item - remove and end
                            finalContent = oldContent.dropLast(match.value.length) + "\n"
                        } else {
                            val currentNumber = match.groupValues[1].toInt()
                            finalContent = newContent + "${currentNumber + 1}. "
                        }
                    }
                }
            }
        }

        val words = if (finalContent.isBlank()) 0 else finalContent.trim().split("\\s+".toRegex()).size
        _uiState.update { it.copy(content = finalContent, wordCount = words, charCount = finalContent.length) }
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
    }

    fun onColorChange(newColor: Long) {
        _uiState.update { it.copy(color = newColor) }
    }

    fun addLabel(label: String) {
        if (label.isBlank()) return
        val currentLabels = _uiState.value.labels
        if (!currentLabels.contains(label)) {
            _uiState.update { it.copy(labels = currentLabels + label) }
        }
    }

    fun removeLabel(label: String) {
        val currentLabels = _uiState.value.labels
        _uiState.update { it.copy(labels = currentLabels - label) }
    }

    private fun pushUndoState(title: String, content: String) {
        val session = EditSession(title, content)
        if (lastPushedSession == session) return

        if (!isUndoingRedoing) {
            redoStack.clear()
        }
        
        undoStack.addLast(session)
        lastPushedSession = session
        
        if (undoStack.size > Constants.MAX_UNDO_STACK_SIZE) {
            undoStack.removeFirst()
        }
    }

    fun undo() {
        if (undoStack.size > 1) {
            isUndoingRedoing = true
            val current = undoStack.removeLast()
            redoStack.addLast(current)
            
            val previous = undoStack.last()
            _uiState.update { it.copy(title = previous.title, content = previous.content) }
            lastPushedSession = previous
            isUndoingRedoing = false
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isUndoingRedoing = true
            val next = redoStack.removeLast()
            undoStack.addLast(next)
            
            _uiState.update { it.copy(title = next.title, content = next.content) }
            lastPushedSession = next
            isUndoingRedoing = false
        }
    }

    fun saveAndExit() {
        viewModelScope.launch {
            saveCurrentNote(_uiState.value)
            _events.emit(EditorUiEvent.NavigateBack)
        }
    }

    fun archiveNote() {
        viewModelScope.launch {
            val state = _uiState.value
            val note = Note(
                id = state.id,
                title = state.title,
                content = state.content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                color = state.color,
                isPinned = state.isPinned,
                isTrashed = false,
                isArchived = true,
                labels = emptyList(),
                notebookId = null
            )
            saveNoteUseCase(note)
            _events.emit(EditorUiEvent.NavigateBack)
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            moveNoteToTrashUseCase(_uiState.value.id)
            _events.emit(EditorUiEvent.NavigateBack)
        }
    }

    private suspend fun syncInlineTasks(content: String, noteId: String) {
        val taskRegex = Regex("- \\[([ x])] (.*)")
        taskRegex.findAll(content).forEach { match ->
            val isCompleted = match.groupValues[1] == "x"
            val title = match.groupValues[2]
            
            // For now, we create a new task if it doesn't exist (basic heuristic)
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = "Linked from note: ${_uiState.value.title}",
                status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO,
                priority = TaskPriority.MEDIUM,
                dueDate = null,
                position = 0,
                projectId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            saveTaskUseCase(task).onSuccess {
                linkTaskToNoteUseCase(task.id, noteId)
            }
        }
    }

    private suspend fun saveCurrentNote(state: EditorUiState) {
        val noteId = state.id
        val note = Note(
            id = noteId,
            title = state.title,
            content = state.content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            color = state.color,
            isPinned = state.isPinned,
            isTrashed = false,
            isArchived = false,
            labels = state.labels,
            notebookId = null
        )
        saveNoteUseCase(note).onSuccess {
            syncInlineTasks(state.content, noteId)
        }
    }
}
