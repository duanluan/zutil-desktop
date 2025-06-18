package top.zhjh

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZDropdownMenu
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.ZTheme

@Composable
@Preview
fun TestApp() {
  // 添加夜间模式状态控制
  var isDarkTheme by remember { mutableStateOf(false) }
  ZTheme(isDarkTheme = isDarkTheme) {
    // Surface 会使用 MaterialTheme 中定义的背景色
    Surface(modifier = Modifier.fillMaxSize()) {
      // 添加通知宿主
      ToastContainer()

      var text by remember { mutableStateOf("") }

      Column(modifier = Modifier.padding(5.dp)) {
        // 添加夜间模式切换按钮
        ZButton(onClick = { isDarkTheme = !isDarkTheme }) {
          Text(if (isDarkTheme) "切换到日间模式" else "切换到夜间模式")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(onClick = { }) { }
          ZButton(onClick = { ToastManager.success("Default") }) { Text("Default") }
          ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(plain = true, onClick = { ToastManager.success("Plain") }) { Text("Plain") }
          ZButton(type = ZColorType.PRIMARY, plain = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, plain = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, plain = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, plain = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, plain = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "请输入内容",
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }, singleLine = false
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZDropdownMenu(
            options = listOf("选项1", "选项2", "选项3")
          )
        }
      }
    }
  }
}
