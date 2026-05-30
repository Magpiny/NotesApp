package com.example.notesapp.domain.repository

import com.example.notesapp.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining note repository operations.
 */
interface NoteRepository {
    fun getActiveNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getTrashedNotes(): Flow<List<Note>>
    fun getLockedNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Result<Note>
    suspend fun saveNote(note: Note): Result<Unit>
    suspend fun updateNotes(notes: List<Note>): Result<Unit>
    fun getAllLabels(): Flow<List<String>>
    suspend fun moveToTrash(id: String): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>
    suspend fun cleanOldTrash(thresholdMillis: Long): Result<Unit>
}