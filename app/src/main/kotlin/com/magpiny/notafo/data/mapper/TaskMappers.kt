package com.magpiny.notafo.data.mapper

import com.magpiny.notafo.data.db.*
import com.magpiny.notafo.domain.model.*

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    dueDate = dueDate,
    position = position,
    projectId = projectId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    recurrencePattern = recurrencePattern,
    recurrenceId = recurrenceId,
    labels = if (labels.isBlank()) emptyList() else labels.split(",")
)

fun SubtaskEntity.toDomain(): Subtask = Subtask(
    id = id,
    taskId = taskId,
    title = title,
    isCompleted = isCompleted,
    position = position
)

fun TaskWithSubtasks.toDomain(): Task = task.toDomain().copy(
    subtasks = subtasks.map { it.toDomain() }
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    dueDate = dueDate,
    position = position,
    projectId = projectId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    recurrencePattern = recurrencePattern,
    recurrenceId = recurrenceId,
    labels = labels.joinToString(",")
)

fun Subtask.toEntity(): SubtaskEntity = SubtaskEntity(
    id = id,
    taskId = taskId,
    title = title,
    isCompleted = isCompleted,
    position = position
)

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    title = title,
    color = color
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    title = title,
    color = color
)
