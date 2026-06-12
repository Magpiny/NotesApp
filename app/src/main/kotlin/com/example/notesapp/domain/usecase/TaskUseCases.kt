package com.example.notesapp.domain.usecase

import com.example.notesapp.domain.model.*
import com.example.notesapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getAllTasks()
}

class GetTaskByIdUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(id: String): Result<Task> = repository.getTaskById(id)
}

class SaveTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Unit> = repository.saveTask(task)
}

class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Unit> = repository.deleteTask(task)
}

class UpdateTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(tasks: List<Task>): Result<Unit> = repository.updateTasks(tasks)
}

class GetAllProjectsUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Project>> = repository.getAllProjects()
}

class CreateProjectUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(project: Project): Result<Unit> = repository.createProject(project)
}

class LinkTaskToNoteUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String, noteId: String): Result<Unit> = repository.linkTaskToNote(taskId, noteId)
}

class GetTasksForNoteUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(noteId: String): Flow<List<Task>> = repository.getTasksForNote(noteId)
}
