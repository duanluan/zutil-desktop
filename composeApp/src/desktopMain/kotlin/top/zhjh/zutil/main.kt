package top.zhjh.zutil

import TestApp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "zutil-desktop",
  ) {
    App()
    // TestApp()
  }
}
