package com.magpiny.notafo.data.mapper

import com.magpiny.notafo.data.db.NoteEntity
import com.magpiny.notafo.domain.model.Note

/**
 * Maps [NoteEntity] to [Note] domain model.
 */
fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    color = color,
    isPinned = isPinned,
    isTrashed = isTrashed,
    isArchived = isArchived,
    isLocked = isLocked,
    labels = if (labels.isBlank()) emptyList() else labels.split(","),
    notebookId = notebookId,
    position = position
)

/**
 * Maps [Note] domain model to [NoteEntity].
 */
fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    color = color,
    isPinned = isPinned,
    isTrashed = isTrashed,
    isArchived = isArchived,
    isLocked = isLocked,
    labels = labels.joinToString(","),
    notebookId = notebookId,
    position = position
)
