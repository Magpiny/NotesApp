package com.magpiny.notafo.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magpiny.notafo.domain.model.Note
import com.magpiny.notafo.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTrashedNotes()
    }

    private fun loadTrashedNotes() {
        repository.getTrashedNotes()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { notes ->
                _uiState.update { it.copy(notes = notes, isLoading = false) }
            }.launchIn(viewModelScope)
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            repository.saveNote(note.copy(isTrashed = false, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deletePermanently(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note.id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            _uiState.value.notes.forEach { repository.deleteNote(it.id) }
        }
    }
}
