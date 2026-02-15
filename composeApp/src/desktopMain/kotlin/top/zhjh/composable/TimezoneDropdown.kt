package top.zhjh.composable

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZDropdownMenu
import java.util.TimeZone

/**
 * 时区下拉选择组件。
 *
 * 职责边界：
 * - 仅负责“展示可选时区 + 透传选中事件”。
 * - 不包含业务转换逻辑，转换由调用方（通常是 ViewModel）处理。
 *
 * @param selectedTimezone 当前已选时区 ID，例如 `Asia/Shanghai`。
 * @param onTimezoneSelected 选中回调，返回用户新选中的时区 ID。
 */
@Composable
fun TimezoneDropdown(
  selectedTimezone: String,
  onTimezoneSelected: (String) -> Unit
) {
  /**
   * 读取并缓存系统可用时区列表。
   *
   * 为什么使用 remember：
   * - TimeZone.getAvailableIDs() 结果在运行时基本稳定；
   * - 若每次重组都重新构建列表，会增加不必要的分配与排序开销。
   */
  val timezones = remember {
    TimeZone.getAvailableIDs()
      .sortedArray()
      .toList()
  }

  /**
   * 使用统一的 ZUI 下拉组件，保持项目交互与视觉一致。
   *
   * 宽度固定为 220.dp 的考虑：
   * - 大多数常见时区字符串可完整展示（如 America/Los_Angeles）；
   * - 不会因内容长度变化引发布局抖动。
   */
  ZDropdownMenu(
    options = timezones,
    defaultSelectedOption = selectedTimezone,
    onOptionSelected = onTimezoneSelected,
    placeholder = "请选择时区",
    modifier = Modifier.width(220.dp)
  )
}
