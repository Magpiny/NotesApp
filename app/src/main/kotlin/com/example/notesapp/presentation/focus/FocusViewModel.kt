package com.example.notesapp.presentation.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.db.FocusSessionEntity
import com.example.notesapp.data.db.TaskDao
import com.example.notesapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class FocusUiState(
    val remainingTime: Long = 25 * 60 * 1000L,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val sessionCount: Int = 0,
    val showCompletionDialog: Boolean = false
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var endTimeMillis: Long = 0L

    init {
        loadTodaySessionCount()
        observeDurations()
    }

    private fun observeDurations() {
        combine(
            settingsRepository.focusDuration,
            settingsRepository.shortBreakDuration,
            settingsRepository.longBreakDuration
        ) { focus, short, _ ->
            if (!_uiState.value.isRunning) {
                val initialTime = if (_uiState.value.isBreak) short * 60 * 1000L else focus * 60 * 1000L
                _uiState.update { it.copy(remainingTime = initialTime) }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadTodaySessionCount() {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        taskDao.getFocusSessionCount(todayStart)
            .onEach { count ->
                _uiState.update { it.copy(sessionCount = count) }
            }.launchIn(viewModelScope)
    }

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        endTimeMillis = System.currentTimeMillis() + _uiState.value.remainingTime
        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = (endTimeMillis - now).coerceAtLeast(0L)
                _uiState.update { it.copy(remainingTime = remaining) }
                if (remaining <= 0) break
                delay(1000L)
            }
            onTimerFinished()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        val now = System.currentTimeMillis()
        val remaining = (endTimeMillis - now).coerceAtLeast(0L)
        _uiState.update { it.copy(isRunning = false, remainingTime = remaining) }
    }

    fun resetTimer() {
        pauseTimer()
        viewModelScope.launch {
            val focus = settingsRepository.focusDuration.first()
            val short = settingsRepository.shortBreakDuration.first()
            val initialTime = if (_uiState.value.isBreak) short * 60 * 1000L else focus * 60 * 1000L
            _uiState.update { it.copy(remainingTime = initialTime) }
        }
    }

    private fun onTimerFinished() {
        pauseTimer()
        viewModelScope.launch {
            val focus = settingsRepository.focusDuration.first()
            val short = settingsRepository.shortBreakDuration.first()
            val duration = if (_uiState.value.isBreak) short * 60 * 1000L else focus * 60 * 1000L
            
            taskDao.insertFocusSession(
                FocusSessionEntity(
                    startTime = System.currentTimeMillis() - duration,
                    duration = duration,
                    isBreak = _uiState.value.isBreak
                )
            )
            _uiState.update { it.copy(showCompletionDialog = true) }
        }
    }

    fun startNextSession() {
        viewModelScope.launch {
            val focus = settingsRepository.focusDuration.first()
            val short = settingsRepository.shortBreakDuration.first()
            _uiState.update { 
                val wasBreak = it.isBreak
                it.copy(
                    showCompletionDialog = false,
                    isBreak = !wasBreak,
                    remainingTime = if (!wasBreak) short * 60 * 1000L else focus * 60 * 1000L
                )
            }
            startTimer()
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showCompletionDialog = false) }
        resetTimer()
    }
}
