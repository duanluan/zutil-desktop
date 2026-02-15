package top.zhjh.enums

// 1. 新增：工具分类枚举
enum class ToolCategory(val label: String) {
  COMMON("常用"),
  AI("AI"),
  SETTINGS("设置"),
  ABOUT("关于")
}

// 2. 修改：ToolItem 增加 category 属性
enum class ToolItem(val toolName: String, val category: ToolCategory) {
  // 常用
  TIMESTAMP("时间戳", ToolCategory.COMMON),
  JSON("JSON 工具", ToolCategory.COMMON),
  JSON("JSON", ToolCategory.COMMON),

  // AI
  SPEECH_TO_TEXT("语音转文本", ToolCategory.AI);

  // 如果没有设置相关的 ToolItem，可以暂时不写，设置页可能单独处理
}
