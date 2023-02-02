package com.siele.audiorec.util

sealed class Screen(val route:String) {
    object AudioList:Screen(route = "audio_list")
    object Record:Screen(route = "record")
}