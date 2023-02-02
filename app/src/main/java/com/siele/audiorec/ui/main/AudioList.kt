package com.siele.audiorec.ui.main

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.*
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
                    id = com.siele.audiorec.R.drawable.ic_mic),
                    contentDescription = null)
            }
        }
    ) {
        if (readPerm) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                state = scrollState,
            ) {
                items(count = 5) {
                    AudioItem(modifier)
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
fun AudioItem(modifier: Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text ="audio1223344.mp3")
        IconButton(onClick = {

        }) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
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


