@file:Suppress("DEPRECATION")

package com.siele.audiorec.ui.main

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.siele.audiorec.R
import com.siele.audiorec.ui.theme.AudioRecTheme
import java.io.File
import java.sql.Timestamp
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
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = modifier.height(20.dp))
            Text(text = "00:00:06")
            Spacer(modifier = modifier.height(20.dp))
            Text(text = "Aud20230202121408.mp3")
        }
        Row(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                onClick = {
                    if (recordPerm) {
                        recordAudio(context)
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
                    painter = painterResource(id = R.drawable.ic_record),
                    contentDescription = null
                )
            }

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
        }
    }

}

private fun recordAudio(context: Context) {
    val stampFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
    val timestamp = stampFormat.format(Calendar.getInstance().time)
    val fileName = "audio$timestamp"
    val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/$fileName.3gp"
    val file = File(filePath)
    val audioRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }
    try {
        audioRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
        }

        audioRecorder.start()

    } catch (e: Exception) {
        e.printStackTrace()
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(showBackground = true)
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