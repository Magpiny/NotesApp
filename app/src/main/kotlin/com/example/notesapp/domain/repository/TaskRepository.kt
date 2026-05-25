package com.example.notesapp.domain.repository

import com.example.notesapp.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Tasks
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: String): Result<Task>
    suspend fun saveTask(task: Task): Result<Unit>
    suspend fun updateTasks(tasks: List<Task>): Result<Unit>
    suspend fun deleteTask(task: Task): Result<Unit>

    // Projects
    fun getAllProjects(): Flow<List<Project>>
    suspend fun createProject(project: Project): Result<Unit>

    // Subtasks
    fun getSubtasksForTask(taskId: String): Flow<List<Subtask>>
    suspend fun saveSubtask(subtask: Subtask): Result<Unit>
    suspend fun deleteSubtask(subtask: Subtask): Result<Unit>

    // Links
    fun getNotesForTask(taskId: String): Flow<List<Note>>
    fun getTasksForNote(noteId: String): Flow<List<Task>>
    suspend fun linkTaskToNote(taskId: String, noteId: String): Result<Unit>
    suspend fun unlinkTaskFromNote(taskId: String, noteId: String): Result<Unit>
}
