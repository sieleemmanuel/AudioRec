package com.siele.audiorec.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siele.audiorec.data.model.AudioRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordingsDao {
    @Insert
    suspend fun insertRecordings(audio:AudioRecording)

    @Query("SELECT * FROM audios_table")
    fun getAudioRecording():Flow<List<AudioRecording>>
}