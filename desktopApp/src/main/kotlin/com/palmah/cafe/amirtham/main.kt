package com.palmah.cafe.amirtham

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PlateCast",
    ) {
        App()
    }
}