package com.siele.audiorec.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.siele.audiorec.util.Screen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    recordGranted: Boolean,
    readGranted: Boolean,
    writeGranted: Boolean,
    permissionState: MultiplePermissionsState
) {
    NavHost(navController = navController, startDestination = Screen.AudioList.route){
        composable(route = Screen.AudioList.route){
            AudioList(
                navController = navController,
                readPerm =readGranted,
                permissionState = permissionState
            )
        }
        composable(route = Screen.Record.route){
            Record(
                navController = navController,
                recordPerm = recordGranted,
                permissionState = permissionState
            )
        }
    }
}