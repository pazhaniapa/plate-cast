package com.palmah.cafe.amirtham

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.palmah.cafe.amirtham.di.initKoin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Firebase.initialize(
        options = FirebaseOptions(
            applicationId = "1:404656520995:web:81b66061b87ca45a2c9696",
            apiKey = "AIzaSyCnUdk29KUmKpt9FMg0xTKmivpX0FWhgXg",
            projectId = "amirtham-999dc",
            authDomain = "amirtham-999dc.firebaseapp.com",
            storageBucket = "amirtham-999dc.firebasestorage.app",
            gcmSenderId = "404656520995",
        )
    )
    initKoin()
    ComposeViewport {
        App()
    }
}