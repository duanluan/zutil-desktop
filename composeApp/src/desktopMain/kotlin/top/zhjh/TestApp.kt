package top.zhjh

import ZLink
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.*
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
      // 滚动状态
      val scrollState = rememberScrollState()

      Box(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier.fillMaxSize().padding(start = 10.dp, end = 20.dp).verticalScroll(scrollState)
          // 每个子元素都插入间距
          , verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Spacer(modifier = Modifier.height(0.dp))

          ZButton(onClick = { isDarkTheme = !isDarkTheme }) {
            Text(if (isDarkTheme) "切换到日间模式" else "切换到夜间模式")
          }

          ZCard(shadow = ZCardShadow.NEVER) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              ZText("H1", style = MaterialTheme.typography.h1, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
              ZText("H2", style = MaterialTheme.typography.h2, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
              ZText("H3", style = MaterialTheme.typography.h3, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
              ZText("H4", style = MaterialTheme.typography.h4, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
              ZText("H5", style = MaterialTheme.typography.h5, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
              ZText("H6", style = MaterialTheme.typography.h6, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
            }
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
              ZTextField(value = text, onValueChange = { text = it }, placeholder = "Please input")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
              var text by remember { mutableStateOf("") }
              ZTextField(value = text, onValueChange = { text = it }, enabled = false, placeholder = "Please input")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
              var text by remember { mutableStateOf("") }
              ZTextField(
                value = text, onValueChange = { text = it }, placeholder = "Please input",
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }, singleLine = false
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
              var text by remember { mutableStateOf("") }
              ZTextField(value = text, onValueChange = { text = it }, type = ZTextFieldType.PASSWORD, placeholder = "Please input password", showPassword = true)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
              var text by remember { mutableStateOf("") }
              ZTextField(
                value = text, onValueChange = { text = it }, placeholder = "Please input", type = ZTextFieldType.TEXTAREA,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
              var text by remember { mutableStateOf("") }
              ZTextField(
                value = text, onValueChange = { text = it }, placeholder = "Please input",
                type = ZTextFieldType.TEXTAREA, resize = false, minLines = 3, maxLines = 3
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

          ZCard(shadow = ZCardShadow.NEVER) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              ZLink("人民网", "http://www.people.com.cn/")
              ZText("_网上的人民日报")
            }
          }

          Spacer(modifier = Modifier.height(0.dp))
        }
        // 垂直滚动条
        VerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState), modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
        // 通知
        ToastContainer()
      }
    }
  }
}
