package com.siele.audiorec.ui.main

import android.Manifest
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.*
import com.siele.audiorec.R
import com.siele.audiorec.data.model.AudioRecording
import com.siele.audiorec.ui.theme.AudioRecTheme
import com.siele.audiorec.util.Screen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioList(
    modifier: Modifier = Modifier,
    navController: NavController,
    readPerm: Boolean,
    permissionState: MultiplePermissionsState
) {
    val scrollState = rememberLazyListState()
    val audiosViewModel:AudioViewModel = hiltViewModel()
    val audioRecordings = audiosViewModel.audios.value.collectAsState(initial = emptyList()).value
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = {
                          navController.navigate(route = Screen.Record.route)
                },
                contentColor = Color.White
            ) {
                Icon(painter = painterResource(
                    id = R.drawable.ic_mic),
                    contentDescription = null)
            }
        }
    ) {
        if (readPerm) {
            if (audioRecordings.isEmpty()){
                Column(modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                    Text(text = "No recordings yet!")
                }
            }else{
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    state = scrollState,
                ) {
                    items(items = audioRecordings) { recording ->
                        AudioItem(
                            modifier = modifier,
                            audioRecording = recording,
                            mediaPlayer = mediaPlayer
                        )
                    }
                }
            }

        }else{
            SideEffect {
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}

@Composable
fun AudioItem(
    modifier: Modifier,
    audioRecording: AudioRecording,

    mediaPlayer: MutableState<MediaPlayer?>
) {

    val isPlaying = remember { mutableStateOf(false) }
    val isPaused = remember { mutableStateOf(false) }
    val isFinished = remember { mutableStateOf(false) }
    val audioViewModel:AudioViewModel = hiltViewModel()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = audioRecording.fileName)
        audioViewModel.playFinished(mediaPlayer, isPlaying, isFinished)
        IconButton(onClick = {
            when{
                !isPlaying.value && !isPaused.value  ->{
                   audioViewModel.playRecording(audioRecording, isPlaying, isFinished ,mediaPlayer)
                }
                isPlaying.value ->{
                    audioViewModel.pausePlay(mediaPlayer, isPlaying, isPaused, isFinished)
                }
                isPaused.value ->{
                    audioViewModel.resumePlay(mediaPlayer, isPlaying, isPaused, isFinished)
                }
                isFinished.value ->{
                    audioViewModel.playRecording(audioRecording, isPlaying, isFinished, mediaPlayer)
                }
                else -> {
                      audioViewModel.playRecording(audioRecording, isPlaying, isFinished, mediaPlayer)
                }
            }

        }) {
            Icon(
                painter = painterResource(id = when {
                    !isPlaying.value && !isPaused.value -> {
                        R.drawable.ic_play
                    }
                    isPlaying.value && !isPaused.value && !isFinished.value->{
                        R.drawable.ic_pause_play
                    }
                    !isPaused.value && isFinished.value->{
                        R.drawable.ic_play
                    }
                    isPaused.value ->{
                        R.drawable.ic_play
                    }
                    isFinished.value && !isPlaying.value && !isPaused.value->{
                        R.drawable.ic_play
                    }
                    else -> {
                        R.drawable.ic_play
                    }
                }
                ),
                contentDescription = null
            )
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun AudioListPreview() {
    AudioRecTheme {
        AudioList(
            navController = rememberNavController(),
            readPerm = true,
            permissionState = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        )
    }
}


