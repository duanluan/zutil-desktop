package top.zhjh.composable

import androidx.compose.runtime.Composable
import top.zhjh.enums.ToolItem

@Composable
fun ToolContent(toolName: ToolItem) {
  when (toolName) {
    ToolItem.TIMESTAMP -> TimestampTool()
    ToolItem.JSON -> JsonTool()
    ToolItem.SPEECH_TO_TEXT -> SpeechToTextTool()
  }
}
