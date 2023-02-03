@file:Suppress("DEPRECATION")

package com.siele.audiorec.ui.main

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.siele.audiorec.R
import com.siele.audiorec.data.model.AudioRecording
import com.siele.audiorec.ui.theme.AudioRecTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Record(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    recordPerm: Boolean,
    writePerm: Boolean,
    permissionState: MultiplePermissionsState
) {
    val audioViewModel:AudioViewModel = hiltViewModel()
    val context = LocalContext.current
    val isRecording = remember { mutableStateOf(false) }
    val isPaused = remember { mutableStateOf(false) }
    val audioName = remember { mutableStateOf("") }
    val startTime = remember { mutableStateOf(0L) }
    val endTime = remember { mutableStateOf(0L) }
    val stampFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
    val timestamp = stampFormat.format(Calendar.getInstance().time)
    val fileName = "audio$timestamp.mp3"
    val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/$fileName"
    val file = File(filePath)
    val audioRecorder = remember { mutableStateOf<MediaRecorder?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = modifier.height(20.dp))
            Text(text = getTimerLabel(audioViewModel.elapsedTime.value), fontSize = 20.sp)
            Spacer(modifier = modifier.height(20.dp))
            Text(text = audioName.value)
        }
        Row(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            IconButton(
            onClick = { navController.popBackStack() },
            modifier = modifier
                .padding(10.dp)
                .clip(shape = CircleShape)
                .background(Color.LightGray)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }

            IconButton(
                onClick = {
                    if (recordPerm) {
                        audioName.value = fileName
                        when{
                            isPaused.value  -> {
                                audioRecorder.value?.let { resumeRecording(it, isPaused, isRecording) }
                                audioViewModel.audioTimer(false)
                            }
                            isRecording.value  -> {
                                audioViewModel.audioTimer(true)
                                audioRecorder.value?.let { pauseRecording(it, isPaused) }
                            }
                            else  -> {
                                recordAudio(
                                    audioRecorder,
                                    filePath,
                                    isRecording,
                                    isPaused,
                                    context,
                                    startTime)
                            }
                        }

                    }else{
                        permissionState.launchMultiplePermissionRequest()
                    }
                },
                modifier = modifier
                    .padding(10.dp)
                    .clip(shape = CircleShape)
                    .background(Color.LightGray)
            ) {
                Icon(
                    painter = painterResource(id = if (isRecording.value) {
                        R.drawable.ic_pause
                    }else{
                        R.drawable.ic_record
                    }),
                    contentDescription = null
                )
            }

            IconButton(
                onClick = {
                    audioRecorder.value?.let { stopRecording(it, isRecording, endTime) }
                    val audioRecording = AudioRecording(
                        fileName = fileName,
                        filePath = filePath,
                        duration = getTimerLabel((endTime.value - startTime.value).toInt())
                    )
                    audioViewModel.insertRecordings(audioRecording)
                    Toast.makeText(context, "Recording saved successfully", Toast.LENGTH_SHORT)
                        .show()
                    navController.popBackStack() },
                modifier = modifier
                    .padding(10.dp)
                    .clip(shape = CircleShape)
                    .background(Color.LightGray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stop),
                    contentDescription = null
                )
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


fun pauseRecording(audioRecorder: MediaRecorder, isPaused: MutableState<Boolean>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        audioRecorder.pause()
    }
    isPaused.value = true
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

private fun recordAudio(
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




@OptIn(ExperimentalPermissionsApi::class)
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun RecordPreview() {
    AudioRecTheme {
        Record(
            navController = rememberNavController(),
            recordPerm = true,
            writePerm = true,
            permissionState =  rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
        )
    }
}