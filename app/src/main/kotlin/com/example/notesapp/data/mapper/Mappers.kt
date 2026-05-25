package com.example.notesapp.data.mapper

import com.example.notesapp.data.db.NoteEntity
import com.example.notesapp.domain.model.Note

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
