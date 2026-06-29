package com.magpiny.notafo.data.mapper

import com.magpiny.notafo.data.db.TaskEntity
import com.magpiny.notafo.domain.model.TaskPriority
import com.magpiny.notafo.domain.model.TaskStatus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TaskMappersTest {

    @Test
    fun `TaskEntity toDomain should correctly map fields`() {
        val entity = TaskEntity(
            id = "1",
            title = "Title",
            description = "Desc",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
            dueDate = 12345L,
            position = 0,
            projectId = "p1",
            createdAt = 100L,
            updatedAt = 200L,
            recurrencePattern = null,
            recurrenceId = null,
            labels = "tag1,tag2"
        )

        val domain = entity.toDomain()

        domain.id shouldBe "1"
        domain.title shouldBe "Title"
        domain.labels shouldBe listOf("tag1", "tag2")
        domain.priority shouldBe TaskPriority.HIGH
    }

    @Test
    fun `Task toEntity should correctly map fields`() {
        val task = com.magpiny.notafo.domain.model.Task(
            id = "1",
            title = "Title",
            description = "Desc",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
            dueDate = 12345L,
            position = 0,
            projectId = "p1",
            createdAt = 100L,
            updatedAt = 200L,
            recurrencePattern = null,
            recurrenceId = null,
            labels = listOf("tag1", "tag2")
        )

        val entity = task.toEntity()

        entity.id shouldBe "1"
        entity.labels shouldBe "tag1,tag2"
    }
}
