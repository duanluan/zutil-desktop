package top.zhjh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZCard
import top.zhjh.zui.composable.ZDropdownMenu
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.enums.ZCardShadow
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

      Column(modifier = Modifier.padding(10.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 添加夜间模式切换按钮
        ZButton(onClick = { isDarkTheme = !isDarkTheme }) {
          Text(if (isDarkTheme) "切换到日间模式" else "切换到夜间模式")
        }

        ZCard(shadow = ZCardShadow.NEVER) {
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
            ZButton(round = true, onClick = { ToastManager.success("Round") }) { Text("Round") }
            ZButton(type = ZColorType.PRIMARY, round = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
            ZButton(type = ZColorType.SUCCESS, round = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
            ZButton(type = ZColorType.INFO, round = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
            ZButton(type = ZColorType.WARNING, round = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
            ZButton(type = ZColorType.DANGER, round = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            ZButton(circle = true, icon = { Icon(Icons.Filled.Search, contentDescription = null) }, onClick = { ToastManager.success("Circle") }) { }
            ZButton(type = ZColorType.PRIMARY, circle = true, icon = { Icon(Icons.Filled.Search, contentDescription = null) }, onClick = { ToastManager.success("Primary") }) {}
            ZButton(type = ZColorType.SUCCESS, circle = true, icon = { Icon(Icons.Filled.Edit, contentDescription = null) }, onClick = { ToastManager.success("Success") }) { }
            ZButton(type = ZColorType.INFO, circle = true, icon = { Icon(Icons.Filled.Check, contentDescription = null) }, onClick = { ToastManager.success("Info") }) { }
            ZButton(type = ZColorType.WARNING, circle = true, icon = { Icon(Icons.Filled.Email, contentDescription = null) }, onClick = { ToastManager.success("Warning") }) { }
            ZButton(type = ZColorType.DANGER, circle = true, icon = { Icon(Icons.Filled.Delete, contentDescription = null) }, onClick = { ToastManager.success("Danger") }) { }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            ZButton(enabled = false, onClick = { ToastManager.success("Default") }) { Text("Default") }
            ZButton(type = ZColorType.PRIMARY, enabled = false, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
            ZButton(type = ZColorType.SUCCESS, enabled = false, onClick = { ToastManager.success("Success") }) { Text("Success") }
            ZButton(type = ZColorType.INFO, enabled = false, onClick = { ToastManager.success("Info") }) { Text("Info") }
            ZButton(type = ZColorType.WARNING, enabled = false, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
            ZButton(type = ZColorType.DANGER, enabled = false, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            ZButton(enabled = false, plain = true, onClick = { ToastManager.success("Plain") }) { Text("Plain") }
            ZButton(type = ZColorType.PRIMARY, enabled = false, plain = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
            ZButton(type = ZColorType.SUCCESS, enabled = false, plain = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
            ZButton(type = ZColorType.INFO, enabled = false, plain = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
            ZButton(type = ZColorType.WARNING, enabled = false, plain = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
            ZButton(type = ZColorType.DANGER, enabled = false, plain = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
          }
        }

        ZCard(shadow = ZCardShadow.NEVER) {
          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            var text by remember { mutableStateOf("") }
            ZTextField(
              value = text,
              onValueChange = { text = it },
              placeholder = "Please input",
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            var text by remember { mutableStateOf("") }
            ZTextField(
              value = text,
              enabled = false,
              onValueChange = { text = it },
              placeholder = "Please input",
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            var text by remember { mutableStateOf("") }
            ZTextField(
              value = text,
              onValueChange = { text = it },
              placeholder = "请输入内容",
              leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
              trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }, singleLine = false
            )
          }
        }

        ZCard(shadow = ZCardShadow.NEVER) {
          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            ZDropdownMenu(
              options = listOf("选项1", "选项2", "选项3")
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZCard() {

          }
          ZCard(shadow = ZCardShadow.HOVER) {

          }
          ZCard(shadow = ZCardShadow.NEVER) {

          }
        }
      }
    }
  }
}
