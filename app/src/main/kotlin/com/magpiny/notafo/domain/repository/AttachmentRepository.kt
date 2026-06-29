package com.magpiny.notafo.domain.repository

import com.magpiny.notafo.domain.model.Attachment
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAttachmentsForNote(noteId: String): Flow<List<Attachment>>
    suspend fun saveAttachment(attachment: Attachment): Result<Unit>
    suspend fun deleteAttachment(attachment: Attachment): Result<Unit>
}
