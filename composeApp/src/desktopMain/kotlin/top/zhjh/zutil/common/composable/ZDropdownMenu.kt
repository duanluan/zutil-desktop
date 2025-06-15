package top.zhjh.zutil.common.composable

import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
  var selectedOption by remember { mutableStateOf(defaultSelectedOption ?: "") }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
  ) {
    ZTextField(
      value = selectedOption,
      onValueChange = {},
      readOnly = true,
      modifier = modifier,
      placeholder = if (selectedOption.isEmpty()) placeholder else selectedOption,
      trailingIcon = {
        // 使用自定义图标，而不是默认的 TrailingIcon
        Icon(
          imageVector = Icons.Filled.ArrowDropDown,
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
            selectedOption = option
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
