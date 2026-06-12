package com.example.notesapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY position ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Update
    suspend fun updateTasks(tasks: List<TaskEntity>)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Projects
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    // Focus Sessions
    @Insert
    suspend fun insertFocusSession(session: FocusSessionEntity)

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE isBreak = 0 AND startTime >= :since")
    fun getFocusSessionCount(since: Long): Flow<Int>

    // Links (Bi-directional linking)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskNoteLink(ref: TaskNoteCrossRef)

    @Delete
    suspend fun deleteTaskNoteLink(ref: TaskNoteCrossRef)

    @Query("""
        SELECT notes.* FROM notes
        JOIN task_note_cross_ref ON notes.id = task_note_cross_ref.noteId
        WHERE task_note_cross_ref.taskId = :taskId
    """)
    fun getNotesForTask(taskId: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT tasks.* FROM tasks
        JOIN task_note_cross_ref ON tasks.id = task_note_cross_ref.taskId
        WHERE task_note_cross_ref.noteId = :noteId
    """)
    fun getTasksForNote(noteId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND status != 'CANCELLED'
        ORDER BY updatedAt DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>
}
