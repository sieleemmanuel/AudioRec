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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
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
    val audioRecorder = remember { mutableStateOf<MediaRecorder?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
       /*     Spacer(modifier = modifier.height(20.dp))
            Text(text = audioViewModel.getTimerLabel(audioViewModel.elapsedTime.value), fontSize = 20.sp)
         */   Spacer(modifier = modifier.height(20.dp))
            Text(text = audioName.value)
        }
        Row(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Button(
            onClick = {
                audioRecorder.value?.release()
                audioRecorder.value = null
                navController.popBackStack()
                      },
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
                Text(text = "Cancel")
        }

            Button(
                onClick = {
                    if (recordPerm) {
                        audioName.value = fileName
                        when{
                            isPaused.value  -> {
                                audioRecorder.value?.let { audioViewModel.resumeRecording(it, isPaused, isRecording) }
                                audioViewModel.audioTimer(false)
                            }
                            isRecording.value  -> {
                                audioViewModel.audioTimer(true)
                                audioRecorder.value?.let { audioViewModel.pauseRecording(it, isPaused, isRecording) }
                            }
                            else  -> {
                                audioViewModel.recordAudio(
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
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (isRecording.value) {
                        R.drawable.ic_pause
                    }else{
                        R.drawable.ic_record
                    }),
                    contentDescription = null
                )
                Text(text = if (isRecording.value) "Pause" else "Record")
            }

            if (isRecording.value) {
                Button(
                    onClick = {
                        audioRecorder.value?.let {
                            audioViewModel.stopRecording(
                                it,
                                isRecording,
                                endTime
                            )
                        }
                        val duration = (endTime.value - startTime.value).toInt()
                        val audioRecording = AudioRecording(
                            fileName = fileName,
                            filePath = filePath,
                            duration = audioViewModel.getTimerLabel(duration)
                        )
                        audioViewModel.insertRecordings(audioRecording)
                        audioViewModel.getRecordings()
                        Toast.makeText(context, "Recording saved successfully", Toast.LENGTH_SHORT)
                            .show()
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null
                    )
                    Text(text = "Save")
                }
            }
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
            permissionState =  rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
        )
    }
}