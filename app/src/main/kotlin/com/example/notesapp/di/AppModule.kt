package com.example.notesapp.di

import android.content.Context
import androidx.room.Room
import com.example.notesapp.data.db.AttachmentDao
import com.example.notesapp.data.db.NoteDao
import com.example.notesapp.data.db.NoteDatabase
import com.example.notesapp.data.db.TaskDao
import com.example.notesapp.data.repository.AttachmentRepositoryImpl
import com.example.notesapp.data.repository.FileRepositoryImpl
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.data.repository.SettingsRepositoryImpl
import com.example.notesapp.data.repository.TaskRepositoryImpl
import com.example.notesapp.domain.repository.AttachmentRepository
import com.example.notesapp.domain.repository.FileRepository
import com.example.notesapp.domain.repository.NoteRepository
import com.example.notesapp.domain.repository.SettingsRepository
import com.example.notesapp.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository

    companion object {
        @Provides
        @Singleton
        fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase {
            return Room.databaseBuilder(
                context,
                NoteDatabase::class.java,
                "notes_db"
            ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        @Provides
        @Singleton
        fun provideNoteDao(database: NoteDatabase): NoteDao {
            return database.noteDao()
        }

        @Provides
        @Singleton
        fun provideTaskDao(database: NoteDatabase): TaskDao {
            return database.taskDao()
        }

        @Provides
        @Singleton
        fun provideAttachmentDao(database: NoteDatabase): AttachmentDao {
            return database.attachmentDao()
        }
    }
}
