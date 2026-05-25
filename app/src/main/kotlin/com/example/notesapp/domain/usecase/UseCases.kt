package com.example.notesapp.domain.usecase

import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Use case to retrieve all active notes. */
class GetActiveNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getActiveNotes()
}

/** Use case to retrieve all archived notes. */
class GetArchivedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getArchivedNotes()
}

/** Use case to retrieve all locked (vault) notes. */
class GetLockedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getLockedNotes()
}

/** Use case to retrieve all unique labels. */
class GetAllLabelsUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<String>> = repository.getAllLabels()
}

/** Use case to save or update a note. */
class SaveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Unit> = repository.saveNote(note)
}

/** Use case to update multiple notes (e.g., for reordering). */
class UpdateNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(notes: List<Note>): Result<Unit> = repository.updateNotes(notes)
}

/** Use case to get a specific note by ID. */
class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String): Result<Note> = repository.getNoteById(id)
}

/** Use case to move a note to trash. */
class MoveNoteToTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.moveToTrash(id)
}