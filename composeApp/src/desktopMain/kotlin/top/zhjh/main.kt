package top.zhjh

import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "ZUtil 工具箱",
    state = rememberWindowState(position = WindowPosition(Alignment.Center))
  ) {
    App()
    // TestApp()
  }
}
