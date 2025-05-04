package top.zhjh.zutil

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.zhjh.zutil.composable.MyTextField
import zutil_desktop.composeapp.generated.resources.Res
import zutil_desktop.composeapp.generated.resources.commonly_used

@Composable
@Preview
fun App() {
  MaterialTheme {
    Row(modifier = Modifier.fillMaxWidth()) {
      val selectedItem = remember { mutableStateOf(0) }
      val searchText = remember { mutableStateOf("") }

      // 导航栏
      NavigationRail {
        NavigationRailItem(
          icon = {
            Icon(
              painter = painterResource(Res.drawable.commonly_used),
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
          },
          label = { Text("常用") },
          selected = selectedItem.value == 0,
          onClick = { selectedItem.value = 0 }
        )
        NavigationRailItem(
          icon = { Icon(Icons.Filled.Settings, null) },
          label = { Text("设置") },
          selected = selectedItem.value == 1,
          onClick = { selectedItem.value = 1 }
        )
      }

      // 搜索栏和工具列表
      Column(
        modifier = Modifier.padding(8.dp, 5.dp)
      ) {
        MyTextField(
          value = searchText.value,
          onValueChange = { searchText.value = it },
          leadingIcon = {
            Icon(
              Icons.Filled.Search,
              contentDescription = null
            )
          },
          singleLine = true
        )
      }
    }
  }
}
