package com.example.notesapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database instance providing DAOs.
 */
@Database(
    entities = [
        NoteEntity::class, 
        NoteFtsEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        SubtaskEntity::class,
        FocusSessionEntity::class,
        TaskNoteCrossRef::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
