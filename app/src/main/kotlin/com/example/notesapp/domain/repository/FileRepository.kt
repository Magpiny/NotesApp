package com.example.notesapp.domain.repository

import android.net.Uri
import com.example.notesapp.domain.model.Note

/**
 * Interface for reading and writing files via the Storage Access Framework (SAF).
 */
interface FileRepository {

    /**
     * Serializes and exports a list of notes to the specified document URI.
     */
    suspend fun exportNotesToJson(uri: Uri, notes: List<Note>): Result<Unit>

    /**
     * Reads and deserializes notes from a specified document URI.
     */
    suspend fun importNotesFromJson(uri: Uri): Result<List<Note>>
}
