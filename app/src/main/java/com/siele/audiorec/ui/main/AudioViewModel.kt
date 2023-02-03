package com.siele.audiorec.ui.main

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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(private val audioRecordingsDao: AudioRecordingsDao):ViewModel() {

    val audios = mutableStateOf(audioRecordingsDao.getAudioRecording())
    val elapsedTime = mutableStateOf(0)

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


}