package com.magpiny.notafo.di

import android.content.Context
import androidx.room.Room
import com.magpiny.notafo.data.db.AttachmentDao
import com.magpiny.notafo.data.db.NoteDao
import com.magpiny.notafo.data.db.NoteDatabase
import com.magpiny.notafo.data.db.TaskDao
import com.magpiny.notafo.data.repository.AttachmentRepositoryImpl
import com.magpiny.notafo.data.repository.FileRepositoryImpl
import com.magpiny.notafo.data.repository.NoteRepositoryImpl
import com.magpiny.notafo.data.repository.SettingsRepositoryImpl
import com.magpiny.notafo.data.repository.TaskRepositoryImpl
import com.magpiny.notafo.domain.repository.AttachmentRepository
import com.magpiny.notafo.domain.repository.FileRepository
import com.magpiny.notafo.domain.repository.NoteRepository
import com.magpiny.notafo.domain.repository.SettingsRepository
import com.magpiny.notafo.domain.repository.TaskRepository
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
            ).build()
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
