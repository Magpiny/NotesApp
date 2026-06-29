package com.magpiny.notafo.data.repository

import com.magpiny.notafo.data.db.AttachmentDao
import com.magpiny.notafo.data.mapper.toDomain
import com.magpiny.notafo.data.mapper.toEntity
import com.magpiny.notafo.domain.model.Attachment
import com.magpiny.notafo.domain.repository.AttachmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val dao: AttachmentDao
) : AttachmentRepository {

    override fun getAttachmentsForNote(noteId: String): Flow<List<Attachment>> {
        return dao.getAttachmentsForNote(noteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveAttachment(attachment: Attachment): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertAttachment(attachment.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAttachment(attachment: Attachment): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteAttachment(attachment.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
