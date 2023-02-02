package com.siele.audiorec.ui.main

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.*
import com.siele.audiorec.ui.theme.AudioRecTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioRecTheme {
                val navController = rememberNavController()
                val recordPermGranted = remember { mutableStateOf(false)}
                val writePermGranted = remember { mutableStateOf(false)}
                val readPermGranted = remember { mutableStateOf(false)}
                val permissionState = rememberMultiplePermissionsState(
                   permissions = listOf(
                       Manifest.permission.RECORD_AUDIO,
                       Manifest.permission.READ_EXTERNAL_STORAGE,
                       Manifest.permission.WRITE_EXTERNAL_STORAGE
                   )
                )

                permissionState.permissions.forEach { perm ->
                when(perm.permission){
                    Manifest.permission.RECORD_AUDIO ->{
                        when{
                            perm.status.isGranted ->{
                                recordPermGranted.value = true
                            }
                            perm.status.shouldShowRationale ->{
                                recordPermGranted.value = false
                            }
                            !perm.status.isGranted && !perm.status.shouldShowRationale ->{
                                recordPermGranted.value = false
                            }
                        }

                    }
                    Manifest.permission.READ_EXTERNAL_STORAGE ->{
                        when{
                            perm.status.isGranted ->{
                                readPermGranted.value = true
                            }
                            perm.status.shouldShowRationale ->{
                                readPermGranted.value = false
                            }
                            !perm.status.isGranted && !perm.status.shouldShowRationale ->{
                                readPermGranted.value = false
                            }
                        }
                    }
                    Manifest.permission.WRITE_EXTERNAL_STORAGE ->{
                        when{
                            perm.status.isGranted ->{
                                writePermGranted.value = true
                            }
                            perm.status.shouldShowRationale ->{
                                writePermGranted.value = false
                            }
                            !perm.status.isGranted && !perm.status.shouldShowRationale ->{
                                writePermGranted.value = false
                            }
                        }
                    }
                }

                }
                NavGraph(
                    navController = navController,
                    recordGranted = recordPermGranted.value,
                    readGranted = readPermGranted.value,
                    writeGranted = writePermGranted.value,
                    permissionState = permissionState
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AudioRecTheme {
    }
}