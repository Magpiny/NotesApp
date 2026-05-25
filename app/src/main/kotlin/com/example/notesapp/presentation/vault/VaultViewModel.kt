package com.example.notesapp.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.usecase.GetLockedNotesUseCase
import com.example.notesapp.domain.usecase.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val getLockedNotesUseCase: GetLockedNotesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)

    val uiState: StateFlow<VaultUiState> = combine(
        getLockedNotesUseCase().onStart { emit(emptyList()) },
        _isAuthenticated
    ) { notes, authenticated ->
        VaultUiState(
            notes = notes,
            isAuthenticated = authenticated,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultUiState(isLoading = true)
    )

    fun onAuthSuccess() {
        _isAuthenticated.value = true
    }

    fun unlockNote(note: Note) {
        viewModelScope.launch {
            saveNoteUseCase(note.copy(isLocked = false, updatedAt = System.currentTimeMillis()))
        }
    }
}
