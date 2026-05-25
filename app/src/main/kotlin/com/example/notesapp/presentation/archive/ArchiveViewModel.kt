package com.example.notesapp.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.usecase.GetArchivedNotesUseCase
import com.example.notesapp.domain.usecase.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val error: String? = null
)

sealed interface ArchiveUiEvent {
    data class ShowSnackbar(val message: String) : ArchiveUiEvent
}

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<ArchiveUiEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<ArchiveUiState> = getArchivedNotesUseCase()
        .map { notes -> ArchiveUiState(notes = notes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ArchiveUiState(isLoading = true)
        )

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            saveNoteUseCase(note.copy(isArchived = false, updatedAt = System.currentTimeMillis()))
        }
    }
    
    fun copyNote(note: Note) {
        // Implement if needed, similar to HomeViewModel
    }
}
