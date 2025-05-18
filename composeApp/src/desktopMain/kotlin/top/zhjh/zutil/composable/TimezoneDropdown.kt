package top.zhjh.zutil.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import top.zhjh.zutil.common.composable.MyTextField
import java.util.*

@Composable
fun TimezoneDropdown(
  // 当前选中的时区
  selectedTimezone: String,
  // 时区变化时的回调函数
  onTimezoneSelected: (String) -> Unit
) {
  // 获取所有可用的时区ID
  val timezones = remember { TimeZone.getAvailableIDs().sortedArray() }

  // 控制下拉菜单的展开状态
  var expanded by remember { mutableStateOf(false) }

  // 用于设置下拉菜单宽度
  var textFieldSize by remember { mutableStateOf(Size.Zero) }
  val localDensity = LocalDensity.current

  Column {
    // 输入框作为下拉菜单的触发器
    MyTextField(
      value = selectedTimezone,
      // 使用外部传入的回调
      onValueChange = { onTimezoneSelected(it) },
      singleLine = true,
      textStyle = TextStyle(fontSize = 15.sp),
      modifier = Modifier.width(220.dp)
        .clickable { expanded = true }
        .onGloballyPositioned { coordinates ->
          // 获取文本框尺寸
          textFieldSize = coordinates.size.toSize()
        },
      // 解决 clickable 无效：https://stackoverflow.com/a/69198950/11403802
      enabled = false,
      readOnly = true,
      trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "下拉箭头") }
    )

    // 下拉菜单
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.width(with(localDensity) { textFieldSize.width.toDp() })
    ) {
      timezones.forEach { timezone ->
        DropdownMenuItem(
          modifier = Modifier.height(38.dp),
          content = { Text(text = timezone, style = TextStyle(fontSize = 15.sp)) },
          onClick = {
            // 使用外部传入的回调
            onTimezoneSelected(timezone)
            expanded = false
          }
        )
      }
    }
  }
}
