package com.example.notesapp.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room entity representing a physical Note in the database.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val color: Long,
    val isPinned: Boolean,
    val isTrashed: Boolean,
    val isArchived: Boolean,
    val isLocked: Boolean = false,
    val labels: String, // Comma separated for simplicity in Room without type converters
    val notebookId: String?,
    val position: Int = 0
)

/**
 * FTS4 entity for full-text search across notes.
 */
@Entity(tableName = "notes_fts")
@Fts4(contentEntity = NoteEntity::class)
data class NoteFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Int,
    val title: String,
    val content: String
)