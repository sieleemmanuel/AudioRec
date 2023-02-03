package com.siele.audiorec.di

import android.content.Context
import com.siele.audiorec.data.database.AudioRecordingsDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAudioRecordingsDb(@ApplicationContext context: Context) =
        AudioRecordingsDb.getInstance(context)

    @Provides
    @Singleton
    fun provideAudioRecordingsDao(db: AudioRecordingsDb) = db.audiosDao
}