package top.zhjh.zui.composable

import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ZDropdownMenu(
  options: List<String>,
  // 行高，默认 20.dp
  lineHeight: Int = 20,
  // 字体大小，默认 14.sp
  fontSize: TextUnit = 12.sp,
  modifier: Modifier = Modifier,
  size: ZFormSize? = null,
  // 受控值。非 null 时，组件显示由外部状态完全控制。
  value: String? = null,
  // 非受控模式下的初始值（兼容旧调用）。
  defaultSelectedOption: String? = null,
  onOptionSelected: (String) -> Unit = {},
  placeholder: String = "请选择"
) {
  // 共享的文字样式，确保一致性
  val textStyle = LocalTextStyle.current.copy(
    fontSize = fontSize,
    lineHeight = lineHeight.sp
  )

  var expanded by remember { mutableStateOf(false) }

  // 非受控模式内部状态；受控模式下不会使用该状态作为最终显示值。
  var internalSelectedOption by remember { mutableStateOf(defaultSelectedOption ?: "") }

  // 当外部默认值变化且当前是非受控模式时，同步内部状态，避免与 ViewModel 脱节。
  LaunchedEffect(defaultSelectedOption, value) {
    if (value == null) {
      internalSelectedOption = defaultSelectedOption ?: ""
    }
  }

  val selectedOption = value ?: internalSelectedOption
  val resolvedSize = size ?: LocalZFormSize.current

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
  ) {
    ZTextField(
      size = resolvedSize,
      value = selectedOption,
      onValueChange = {},
      readOnly = true,
      modifier = modifier,
      placeholder = if (selectedOption.isEmpty()) placeholder else selectedOption,
      trailingIcon = {
        // 使用自定义图标，而不是默认的 TrailingIcon
        Icon(
          imageVector = FeatherIcons.ChevronDown,
          contentDescription = "下拉菜单",
          // 根据展开状态旋转图标
          modifier = Modifier.graphicsLayer(
            rotationZ = if (expanded) 180f else 0f
          )
        )
      }
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      // 为每个选项创建一个菜单项
      options.forEach { option ->
        DropdownMenuItem(
          onClick = {
            if (value == null) {
              internalSelectedOption = option
            }
            expanded = false
            onOptionSelected(option)
          },
          modifier = Modifier.height(lineHeight.dp)
        ) {
          Text(text = option, style = textStyle)
        }
      }
    }
  }
}
