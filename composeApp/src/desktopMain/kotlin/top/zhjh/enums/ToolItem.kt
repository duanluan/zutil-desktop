package top.zhjh.enums

enum class ToolCategory(val label: String) {
  COMMON("常用"),
  AI("AI"),
  SETTINGS("设置"),
  ZUI("ZUI"),
  ABOUT("关于")
}

enum class ToolItem(val toolName: String, val category: ToolCategory) {
  // 常用
  TIMESTAMP("时间戳", ToolCategory.COMMON),
  JSON("JSON", ToolCategory.COMMON),
  UUID("UUID 生成", ToolCategory.COMMON),

  // AI
  SPEECH_TO_TEXT("语音转文本", ToolCategory.AI)
}
