package com.magpiny.notafo.data.repository

import com.magpiny.notafo.data.db.*
import com.magpiny.notafo.data.mapper.*
import com.magpiny.notafo.domain.model.*
import com.magpiny.notafo.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return dao.searchTasks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val entity = dao.getTaskById(id)
            if (entity != null) Result.success(entity.toDomain())
            else Result.failure(Exception("Task not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertTask(task.toEntity())
            // Also insert/update subtasks if any
            task.subtasks.forEach { subtask ->
                dao.insertSubtask(subtask.toEntity())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTasks(tasks: List<Task>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.updateTasks(tasks.map { it.toEntity() })
            // Subtasks update logic could be more complex (syncing), but keeping it simple for now
            tasks.forEach { task ->
                task.subtasks.forEach { subtask ->
                    dao.insertSubtask(subtask.toEntity())
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteTask(task.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllProjects(): Flow<List<Project>> {
        return dao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createProject(project: Project): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertProject(project.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotesForTask(taskId: String): Flow<List<Note>> {
        return dao.getNotesForTask(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksForNote(noteId: String): Flow<List<Task>> {
        return dao.getTasksForNote(noteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun linkTaskToNote(taskId: String, noteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertTaskNoteLink(TaskNoteCrossRef(taskId, noteId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkTaskFromNote(taskId: String, noteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteTaskNoteLink(TaskNoteCrossRef(taskId, noteId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
