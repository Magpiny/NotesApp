package com.example.notesapp.data.db

import androidx.room.*
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.domain.model.TaskStatus

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val color: Long
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: Long?,
    val position: Int,
    val projectId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val recurrencePattern: String?,
    val recurrenceId: String?,
    val labels: String // Comma separated
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val duration: Long,
    val isBreak: Boolean
)

@Entity(
    tableName = "task_note_cross_ref",
    primaryKeys = ["taskId", "noteId"],
    indices = [Index("noteId")]
)
data class TaskNoteCrossRef(
    val taskId: String,
    val noteId: String
)
