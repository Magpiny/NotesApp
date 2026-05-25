package com.example.notesapp.presentation.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusUiState(
    val remainingTime: Long = 25 * 60 * 1000L,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val sessionCount: Int = 0
)

@HiltViewModel
class FocusViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTime > 0) {
                delay(1000L)
                _uiState.update { it.copy(remainingTime = it.remainingTime - 1000L) }
            }
            onTimerFinished()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        val initialTime = if (_uiState.value.isBreak) 5 * 60 * 1000L else 25 * 60 * 1000L
        _uiState.update { it.copy(remainingTime = initialTime) }
    }

    private fun onTimerFinished() {
        _uiState.update { 
            val wasBreak = it.isBreak
            val newSessionCount = if (!wasBreak) it.sessionCount + 1 else it.sessionCount
            it.copy(
                isRunning = false,
                isBreak = !wasBreak,
                remainingTime = if (!wasBreak) 5 * 60 * 1000L else 25 * 60 * 1000L,
                sessionCount = newSessionCount
            )
        }
    }
}
