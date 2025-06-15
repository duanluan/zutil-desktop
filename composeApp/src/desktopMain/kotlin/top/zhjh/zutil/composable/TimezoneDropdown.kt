package top.zhjh.zutil.composable

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.zutil.common.composable.ZDropdownMenu
import java.util.*

@Composable
fun TimezoneDropdown(
  // 当前选中的时区
  selectedTimezone: String,
  // 时区变化时的回调函数
  onTimezoneSelected: (String) -> Unit
) {
  // 获取所有可用的时区ID
  val timezones = remember { TimeZone.getAvailableIDs().sortedArray().toList() }

  // 使用ZDropdownMenu替代原来的实现
  ZDropdownMenu(
    options = timezones,
    defaultSelectedOption = selectedTimezone,
    onOptionSelected = onTimezoneSelected,
    placeholder = "请选择时区",
    modifier = Modifier.width(220.dp) // 保持原来的宽度
  )
}
