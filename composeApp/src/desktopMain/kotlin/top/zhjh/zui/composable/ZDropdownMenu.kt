package top.zhjh.zui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.XCircle

enum class ZDropdownMenuSize {
  Large,
  Default,
  Small
}

object ZDropdownMenuDefaults {
  fun fromFormSize(size: ZFormSize?): ZDropdownMenuSize {
    return when (size) {
      ZFormSize.LARGE -> ZDropdownMenuSize.Large
      ZFormSize.SMALL -> ZDropdownMenuSize.Small
      else -> ZDropdownMenuSize.Default
    }
  }

  fun toFormSize(size: ZDropdownMenuSize): ZFormSize {
    return when (size) {
      ZDropdownMenuSize.Large -> ZFormSize.LARGE
      ZDropdownMenuSize.Small -> ZFormSize.SMALL
      ZDropdownMenuSize.Default -> ZFormSize.DEFAULT
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ZDropdownMenu(
  options: List<String>,
  // 行高，默认 20.dp
  lineHeight: Int = 20,
  // 字体大小，默认 14.sp
  fontSize: TextUnit = 12.sp,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  size: ZDropdownMenuSize? = null,
  // 受控值。非 null 时，组件显示由外部状态完全控制。
  value: String? = null,
  // 非受控模式下的初始值（兼容旧调用）。
  defaultSelectedOption: String? = null,
  onOptionSelected: (String) -> Unit = {},
  placeholder: String = "请选择",
  disabledOptions: Set<String> = emptySet(),
  clearable: Boolean = false
) {
  // 共享的文字样式，确保一致性
  val textStyle = LocalTextStyle.current.copy(
    fontSize = fontSize,
    lineHeight = lineHeight.sp
  )

  var expanded by remember { mutableStateOf(false) }
  var suppressNextToggle by remember { mutableStateOf(false) }
  val hoverInteractionSource = remember { MutableInteractionSource() }
  val isHovered by hoverInteractionSource.collectIsHoveredAsState()
  LaunchedEffect(enabled) {
    if (!enabled) {
      expanded = false
    }
  }

  // 非受控模式内部状态；受控模式下不会使用该状态作为最终显示值。
  var internalSelectedOption by remember { mutableStateOf(defaultSelectedOption ?: "") }

  // 当外部默认值变化且当前是非受控模式时，同步内部状态，避免与 ViewModel 脱节。
  LaunchedEffect(defaultSelectedOption, value) {
    if (value == null) {
      internalSelectedOption = defaultSelectedOption ?: ""
    }
  }

  val selectedOption = value ?: internalSelectedOption
  val resolvedSize = size ?: ZDropdownMenuDefaults.fromFormSize(LocalZFormSize.current)
  val resolvedFormSize = ZDropdownMenuDefaults.toFormSize(resolvedSize)
  val optionHeight = ZFormDefaults.resolveControlHeight(resolvedFormSize, ZTextFieldDefaults.MinHeight)
  val showClearIcon = clearable && enabled && selectedOption.isNotEmpty() && isHovered && !expanded

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = {
      if (suppressNextToggle) {
        suppressNextToggle = false
        return@ExposedDropdownMenuBox
      }
      if (enabled) {
        expanded = !expanded
      }
    }
  ) {
    ZTextField(
      size = resolvedFormSize,
      value = selectedOption,
      onValueChange = {},
      enabled = enabled,
      readOnly = true,
      modifier = modifier.hoverable(hoverInteractionSource),
      placeholder = if (selectedOption.isEmpty()) placeholder else selectedOption,
      trailingIcon = {
        if (showClearIcon) {
          Icon(
            imageVector = FeatherIcons.XCircle,
            contentDescription = "Clear selection",
            modifier = Modifier.clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              suppressNextToggle = true
              expanded = false
              if (value == null) {
                internalSelectedOption = ""
              }
              onOptionSelected("")
            }
          )
        } else {
          Icon(
            imageVector = FeatherIcons.ChevronDown,
            contentDescription = "Toggle options",
            modifier = Modifier.graphicsLayer(
              rotationZ = if (expanded) 180f else 0f
            )
          )
        }
      }
    )

    ExposedDropdownMenu(
      expanded = expanded && enabled,
      onDismissRequest = { expanded = false }
    ) {
      // 为每个选项创建一个菜单项
      options.forEach { option ->
        val isDisabled = option in disabledOptions
        DropdownMenuItem(
          onClick = {
            if (value == null) {
              internalSelectedOption = option
            }
            expanded = false
            onOptionSelected(option)
          },
          enabled = enabled && !isDisabled,
          modifier = Modifier.height(optionHeight)
        ) {
          Text(text = option, style = textStyle)
        }
      }
    }
  }
}
