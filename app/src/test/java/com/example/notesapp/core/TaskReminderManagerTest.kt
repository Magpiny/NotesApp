package com.example.notesapp.core

import android.app.AlarmManager
import android.content.Context
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TaskReminderManagerTest {

    private val alarmManager: AlarmManager = mockk()
    private val context: Context = mockk {
        every { getSystemService(Context.ALARM_SERVICE) } returns alarmManager
    }
    private val manager = TaskReminderManager(context)

    @Test
    fun `generateRequestCode should produce unique codes for different stages`() {
        val taskId = "test-uuid"
        val code0 = manager.generateRequestCode(taskId, 0)
        val code5 = manager.generateRequestCode(taskId, 5)
        val code10 = manager.generateRequestCode(taskId, 10)

        code0 shouldNotBe code5
        code5 shouldNotBe code10
        code0 shouldNotBe code10
    }

    @Test
    fun `generateRequestCode should produce unique codes for different tasks`() {
        val task1 = "uuid-1"
        val task2 = "uuid-2"
        val code1 = manager.generateRequestCode(task1, 0)
        val code2 = manager.generateRequestCode(task2, 0)

        code1 shouldNotBe code2
    }
}
