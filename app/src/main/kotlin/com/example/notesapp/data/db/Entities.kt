package com.example.notesapp.data.db

import androidx.room.*

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

/**
 * Room entity representing a Media Attachment (Image, Audio, Sketch).
 */
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val localPath: String,
    val type: String, // "IMAGE", "AUDIO", "SKETCH"
    val ocrText: String? = null,
    val createdAt: Long
)
