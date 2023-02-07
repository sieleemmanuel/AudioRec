package com.siele.audiorec.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "audios_table")
data class AudioRecording(
    val fileName:String,
    val filePath:String,
    val duration: String,
){
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
    @Ignore
    var isPlaying:Boolean = false
    @Ignore
    var isFinished:Boolean = false
}
