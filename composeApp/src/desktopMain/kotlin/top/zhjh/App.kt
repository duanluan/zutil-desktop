package top.zhjh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.compose.resources.painterResource
import top.zhjh.common.theme.mapleMonoFontFamily
import top.zhjh.common.theme.mapleMonoTypography
import top.zhjh.composable.ToolContent
import top.zhjh.enums.ToolCategory
import top.zhjh.enums.ToolItem
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.ZTheme
import zutil_desktop.composeapp.generated.resources.Res
import zutil_desktop.composeapp.generated.resources.commonly_used

@Composable
@Preview
fun App() {
  ZTheme(
    typography = mapleMonoTypography(),
    defaultFontFamily = mapleMonoFontFamily()
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      // 使用枚举来管理当前选中的分类，默认选中常用
      var selectedCategory by remember { mutableStateOf(ToolCategory.COMMON) }

      val searchText = remember { mutableStateOf("") }
      val openWindows = remember { mutableStateListOf<ToolItem>() }

      // --- 左侧导航栏 ---
      NavigationRail(modifier = Modifier.width(72.dp)) { // 给个固定宽度美观些

        // 1. 常用
        NavigationRailItem(
          icon = {
            Icon(
              // 暂时复用之前的资源，建议后续换成矢量图 Icons.Filled.Home
              painter = painterResource(Res.drawable.commonly_used),
              contentDescription = null,
              modifier = Modifier.size(24.dp)
            )
          },
          label = { Text(ToolCategory.COMMON.label) },
          selected = selectedCategory == ToolCategory.COMMON,
          onClick = { selectedCategory = ToolCategory.COMMON }
        )

        // 2. AI
        NavigationRailItem(
          icon = { Icon(Icons.Filled.SmartToy, null) }, // 如果报错找不到图标，暂时用 Icons.Filled.Star
          label = { Text(ToolCategory.AI.label) },
          selected = selectedCategory == ToolCategory.AI,
          onClick = { selectedCategory = ToolCategory.AI }
        )

        Spacer(Modifier.weight(1f)) // 把设置顶到底部

        // 3. 设置 (通常设置在最底部)
        NavigationRailItem(
          icon = { Icon(Icons.Filled.Settings, null) },
          label = { Text(ToolCategory.SETTINGS.label) },
          selected = selectedCategory == ToolCategory.SETTINGS,
          onClick = { selectedCategory = ToolCategory.SETTINGS }
        )
      }

      // --- 右侧内容区域 ---
      Column(Modifier.fillMaxSize().padding(10.dp)) {

        // 只有在非设置页面显示搜索框
        if (selectedCategory != ToolCategory.SETTINGS) {
          ZTextField(
            value = searchText.value,
            onValueChange = { searchText.value = it },
            placeholder = "搜索${selectedCategory.label}工具...",
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        // 内容切换逻辑
        Box(modifier = Modifier.weight(1f)) {
          if (selectedCategory == ToolCategory.SETTINGS) {
            // 设置页面占位
            SettingsPage()
          } else {
            // 工具列表页面
            ToolListGrid(
              category = selectedCategory,
              filterText = searchText.value,
              onToolClick = { tool ->
                // 避免重复打开
                if (!openWindows.contains(tool)) {
                  openWindows.add(tool)
                }
              }
            )
          }
        }

        // --- 窗口管理逻辑 (保持不变，增加新工具的窗口大小配置) ---
        for (tool in openWindows) {
          val windowState = when (tool) {
            ToolItem.TIMESTAMP -> rememberWindowState(width = 800.dp, height = 720.dp, position = WindowPosition(Alignment.Center))
            ToolItem.SPEECH_TO_TEXT -> rememberWindowState(width = 900.dp, height = 800.dp, position = WindowPosition(Alignment.Center)) // AI 工具通常需要大一点
          }

          Window(
            onCloseRequest = { openWindows.remove(tool) },
            title = tool.toolName,
            state = windowState
          ) {
            ToolContent(tool)
          }
        }
      }
    }
  }
}

// 抽离：设置页面组件
@Composable
fun SettingsPage() {
  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text("设置页面开发中...", style = MaterialTheme.typography.h5)
  }
}

// 抽离：工具网格组件
@Composable
fun ToolListGrid(
  category: ToolCategory,
  filterText: String,
  onToolClick: (ToolItem) -> Unit
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 200.dp),
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 核心过滤逻辑：先按分类筛选，再按搜索词筛选
    val tools = ToolItem.entries.filter {
      it.category == category &&
        (filterText.isEmpty() || it.toolName.contains(filterText, ignoreCase = true))
    }

    items(tools.size) { i ->
      val tool = tools[i]
      ZButton(
        type = ZColorType.PRIMARY,
        contentPadding = PaddingValues(15.dp),
        contentAlignment = Alignment.CenterStart,
        onClick = { onToolClick(tool) }
      ) {
        // 根据工具类型动态显示图标
        val iconModifier = Modifier.size(24.dp).padding(end = 8.dp)
        if (tool.category == ToolCategory.AI) {
          Icon(Icons.Filled.SmartToy, null, modifier = iconModifier)
        } else {
          Icon(painterResource(Res.drawable.commonly_used), null, modifier = iconModifier)
        }

        Text(tool.toolName)
      }
    }
  }
}
