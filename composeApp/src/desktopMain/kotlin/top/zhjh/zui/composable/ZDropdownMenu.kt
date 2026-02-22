package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.X
import compose.icons.feathericons.XCircle
import kotlinx.coroutines.delay
import top.zhjh.zui.theme.isAppInDarkTheme

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

private data class ZDropdownMenuFieldStyle(
  val backgroundColor: Color,
  val borderColor: Color,
  val textColor: Color,
  val placeholderColor: Color,
  val iconColor: Color,
  val tagBackgroundColor: Color,
  val tagTextColor: Color,
  val tagRemoveColor: Color
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
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
  clearable: Boolean = false,
  multiple: Boolean = false,
  values: List<String>? = null,
  defaultSelectedOptions: List<String> = emptyList(),
  onOptionsSelected: (List<String>) -> Unit = {},
  collapseTags: Boolean = false,
  collapseTagsTooltip: Boolean = false,
  maxCollapseTags: Int? = null
) {
  val textStyle = LocalTextStyle.current.copy(
    fontSize = fontSize,
    lineHeight = lineHeight.sp
  )

  var expanded by remember { mutableStateOf(false) }
  // Prevent ExposedDropdownMenuBox from toggling when clicking inner clear/remove controls.
  var suppressNextToggle by remember { mutableStateOf(false) }
  val hoverInteractionSource = remember { MutableInteractionSource() }
  val isHovered by hoverInteractionSource.collectIsHoveredAsState()
  LaunchedEffect(enabled) {
    if (!enabled) {
      expanded = false
    }
  }

  var internalSelectedOption by remember {
    mutableStateOf(defaultSelectedOption?.takeIf { it in options } ?: "")
  }
  var internalSelectedOptions by remember {
    mutableStateOf(normalizeDropdownMenuValues(defaultSelectedOptions, options))
  }

  LaunchedEffect(defaultSelectedOption, value, options) {
    if (value == null) {
      internalSelectedOption = defaultSelectedOption?.takeIf { it in options } ?: ""
    }
  }
  LaunchedEffect(defaultSelectedOptions, values, options) {
    if (values == null) {
      internalSelectedOptions = normalizeDropdownMenuValues(defaultSelectedOptions, options)
    }
  }

  val selectedOption = (value ?: internalSelectedOption).takeIf { it in options } ?: ""
  val selectedOptions = normalizeDropdownMenuValues(values ?: internalSelectedOptions, options)
  val resolvedSize = size ?: ZDropdownMenuDefaults.fromFormSize(LocalZFormSize.current)
  val resolvedFormSize = ZDropdownMenuDefaults.toFormSize(resolvedSize)
  val optionHeight = ZFormDefaults.resolveControlHeight(resolvedFormSize, ZTextFieldDefaults.MinHeight)
  val hasSelection = if (multiple) selectedOptions.isNotEmpty() else selectedOption.isNotEmpty()
  val showClearIcon = clearable && enabled && hasSelection && isHovered && !expanded
  val visibleTagLimit = resolveVisibleTagLimit(collapseTags = collapseTags, maxCollapseTags = maxCollapseTags)
  val visibleTags = if (multiple) selectedOptions.take(visibleTagLimit) else emptyList()
  val hiddenTags = if (multiple && selectedOptions.size > visibleTags.size) {
    selectedOptions.drop(visibleTags.size)
  } else {
    emptyList()
  }
  val supportsCollapsedTagsTooltip = (collapseTagsTooltip || maxCollapseTags != null) && enabled
  val collapsedTagHoverInteractionSource = remember { MutableInteractionSource() }
  val isCollapsedTagHovered by collapsedTagHoverInteractionSource.collectIsHoveredAsState()
  val collapsedTooltipHoverInteractionSource = remember { MutableInteractionSource() }
  val isCollapsedTooltipHovered by collapsedTooltipHoverInteractionSource.collectIsHoveredAsState()
  var collapsedTagsTooltipVisible by remember { mutableStateOf(false) }
  val canShowCollapsedTagsTooltip = supportsCollapsedTagsTooltip && hiddenTags.isNotEmpty() && !expanded
  LaunchedEffect(canShowCollapsedTagsTooltip, isCollapsedTagHovered, isCollapsedTooltipHovered) {
    if (!canShowCollapsedTagsTooltip) {
      collapsedTagsTooltipVisible = false
      return@LaunchedEffect
    }
    if (isCollapsedTagHovered || isCollapsedTooltipHovered) {
      collapsedTagsTooltipVisible = true
    } else {
      // 给鼠标从 +N 移动到悬浮层留出过渡时间，避免边界抖动闪烁。
      delay(120)
      if (!isCollapsedTagHovered && !isCollapsedTooltipHovered) {
        collapsedTagsTooltipVisible = false
      }
    }
  }
  val isDarkTheme = isAppInDarkTheme()
  val popupGapPx = with(LocalDensity.current) { 6.dp.roundToPx() }
  val collapsedTagPopupPositionProvider = remember(popupGapPx) {
    object : PopupPositionProvider {
      override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
      ): IntOffset {
        val x = anchorBounds.left.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val belowY = anchorBounds.bottom + popupGapPx
        val aboveY = anchorBounds.top - popupGapPx - popupContentSize.height
        val y = when {
          belowY + popupContentSize.height <= windowSize.height -> belowY
          aboveY >= 0 -> aboveY
          else -> belowY.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        }
        return IntOffset(x, y)
      }
    }
  }
  val fieldStyle = getZDropdownMenuFieldStyle(
    isDarkTheme = isDarkTheme,
    isHovered = isHovered,
    isFocused = expanded,
    enabled = enabled
  )

  val updateSingleSelection: (String) -> Unit = { option ->
    val normalized = option.takeIf { it in options } ?: ""
    if (value == null) {
      internalSelectedOption = normalized
    }
    onOptionSelected(normalized)
  }
  val updateMultiSelection: (List<String>) -> Unit = { selections ->
    val normalized = normalizeDropdownMenuValues(selections, options)
    if (values == null) {
      internalSelectedOptions = normalized
    }
    onOptionsSelected(normalized)
  }

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
    if (!multiple) {
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
                updateSingleSelection("")
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
    } else {
      Box(
        modifier = modifier
          .hoverable(hoverInteractionSource)
          .background(fieldStyle.backgroundColor, ZTextFieldDefaults.Shape)
          .border(1.dp, fieldStyle.borderColor, ZTextFieldDefaults.Shape)
          .defaultMinSize(minHeight = optionHeight)
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.weight(1f)) {
            if (selectedOptions.isEmpty()) {
              Text(
                text = placeholder,
                style = textStyle,
                color = fieldStyle.placeholderColor,
                modifier = Modifier.padding(vertical = 4.dp)
              )
            } else {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                visibleTags.forEach { selected ->
                  ZDropdownSelectionTag(
                    text = selected,
                    textStyle = textStyle,
                    fieldStyle = fieldStyle,
                    enabled = enabled,
                    removable = enabled,
                    onRemove = {
                      suppressNextToggle = true
                      updateMultiSelection(selectedOptions - selected)
                    }
                  )
                }
                if (hiddenTags.isNotEmpty()) {
                  Box(
                    modifier = if (supportsCollapsedTagsTooltip) {
                      Modifier.hoverable(collapsedTagHoverInteractionSource)
                    } else {
                      Modifier
                    }
                  ) {
                    ZDropdownSelectionTag(
                      text = "+ ${hiddenTags.size}",
                      textStyle = textStyle,
                      fieldStyle = fieldStyle,
                      enabled = enabled,
                      removable = false
                    )
                    if (collapsedTagsTooltipVisible) {
                      Popup(
                        popupPositionProvider = collapsedTagPopupPositionProvider,
                        properties = PopupProperties(
                          focusable = false,
                          dismissOnBackPress = false,
                          dismissOnClickOutside = false
                        )
                      ) {
                        Surface(
                          shape = RoundedCornerShape(4.dp),
                          elevation = 8.dp,
                          modifier = Modifier.hoverable(collapsedTooltipHoverInteractionSource)
                        ) {
                          Box(
                            modifier = Modifier
                              .padding(horizontal = 6.dp, vertical = 6.dp)
                              .widthIn(max = 280.dp)
                          ) {
                            FlowRow(
                              horizontalArrangement = Arrangement.spacedBy(6.dp),
                              verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                              hiddenTags.forEach { hidden ->
                                ZDropdownSelectionTag(
                                  text = hidden,
                                  textStyle = textStyle,
                                  fieldStyle = fieldStyle,
                                  enabled = enabled,
                                  removable = enabled,
                                  onRemove = {
                                    suppressNextToggle = true
                                    updateMultiSelection(selectedOptions - hidden)
                                  }
                                )
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          if (showClearIcon) {
            Icon(
              imageVector = FeatherIcons.XCircle,
              contentDescription = "Clear selections",
              tint = fieldStyle.iconColor,
              modifier = Modifier
                .size(ZTextFieldDefaults.IconSize)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  suppressNextToggle = true
                  expanded = false
                  updateMultiSelection(emptyList())
                }
            )
          } else {
            Icon(
              imageVector = FeatherIcons.ChevronDown,
              contentDescription = "Toggle options",
              tint = fieldStyle.iconColor,
              modifier = Modifier
                .size(ZTextFieldDefaults.IconSize)
                .graphicsLayer(rotationZ = if (expanded) 180f else 0f)
            )
          }
        }
      }
    }

    ExposedDropdownMenu(
      expanded = expanded && enabled,
      onDismissRequest = { expanded = false }
    ) {
      options.forEach { option ->
        val isDisabled = option in disabledOptions
        val isSelected = if (multiple) option in selectedOptions else option == selectedOption
        DropdownMenuItem(
          onClick = {
            if (multiple) {
              if (isSelected) {
                updateMultiSelection(selectedOptions - option)
              } else {
                updateMultiSelection(selectedOptions + option)
              }
            } else {
              updateSingleSelection(option)
              expanded = false
            }
          },
          enabled = enabled && !isDisabled,
          modifier = Modifier.height(optionHeight)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = option,
              style = textStyle,
              color = if (isSelected && enabled) Color(0xff409eff) else LocalContentColor.current
            )
            if (multiple && isSelected) {
              Icon(
                imageVector = FeatherIcons.Check,
                contentDescription = "Selected",
                tint = Color(0xff409eff),
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ZDropdownSelectionTag(
  text: String,
  textStyle: androidx.compose.ui.text.TextStyle,
  fieldStyle: ZDropdownMenuFieldStyle,
  enabled: Boolean,
  removable: Boolean,
  verticalPadding: Dp = 4.dp,
  onRemove: (() -> Unit)? = null,
  onClick: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(4.dp)
  Row(
    modifier = Modifier
      .background(fieldStyle.tagBackgroundColor, shape)
      .then(
        if (onClick != null) {
          Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
          )
        } else {
          Modifier
        }
      )
      .padding(horizontal = 8.dp, vertical = verticalPadding),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = text,
      style = textStyle,
      color = fieldStyle.tagTextColor
    )
    if (removable && onRemove != null && enabled) {
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = FeatherIcons.X,
        contentDescription = "Remove $text",
        tint = fieldStyle.tagRemoveColor,
        modifier = Modifier
          .size(12.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onRemove
          )
      )
    }
  }
}

private fun resolveVisibleTagLimit(
  collapseTags: Boolean,
  maxCollapseTags: Int?
): Int {
  if (maxCollapseTags != null) {
    return maxCollapseTags.coerceAtLeast(1)
  }
  if (collapseTags) {
    return 1
  }
  return Int.MAX_VALUE
}

private fun normalizeDropdownMenuValues(
  selections: List<String>,
  options: List<String>
): List<String> {
  if (selections.isEmpty() || options.isEmpty()) return emptyList()
  val selectedSet = selections.toSet()
  return options.filter { it in selectedSet }
}

private fun getZDropdownMenuFieldStyle(
  isDarkTheme: Boolean,
  isHovered: Boolean,
  isFocused: Boolean,
  enabled: Boolean
): ZDropdownMenuFieldStyle {
  if (!enabled) {
    return if (isDarkTheme) {
      ZDropdownMenuFieldStyle(
        backgroundColor = Color(0xff262727),
        borderColor = Color(0xff414243),
        textColor = Color(0xff8d9095),
        placeholderColor = Color(0xff8d9095),
        iconColor = Color(0xff8d9095),
        tagBackgroundColor = Color(0xff3b3d3f),
        tagTextColor = Color(0xff8d9095),
        tagRemoveColor = Color(0xff8d9095)
      )
    } else {
      ZDropdownMenuFieldStyle(
        backgroundColor = Color(0xfff5f7fa),
        borderColor = Color(0xffe4e7ed),
        textColor = Color(0xffa8abb2),
        placeholderColor = Color(0xffa8abb2),
        iconColor = Color(0xffa8abb2),
        tagBackgroundColor = Color(0xffebeef5),
        tagTextColor = Color(0xffa8abb2),
        tagRemoveColor = Color(0xffa8abb2)
      )
    }
  }

  if (isFocused) {
    return if (isDarkTheme) {
      ZDropdownMenuFieldStyle(
        backgroundColor = Color.Transparent,
        borderColor = Color(0xff409eff),
        textColor = Color(0xffcfd3dc),
        placeholderColor = Color(0xff8d9095),
        iconColor = Color(0xffa3a6ad),
        tagBackgroundColor = Color(0xff2f3133),
        tagTextColor = Color(0xffcfd3dc),
        tagRemoveColor = Color(0xffa3a6ad)
      )
    } else {
      ZDropdownMenuFieldStyle(
        backgroundColor = Color.Transparent,
        borderColor = Color(0xff409eff),
        textColor = Color(0xff606266),
        placeholderColor = Color(0xffa8abb2),
        iconColor = Color(0xff909399),
        tagBackgroundColor = Color(0xfff0f2f5),
        tagTextColor = Color(0xff909399),
        tagRemoveColor = Color(0xff909399)
      )
    }
  }

  if (isHovered) {
    return if (isDarkTheme) {
      ZDropdownMenuFieldStyle(
        backgroundColor = Color.Transparent,
        borderColor = Color(0xff6c6e72),
        textColor = Color(0xffcfd3dc),
        placeholderColor = Color(0xff8d9095),
        iconColor = Color(0xffa3a6ad),
        tagBackgroundColor = Color(0xff2f3133),
        tagTextColor = Color(0xffcfd3dc),
        tagRemoveColor = Color(0xffa3a6ad)
      )
    } else {
      ZDropdownMenuFieldStyle(
        backgroundColor = Color.Transparent,
        borderColor = Color(0xffc0c4cc),
        textColor = Color(0xff606266),
        placeholderColor = Color(0xffa8abb2),
        iconColor = Color(0xff909399),
        tagBackgroundColor = Color(0xfff0f2f5),
        tagTextColor = Color(0xff909399),
        tagRemoveColor = Color(0xff909399)
      )
    }
  }

  return if (isDarkTheme) {
    ZDropdownMenuFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = Color(0xff4c4d4f),
      textColor = Color(0xffcfd3dc),
      placeholderColor = Color(0xff8d9095),
      iconColor = Color(0xff8d9095),
      tagBackgroundColor = Color(0xff2f3133),
      tagTextColor = Color(0xffcfd3dc),
      tagRemoveColor = Color(0xffa3a6ad)
    )
  } else {
    ZDropdownMenuFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = Color(0xffdcdfe6),
      textColor = Color(0xff606266),
      placeholderColor = Color(0xffa8abb2),
      iconColor = Color(0xffa8abb2),
      tagBackgroundColor = Color(0xfff0f2f5),
      tagTextColor = Color(0xff909399),
      tagRemoveColor = Color(0xff909399)
    )
  }
}
