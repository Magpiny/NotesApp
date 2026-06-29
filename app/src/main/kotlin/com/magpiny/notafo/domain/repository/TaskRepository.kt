package com.magpiny.notafo.domain.repository

import com.magpiny.notafo.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Tasks
    fun getAllTasks(): Flow<List<Task>>
    fun searchTasks(query: String): Flow<List<Task>>
    suspend fun getTaskById(id: String): Result<Task>
    suspend fun saveTask(task: Task): Result<Unit>
    suspend fun updateTasks(tasks: List<Task>): Result<Unit>
    suspend fun deleteTask(task: Task): Result<Unit>

    // Projects
    fun getAllProjects(): Flow<List<Project>>
    suspend fun createProject(project: Project): Result<Unit>

    // Links
    fun getNotesForTask(taskId: String): Flow<List<Note>>
    fun getTasksForNote(noteId: String): Flow<List<Task>>
    suspend fun linkTaskToNote(taskId: String, noteId: String): Result<Unit>
    suspend fun unlinkTaskFromNote(taskId: String, noteId: String): Result<Unit>
}
