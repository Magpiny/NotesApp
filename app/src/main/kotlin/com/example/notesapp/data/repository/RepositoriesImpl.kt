package com.example.notesapp.data.repository

import com.example.notesapp.data.db.NoteDao
import com.example.notesapp.data.mapper.toDomain
import com.example.notesapp.data.mapper.toEntity
import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [NoteRepository] using Room database.
 */
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun getActiveNotes(): Flow<List<Note>> {
        return dao.getActiveNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getArchivedNotes(): Flow<List<Note>> {
        return dao.getArchivedNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getLockedNotes(): Flow<List<Note>> {
        return dao.getLockedNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        // FTS4 prefix matching: "term*"
        // We sanitize to remove double quotes which could break MATCH syntax
        val sanitizedQuery = query.replace("\"", "")
        val ftsQuery = if (sanitizedQuery.isBlank()) "" else "$sanitizedQuery*"

        return dao.searchNotes(ftsQuery).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getNoteById(id: String): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val note = dao.getNoteById(id)?.toDomain()
            if (note != null) Result.success(note)
            else Result.failure(Exception("Note not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveNote(note: Note): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertNote(note.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotes(notes: List<Note>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.updateNotes(notes.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllLabels(): Flow<List<String>> {
        return dao.getAllLabels().map { labelsList ->
            labelsList
                .flatMap { it.split(",") }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
    }

    override suspend fun moveToTrash(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.moveToTrash(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteNote(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cleanOldTrash(thresholdMillis: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteOldTrashedNotes(thresholdMillis)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}