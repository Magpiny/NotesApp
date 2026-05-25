package com.example.notesapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Notes.
 */
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isArchived = 0 AND isLocked = 0 ORDER BY isPinned DESC, position ASC, updatedAt DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isArchived = 1 AND isLocked = 0 ORDER BY isPinned DESC, position ASC, updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isLocked = 1 ORDER BY updatedAt DESC")
    fun getLockedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Update
    suspend fun updateNotes(notes: List<NoteEntity>)

    @Query("SELECT labels FROM notes WHERE isTrashed = 0")
    fun getAllLabels(): Flow<List<String>>

    @Query("UPDATE notes SET isTrashed = 1 WHERE id = :id")
    suspend fun moveToTrash(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("""
        SELECT notes.* FROM notes 
        JOIN notes_fts ON notes.id = notes_fts.rowid 
        WHERE notes_fts MATCH :query AND isTrashed = 0 AND isLocked = 0
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("DELETE FROM notes WHERE isTrashed = 1 AND updatedAt < :threshold")
    suspend fun deleteOldTrashedNotes(threshold: Long)
}