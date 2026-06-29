package com.magpiny.notafo.domain.usecase

import android.net.Uri
import com.magpiny.notafo.domain.repository.FileRepository
import com.magpiny.notafo.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Retrieves all active notes and exports them to a user-selected JSON file.
 */
class ExportNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(destinationUri: Uri): Result<Unit> {
        return try {
            val notes = noteRepository.getActiveNotes().first()
            fileRepository.exportNotesToJson(destinationUri, notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Imports notes from a JSON file and saves them to the local Room database.
 */
class ImportNotesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(sourceUri: Uri): Result<Int> {
        return try {
            val result = fileRepository.importNotesFromJson(sourceUri)
            if (result.isSuccess) {
                val notes = result.getOrThrow()
                notes.forEach { noteRepository.saveNote(it) }
                Result.success(notes.size)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
