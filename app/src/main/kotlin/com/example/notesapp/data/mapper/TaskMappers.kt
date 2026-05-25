package com.example.notesapp.data.mapper

import com.example.notesapp.data.db.*
import com.example.notesapp.domain.model.*

fun TaskEntity.toDomain(subtasks: List<Subtask> = emptyList()): Task = Task(
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
    labels = if (labels.isBlank()) emptyList() else labels.split(","),
    subtasks = subtasks
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

fun SubtaskEntity.toDomain(): Subtask = Subtask(
    id = id,
    taskId = taskId,
    title = title,
    isCompleted = isCompleted,
    position = position
)

fun Subtask.toEntity(): SubtaskEntity = SubtaskEntity(
    id = id,
    taskId = taskId,
    title = title,
    isCompleted = isCompleted,
    position = position
)
