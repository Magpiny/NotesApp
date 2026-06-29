package com.magpiny.notafo.domain.usecase

import com.magpiny.notafo.domain.model.Attachment
import com.magpiny.notafo.domain.repository.AttachmentRepository
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
