package com.example.notesapp.domain.usecase

import com.example.notesapp.domain.model.Attachment
import com.example.notesapp.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAttachmentsUseCase @Inject constructor(private val repository: AttachmentRepository) {
    operator fun invoke(noteId: String): Flow<List<Attachment>> = repository.getAttachmentsForNote(noteId)
}

class SaveAttachmentUseCase @Inject constructor(private val repository: AttachmentRepository) {
    suspend operator fun invoke(attachment: Attachment): Result<Unit> = repository.saveAttachment(attachment)
}

class DeleteAttachmentUseCase @Inject constructor(private val repository: AttachmentRepository) {
    suspend operator fun invoke(attachment: Attachment): Result<Unit> = repository.deleteAttachment(attachment)
}
