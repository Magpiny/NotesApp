package com.magpiny.notafo.data.db

import androidx.room.TypeConverter
import com.magpiny.notafo.domain.model.TaskPriority
import com.magpiny.notafo.domain.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
}
