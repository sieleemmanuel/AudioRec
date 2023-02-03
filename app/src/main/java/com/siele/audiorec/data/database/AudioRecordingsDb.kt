package com.siele.audiorec.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.siele.audiorec.data.model.AudioRecording

@Database(entities = [AudioRecording::class], version = 1)
abstract class AudioRecordingsDb:RoomDatabase() {
    abstract val audiosDao:AudioRecordingsDao

    companion object{
        @Volatile
        private var INSTANCE:AudioRecordingsDb? = null

        fun getInstance(context: Context): AudioRecordingsDb{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context,
                        AudioRecordingsDb::class.java,
                        "audio_recordings_db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}