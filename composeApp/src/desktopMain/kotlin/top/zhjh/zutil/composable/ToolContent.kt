package top.zhjh.zutil.composable

import androidx.compose.runtime.Composable
import top.zhjh.zutil.enums.ToolItem
import top.zhjh.zutil.enums.ToolItem.TIMESTAMP

@Composable
fun ToolContent(toolName: ToolItem) {
  when (toolName) {
    TIMESTAMP -> TimestampTool()
  }
}
