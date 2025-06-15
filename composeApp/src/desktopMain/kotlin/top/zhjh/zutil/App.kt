package top.zhjh.zutil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.zhjh.zutil.common.composable.ZTextField
import top.zhjh.zutil.composable.ToolContent
import top.zhjh.zutil.enums.ToolItem
import top.zhjh.zutil.theme.GrayTheme
import zutil_desktop.composeapp.generated.resources.Res
import zutil_desktop.composeapp.generated.resources.commonly_used

@Composable
@Preview
fun App() {
  GrayTheme {
    Row(modifier = Modifier.fillMaxWidth()) {
      val selectedItem = remember { mutableStateOf(0) }
      val searchText = remember { mutableStateOf("") }
      val openWindows = remember { mutableStateListOf<ToolItem>() }

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

      Column(Modifier.padding(8.dp, 5.dp)) {
        // 搜索框
        ZTextField(
          value = searchText.value,
          onValueChange = { searchText.value = it },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
        )

        // 平铺布局
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 200.dp),
          modifier = Modifier.fillMaxSize().padding(top = 5.dp),
        ) {
          val tools = ToolItem.entries
          items(tools.size) { i ->
            val tool = tools[i]
            // 搜索过滤工具
            if (searchText.value.isNotEmpty() && !tool.toolName.contains(searchText.value)) {
              return@items
            }
            Button(
              onClick = { openWindows.add(tool) },
            ) {
              Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                // 左侧图标
                Icon(
                  painter = painterResource(Res.drawable.commonly_used),
                  contentDescription = null,
                  modifier = Modifier.size(30.dp).padding(end = 8.dp) // 图标与文字间距
                )
                // 右侧文字
                Text(tool.toolName)
              }
            }
          }
        }

        // 动态创建新窗口
        for (tool in openWindows) {
          Window(
            onCloseRequest = { openWindows.remove(tool) },
            title = tool.toolName
          ) {
            ToolContent(tool)
          }
        }
      }
    }
  }
}
