package com.example.notesapp.domain.repository

import com.example.notesapp.domain.model.Attachment
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAttachmentsForNote(noteId: String): Flow<List<Attachment>>
    suspend fun saveAttachment(attachment: Attachment): Result<Unit>
    suspend fun deleteAttachment(attachment: Attachment): Result<Unit>
}
