package io.github.zainkhalid91.loaders.gallery

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Loaders",
        state = rememberWindowState(width = 760.dp, height = 900.dp),
    ) {
        LoaderGallery()
    }
}
