package com.siele.audiorec.ui.main

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siele.audiorec.data.database.AudioRecordingsDao
import com.siele.audiorec.data.model.AudioRecording
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(private val audioRecordingsDao: AudioRecordingsDao):ViewModel() {

    val audios = mutableStateOf(audioRecordingsDao.getAudioRecording())
    val elapsedTime = mutableStateOf(0)

   val  isPLaying = mutableStateOf<Map<Int,Boolean>>(emptyMap())

    fun insertRecordings(audioRecording: AudioRecording){
        viewModelScope.launch {
            audioRecordingsDao.insertRecordings(audioRecording)
        }
    }

    fun audioTimer(recording:Boolean) {
         viewModelScope.launch{
            flow {
                while (recording){
                    emit(elapsedTime.value)
                    delay(1000)
                    elapsedTime.value++
                }
            }
        }
    }



    fun getTimerLabel(value: Int): String {
        return "${timerPadding(value/60)}:${timerPadding(value % 60)}"
    }

    fun timerPadding(value: Int): String {
        return if (value<10){
            ("0$value")
        }else{
            ""+value
        }
    }


    fun pauseRecording(
        audioRecorder: MediaRecorder,
        isPaused: MutableState<Boolean>,
        isRecording: MutableState<Boolean>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioRecorder.pause()
        }
        isPaused.value = true
        isRecording.value = false
    }

    fun resumeRecording(
        audioRecorder: MediaRecorder,
        isPaused: MutableState<Boolean>,
        isRecording: MutableState<Boolean>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioRecorder.resume()
        }
        isPaused.value = false
        isRecording.value = true
    }

    fun recordAudio(
        audioRecorder: MutableState<MediaRecorder?>,
        filePath: String,
        isRecording: MutableState<Boolean>,
        isPaused: MutableState<Boolean>,
        context: Context,
        startTime: MutableState<Long>
    ) {
        audioRecorder.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        try {
            audioRecorder.value?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(filePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
                startTime.value = System.currentTimeMillis()
                isRecording.value = true
                isPaused.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording(
        audioRecorder: MediaRecorder,
        isRecording: MutableState<Boolean>,
        endTime: MutableState<Long>
    ) {
        if (isRecording.value){
            audioRecorder.stop()
            endTime.value = System.currentTimeMillis()
            isRecording.value = false
            audioRecorder.apply {
                reset()
                release()
            }
        }
    }


    fun pausePlay(
        mediaPlayer: MutableState<MediaPlayer?>,
        isPlaying: MutableState<Boolean>
    ) {
        mediaPlayer.value?.pause()
        isPlaying.value = false

    }

    fun playRecording(
        audioRecording: AudioRecording,
        isPLaying: MutableState<Boolean>,
        mediaPlayer: MutableState<MediaPlayer?>
    ) {
        mediaPlayer.value = MediaPlayer()
        try {
            mediaPlayer.value?.apply {
                reset()
                setDataSource(audioRecording.filePath)
                prepare()
                start()
            }
            isPLaying.value = true

        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    /*fun setIsPlaying (audioRecording: AudioRecording, audioRecordings: List<AudioRecording>){
        audioRecordings.find { it==audioRecording }?.isPlaying = true
        audios.value= flowOf(audioRecordings)
    }

    fun setIsPaused (audioRecording: AudioRecording, audioRecordings: List<AudioRecording>){
        audioRecordings.find { it==audioRecording }?.isPlaying = false
        audios.value = flowOf(audioRecordings)
    }*/


}