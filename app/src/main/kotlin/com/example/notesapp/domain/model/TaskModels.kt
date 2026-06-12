package com.example.notesapp.domain.model

import kotlinx.serialization.Serializable

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    TODO, IN_PROGRESS, COMPLETED, CANCELLED
}

@Serializable
data class Project(
    val id: String,
    val title: String,
    val color: Long
)

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: Long?,
    val position: Int,
    val projectId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val recurrencePattern: String? = null, // e.g., "DAILY", "WEEKLY", "MONTHLY"
    val recurrenceId: String? = null,
    val labels: List<String> = emptyList()
)
