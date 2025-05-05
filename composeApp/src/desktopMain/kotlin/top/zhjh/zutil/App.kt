package top.zhjh.zutil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.zhjh.zutil.common.composable.MyTextField
import top.zhjh.zutil.composable.ToolContent
import top.zhjh.zutil.enums.ToolItem
import zutil_desktop.composeapp.generated.resources.Res
import zutil_desktop.composeapp.generated.resources.commonly_used

@Composable
@Preview
fun App() {
  MaterialTheme {
    Row(modifier = Modifier.fillMaxWidth()) {
      val selectedItem = remember { mutableStateOf(0) }
      val searchText = remember { mutableStateOf("") }
      val selectedTool = remember { mutableStateOf(ToolItem.NONE) }

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
        modifier = Modifier.padding(8.dp, 5.dp).width(200.dp)
      ) {
        MyTextField(
          modifier = Modifier.height(30.dp),
          value = searchText.value,
          onValueChange = { searchText.value = it },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          singleLine = true
        )

        ScrollBoxes(
          searchText = searchText.value,
          onToolSelected = { toolName ->
            selectedTool.value = ToolItem.fromToolName(toolName)
          }
        )
      }

      // 右侧工具内容区
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        ToolContent(selectedTool.value)
      }
    }
  }
}

@Composable
private fun ScrollBoxes(searchText: String = "", onToolSelected: (String) -> Unit) {
  val allItems = remember {
    ToolItem.entries
      .filter { it != ToolItem.NONE }
      .map { it.toolName }
  }

  // 根据搜索文本筛选列表
  val filteredItems = remember(searchText) {
    if (searchText.isBlank()) {
      allItems
    } else {
      allItems.filter { it.contains(searchText, ignoreCase = true) }
    }
  }

  // 记录当前选中的项目
  val selectedItem = remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 5.dp, bottom = 5.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // 3. 显示筛选后的列表项
    filteredItems.forEach { item ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            // 选中项浅蓝色背景
            if (selectedItem.value == item) Color(0xFFD7E8FF) else Color.White
          )
          // 内边距要放在选中背景后面，否则选中背景不会包含内边距
          .padding(vertical = 5.dp, horizontal = 5.dp)
          .clickable {
            // 处理点击事件
            selectedItem.value = item
            onToolSelected(item)
          }
      ) {
        Text(text = item)
      }
      Divider(color = Color.Gray, thickness = 1.dp)
    }
  }
}
