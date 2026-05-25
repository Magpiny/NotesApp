package com.example.notesapp.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a Note.
 */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val color: Long,
    val isPinned: Boolean,
    val isTrashed: Boolean,
    val isArchived: Boolean,
    val isLocked: Boolean = false,
    val labels: List<String>,
    val notebookId: String?,
    val position: Int = 0
)
