package com.magpiny.notafo.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magpiny.notafo.domain.model.Note
import com.magpiny.notafo.domain.usecase.GetLockedNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class VaultUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    getLockedNotesUseCase: GetLockedNotesUseCase
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
}
