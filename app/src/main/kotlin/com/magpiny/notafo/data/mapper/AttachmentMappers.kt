package com.magpiny.notafo.data.mapper

import com.magpiny.notafo.data.db.AttachmentEntity
import com.magpiny.notafo.domain.model.Attachment
import com.magpiny.notafo.domain.model.AttachmentType

fun AttachmentEntity.toDomain(): Attachment = Attachment(
    id = id,
    noteId = noteId,
    localPath = localPath,
    type = AttachmentType.valueOf(type),
    ocrText = ocrText,
    createdAt = createdAt
)

fun Attachment.toEntity(): AttachmentEntity = AttachmentEntity(
    id = id,
    noteId = noteId,
    localPath = localPath,
    type = type.name,
    ocrText = ocrText,
    createdAt = createdAt
)
