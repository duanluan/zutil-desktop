package top.zhjh.composable

import androidx.compose.runtime.Composable
import top.zhjh.enums.ToolItem
import top.zhjh.enums.ToolItem.TIMESTAMP

@Composable
fun ToolContent(toolName: ToolItem) {
  when (toolName) {
    TIMESTAMP -> TimestampTool()
  }
}
