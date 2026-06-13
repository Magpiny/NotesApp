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
        SubtaskEntity::class,
        ProjectEntity::class,
        FocusSessionEntity::class,
        TaskNoteCrossRef::class,
        AttachmentEntity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun attachmentDao(): AttachmentDao
}
