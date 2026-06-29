package com.magpiny.notafo.data.db

import androidx.room.*
import com.magpiny.notafo.domain.model.TaskPriority
import com.magpiny.notafo.domain.model.TaskStatus

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

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class SubtaskEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val title: String,
    val isCompleted: Boolean,
    val position: Int
)

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>
)
