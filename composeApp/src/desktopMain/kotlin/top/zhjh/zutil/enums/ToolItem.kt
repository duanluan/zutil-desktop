package top.zhjh.zutil.enums

enum class ToolItem(val toolName: String) {
  NONE(""),
  TIMESTAMP("时间戳转换"),
  ;

  companion object {
    fun fromToolName(toolName: String): ToolItem {
      return entries.find { it.toolName == toolName } ?: NONE
    }
  }
}
