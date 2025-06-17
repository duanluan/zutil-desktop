package top.zhjh

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.zhjh.zui.composable.ZDropdownMenu
import top.zhjh.zui.composable.ZTextField
import top.zhjh.theme.GrayTheme

@Composable
@Preview
fun TestApp() {
  GrayTheme {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(5.dp)) {
      ZTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = "请输入内容",
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }, singleLine = false
      )

      ZDropdownMenu(
        options = listOf("选项1", "选项2", "选项3")
      )
    }
  }
}
