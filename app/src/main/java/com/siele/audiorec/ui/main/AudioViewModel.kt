package com.siele.audiorec.ui.main

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siele.audiorec.data.database.AudioRecordingsDao
import com.siele.audiorec.data.model.AudioRecording
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltViewModel
class AudioViewModel @Inject constructor(private val audioRecordingsDao: AudioRecordingsDao):ViewModel() {

    val audios = mutableStateOf(audioRecordingsDao.getAudioRecording())
    val elapsedTime = mutableStateOf(0)

    fun insertRecordings(audioRecording: AudioRecording){
        viewModelScope.launch {
            audioRecordingsDao.insertRecordings(audioRecording)
        }
    }

    fun getRecordings() {
        audios.value = audioRecordingsDao.getAudioRecording()
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

    private fun timerPadding(value: Int): String {
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
        audioRecording: AudioRecording,
        mediaPlayer: MutableState<MediaPlayer?>,
        isPaused: MutableState<Boolean>
    ) {
        mediaPlayer.value?.pause()
        isPaused.value = true
        audioRecording.isPlaying = false
        audioRecording.isFinished = false

    }
    fun resumePlay(
        audioRecording: AudioRecording,
        mediaPlayer: MutableState<MediaPlayer?>,
        isPaused: MutableState<Boolean>,
    ) {
        mediaPlayer.value?.start()
        isPaused.value = false
        audioRecording.isPlaying = true
        audioRecording.isFinished = false

    }

    fun playFinished(
        audioRecording: AudioRecording,
        recordings:List<AudioRecording>,
        mediaPlayer: MutableState<MediaPlayer?>,
    ) {
        mediaPlayer.value?.setOnCompletionListener {
            audioRecording.isFinished = true
            audioRecording.isPlaying = false
            recordings.forEach {
                it.isPlaying = false
                it.isFinished = it==audioRecording
            }
            audios.value = flowOf(recordings)
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
            mediaPlayer.value = null
        }


    }

    fun playRecording(
        audioRecording: AudioRecording,
        audioRecordings:List<AudioRecording>,
        mediaPlayer: MutableState<MediaPlayer?>
    ) {
        mediaPlayer.value?.stop()
        mediaPlayer.value = MediaPlayer()
        try {
            mediaPlayer.value?.apply {
                reset()
                setDataSource(audioRecording.filePath)
                prepare()
                start()
            }
            audioRecording.isPlaying = true
            audioRecording.isFinished = false

            audioRecordings.forEach {
                it.isPlaying = it==audioRecording
            }
            audios.value = flowOf(audioRecordings)
        }catch (e: IOException){
            e.printStackTrace()
        }
    }



}