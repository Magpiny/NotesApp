package com.example.notesapp.data.repository

import com.example.notesapp.data.db.*
import com.example.notesapp.data.mapper.*
import com.example.notesapp.domain.model.*
import com.example.notesapp.domain.repository.TaskRepository
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
        return dao.getAllTasks().map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return dao.searchTasks(query).map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val relation = dao.getTaskById(id)
            if (relation != null) Result.success(relation.toDomain())
            else Result.failure(Exception("Task not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertTask(task.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTasks(tasks: List<Task>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.updateTasks(tasks.map { it.toEntity() })
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

    override fun getSubtasksForTask(taskId: String): Flow<List<Subtask>> {
        return dao.getSubtasksForTask(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSubtask(subtask: Subtask): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertSubtask(subtask.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubtask(subtask: Subtask): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteSubtask(subtask.toEntity())
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
