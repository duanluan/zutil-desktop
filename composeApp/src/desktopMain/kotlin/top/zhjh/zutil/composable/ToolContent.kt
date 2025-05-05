package top.zhjh.zutil.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.zhjh.zutil.enums.ToolItem
import top.zhjh.zutil.enums.ToolItem.NONE
import top.zhjh.zutil.enums.ToolItem.TIMESTAMP

@Composable
fun ToolContent(toolName: ToolItem) {
  when (toolName) {
    NONE -> EmptyTool()
    TIMESTAMP -> TimestampTool()
  }
}

@Composable
fun EmptyTool() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text("请从左侧选择一个工具")
  }
}
