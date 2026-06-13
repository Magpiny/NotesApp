package com.example.notesapp.presentation.editor

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.core.*
import com.example.notesapp.domain.model.*
import com.example.notesapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.notesapp.domain.model.Task
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.domain.model.TaskStatus
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class EditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val color: Long = 0xFFFFFFFFL,
    val isPinned: Boolean = false,
    val labels: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val isLoading: Boolean = false,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val isRecording: Boolean = false,
    val isSpeaking: Boolean = false,
    val isAudioPlaying: Boolean = false,
    val ttsSpeed: Float = 1.0f,
    val recordingWaveform: List<Float> = emptyList()
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
    @param:ApplicationContext private val context: Context,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val linkTaskToNoteUseCase: LinkTaskToNoteUseCase,
    private val getAttachmentsUseCase: GetAttachmentsUseCase,
    private val saveAttachmentUseCase: SaveAttachmentUseCase,
    private val deleteAttachmentUseCase: DeleteAttachmentUseCase,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]
    val isExistingNote = !noteId.isNullOrBlank()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorUiEvent>()
    val events = _events.asSharedFlow()

    private val undoStack = ArrayDeque<EditSession>()
    private val redoStack = ArrayDeque<EditSession>()

    private var lastPushedSession: EditSession? = null
    private var isUndoingRedoing = false
    private var currentRecordingFile: File? = null

    init {
        loadNote()
        setupAutoSave()
        setupUndoRedo()
        loadAttachments()
        observeAudioPlayback()
    }

    private fun observeAudioPlayback() {
        audioPlayer.isPlaying.onEach { isPlaying ->
            _uiState.update { it.copy(isAudioPlaying = isPlaying) }
        }.launchIn(viewModelScope)

        audioPlayer.waveform.onEach { waveform ->
            if (_uiState.value.isAudioPlaying) {
                _uiState.update { it.copy(recordingWaveform = waveform) }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadAttachments() {
        if (noteId != null) {
            getAttachmentsUseCase(noteId).onEach { attachments ->
                _uiState.update { it.copy(attachments = attachments) }
            }.launchIn(viewModelScope)
        }
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
                            val currentNumber = match.groupValues[1].toIntOrNull() ?: 1
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

    fun addAttachment(uri: String, type: AttachmentType) {
        viewModelScope.launch {
            // Ensure the note is saved before adding an attachment due to foreign key constraint
            saveCurrentNote(_uiState.value)

            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                noteId = _uiState.value.id,
                localPath = uri,
                type = type,
                createdAt = System.currentTimeMillis()
            )
            saveAttachmentUseCase(attachment)
        }
    }

    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch {
            deleteAttachmentUseCase(attachment)
        }
    }

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "audio_${UUID.randomUUID()}.m4a")
            currentRecordingFile = file
            audioRecorder.start(file)
            _uiState.update { it.copy(isRecording = true, recordingWaveform = emptyList()) }
            
            // Start amplitude tracking
            viewModelScope.launch {
                while (_uiState.value.isRecording) {
                    val amplitude = audioRecorder.getMaxAmplitude()
                    val normalized = (amplitude.toFloat() / 32767f).coerceIn(0f, 1f)
                    _uiState.update { 
                        it.copy(recordingWaveform = it.recordingWaveform + normalized) 
                    }
                    kotlinx.coroutines.delay(100)
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _events.emit(EditorUiEvent.ShowSnackbar("Failed to start recording"))
            }
        }
    }

    fun stopRecording() {
        try {
            audioRecorder.stop()
            _uiState.update { it.copy(isRecording = false) }
            currentRecordingFile?.let { file ->
                addAttachment(file.absolutePath, AttachmentType.AUDIO)
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _events.emit(EditorUiEvent.ShowSnackbar("Failed to stop recording"))
            }
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }

    fun playAudio(attachment: Attachment) {
        val file = File(attachment.localPath)
        if (file.exists()) {
            audioPlayer.playFile(file)
        } else {
            viewModelScope.launch {
                _events.emit(EditorUiEvent.ShowSnackbar("Audio file not found"))
            }
        }
    }

    fun toggleReadAloud() {
        if (_uiState.value.isSpeaking) {
            ttsManager.stop()
            _uiState.update { it.copy(isSpeaking = false) }
        } else {
            val textToSpeak = "${_uiState.value.title}. ${_uiState.value.content}"
            if (textToSpeak.isNotBlank()) {
                ttsManager.speak(textToSpeak, _uiState.value.ttsSpeed)
                _uiState.update { it.copy(isSpeaking = true) }
            }
        }
    }

    fun onTtsSpeedChange(speed: Float) {
        _uiState.update { it.copy(ttsSpeed = speed) }
        val state = _uiState.value
        if (state.isSpeaking) {
            // Restart with new speed
            val textToSpeak = "${state.title}. ${state.content}"
            ttsManager.speak(textToSpeak, speed)
        }
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
            // TODO: Fix duplicate task creation before re-enabling sync
            // syncInlineTasks(state.content, noteId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        // audioPlayer.stop() // Removed for background playback
    }
}
