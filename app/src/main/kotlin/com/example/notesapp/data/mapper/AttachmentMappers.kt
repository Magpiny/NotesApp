package com.example.notesapp.data.mapper

import com.example.notesapp.data.db.AttachmentEntity
import com.example.notesapp.domain.model.Attachment
import com.example.notesapp.domain.model.AttachmentType

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
