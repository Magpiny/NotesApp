package com.example.notesapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.repository.SettingsRepository
import com.example.notesapp.domain.usecase.GetActiveNotesUseCase
import com.example.notesapp.domain.usecase.SaveNoteUseCase
import com.example.notesapp.domain.usecase.GetAllLabelsUseCase
import com.example.notesapp.domain.usecase.MoveNoteToTrashUseCase
import com.example.notesapp.domain.usecase.UpdateNotesUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

/** UI State for Home Screen. */
data class HomeUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val labels: List<String> = emptyList(),
    val selectedLabel: String? = null,
    val isGridView: Boolean = true,
    val error: String? = null,
    val selectedNoteIds: Set<String> = emptySet()
)

/** UI Events for Home Screen (e.g., Snackbar). */
sealed interface HomeUiEvent {
    data class ShowSnackbar(val message: String) : HomeUiEvent
}

/**
 * ViewModel managing state for the Home Screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    getActiveNotesUseCase: GetActiveNotesUseCase,
    getAllLabelsUseCase: GetAllLabelsUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val updateNotesUseCase: UpdateNotesUseCase,
    private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<HomeUiEvent>()
    val events = _events.asSharedFlow()

    private val _selectedLabel = MutableStateFlow<String?>(null)

    private val _selectedNoteIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        getActiveNotesUseCase().onStart { emit(emptyList()) },
        getAllLabelsUseCase().onStart { emit(emptyList()) },
        _selectedLabel,
        settingsRepository.isGridView.onStart { emit(true) },
        _selectedNoteIds
    ) { notes, labels, selectedLabel, isGrid, selectedIds ->
        val filteredNotes = if (selectedLabel == null) {
            notes
        } else {
            notes.filter { it.labels.contains(selectedLabel) }
        }
        HomeUiState(
            notes = filteredNotes,
            labels = labels,
            selectedLabel = selectedLabel,
            isGridView = isGrid,
            isLoading = false,
            selectedNoteIds = selectedIds
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onLabelSelected(label: String?) {
        _selectedLabel.value = label
    }

    fun onMove(fromIndex: Int, toIndex: Int) {
        val currentNotes = uiState.value.notes.toMutableList()
        if (fromIndex !in currentNotes.indices || toIndex !in currentNotes.indices) return

        val movedNote = currentNotes.removeAt(fromIndex)
        currentNotes.add(toIndex, movedNote)

        // Update positions based on new order
        val updatedNotes = currentNotes.mapIndexed { index, note ->
            note.copy(position = index)
        }

        viewModelScope.launch {
            updateNotesUseCase(updatedNotes)
        }
    }

    fun copyNote(note: Note) {
        viewModelScope.launch {
            val copy = note.copy(
                id = UUID.randomUUID().toString(),
                title = "${note.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveNoteUseCase(copy)
        }
    }

    fun toggleLayout() {
        viewModelScope.launch {
            settingsRepository.setGridView(!uiState.value.isGridView)
        }
    }

    fun lockNote(note: Note) {
        viewModelScope.launch {
            saveNoteUseCase(note.copy(isLocked = true, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleNoteSelection(noteId: String) {
        _selectedNoteIds.update { current ->
            if (current.contains(noteId)) current - noteId else current + noteId
        }
    }

    fun clearSelection() {
        _selectedNoteIds.value = emptySet()
    }

    fun bulkArchive() {
        val selectedIds = _selectedNoteIds.value
        if (selectedIds.isEmpty()) return
        
        viewModelScope.launch {
            val allNotes = uiState.value.notes
            val notesToArchive = allNotes.filter { selectedIds.contains(it.id) }.map {
                it.copy(isArchived = true, updatedAt = System.currentTimeMillis())
            }
            updateNotesUseCase(notesToArchive)
            clearSelection()
        }
    }

    fun bulkDelete() {
        val selectedIds = _selectedNoteIds.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            selectedIds.forEach { moveNoteToTrashUseCase(it) }
            clearSelection()
        }
    }

    fun bulkLock() {
        val selectedIds = _selectedNoteIds.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            val allNotes = uiState.value.notes
            val notesToLock = allNotes.filter { selectedIds.contains(it.id) }.map {
                it.copy(isLocked = true, updatedAt = System.currentTimeMillis())
            }
            updateNotesUseCase(notesToLock)
            clearSelection()
        }
    }
}
