package com.example.notesapp.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.model.TaskPriority
import com.example.notesapp.domain.model.TaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NoteDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoteDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAllTasks() = runBlocking {
        val task = TaskEntity(
            id = "1",
            title = "Test Task",
            description = "",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            dueDate = null,
            position = 0,
            projectId = null,
            createdAt = 0L,
            updatedAt = 0L,
            recurrencePattern = null,
            recurrenceId = null,
            labels = ""
        )
        dao.insertTask(task)

        val allTasks = dao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Test Task", allTasks[0].task.title)
    }

    @Test
    fun insertSubtaskAndGetWithRelation() = runBlocking {
        val task = TaskEntity(
            id = "1",
            title = "Parent",
            description = "",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            dueDate = null,
            position = 0,
            projectId = null,
            createdAt = 0L,
            updatedAt = 0L,
            recurrencePattern = null,
            recurrenceId = null,
            labels = ""
        )
        dao.insertTask(task)

        val subtask = SubtaskEntity(
            id = "s1",
            taskId = "1",
            title = "Child",
            isCompleted = false,
            position = 0
        )
        dao.insertSubtask(subtask)

        val taskWithSubtasks = dao.getTaskById("1")
        assertEquals(1, taskWithSubtasks?.subtasks?.size)
        assertEquals("Child", taskWithSubtasks?.subtasks?.get(0)?.title)
    }
}
