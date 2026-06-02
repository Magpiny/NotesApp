package com.example.notesapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.notesapp.domain.model.Note
import com.example.notesapp.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [FileRepository] utilizing Android ContentResolver and kotlinx.serialization.
 */
@Singleton
class FileRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : FileRepository {

    private val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportNotesToJson(uri: Uri, notes: List<Note>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val jsonString = jsonFormat.encodeToString(notes)
                outputStream.write(jsonString.toByteArray())
            } ?: throw IllegalStateException("Unable to open output stream")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importNotesFromJson(uri: Uri): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val notes = jsonFormat.decodeFromString<List<Note>>(jsonString)
                Result.success(notes)
            } ?: throw IllegalStateException("Unable to open input stream")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
