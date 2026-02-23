package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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

data class ZDropdownMenuOptionGroup(
  val label: String,
  val options: List<String>
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
  filterable: Boolean = false,
  filterMethod: ((inputValue: String, option: String) -> Boolean)? = null,
  allowCreate: Boolean = false,
  defaultFirstOption: Boolean = false,
  multiple: Boolean = false,
  values: List<String>? = null,
  defaultSelectedOptions: List<String> = emptyList(),
  onOptionsSelected: (List<String>) -> Unit = {},
  collapseTags: Boolean = false,
  collapseTagsTooltip: Boolean = false,
  maxCollapseTags: Int? = null,
  optionGroups: List<ZDropdownMenuOptionGroup> = emptyList(),
  dropdownHeader: (@Composable () -> Unit)? = null,
  dropdownFooter: (@Composable () -> Unit)? = null
) {
  val textStyle = LocalTextStyle.current.copy(
    fontSize = fontSize,
    lineHeight = lineHeight.sp
  )
  val compactFieldTextStyle = textStyle.copy(lineHeight = TextUnit.Unspecified)

  var expanded by remember { mutableStateOf(false) }
  // Prevent ExposedDropdownMenuBox from toggling when clicking inner clear/remove controls.
  var suppressNextToggle by remember { mutableStateOf(false) }
  var filterKeyword by remember { mutableStateOf("") }
  val multiFilterInputFocusRequester = remember { FocusRequester() }
  val hoverInteractionSource = remember { MutableInteractionSource() }
  val isHovered by hoverInteractionSource.collectIsHoveredAsState()
  LaunchedEffect(enabled) {
    if (!enabled) {
      expanded = false
    }
  }

  val resolvedOptionGroups = optionGroups.filter { it.options.isNotEmpty() }
  val baseResolvedOptions = resolveDropdownMenuOptions(options, resolvedOptionGroups)
  val allowCreateEnabled = allowCreate && filterable
  val createdOptions = remember { mutableStateListOf<String>() }
  val addCreatedOptionIfNeeded: (String) -> Unit = { rawOption ->
    if (allowCreateEnabled) {
      val normalizedOption = rawOption.trim()
      if (
        normalizedOption.isNotEmpty() &&
        normalizedOption !in baseResolvedOptions &&
        normalizedOption !in createdOptions
      ) {
        createdOptions.add(normalizedOption)
      }
    }
  }
  val resolvedOptions = (baseResolvedOptions + createdOptions).distinct()

  var internalSelectedOption by remember {
    mutableStateOf(defaultSelectedOption?.takeIf { it in baseResolvedOptions } ?: "")
  }
  var internalSelectedOptions by remember {
    mutableStateOf(normalizeDropdownMenuValues(defaultSelectedOptions, baseResolvedOptions))
  }

  LaunchedEffect(defaultSelectedOption, value, resolvedOptions, allowCreateEnabled) {
    if (value == null) {
      internalSelectedOption = normalizeDropdownMenuSingleValue(defaultSelectedOption, resolvedOptions, allowCreateEnabled)
    }
  }
  LaunchedEffect(defaultSelectedOptions, values, resolvedOptions, allowCreateEnabled) {
    if (values == null) {
      internalSelectedOptions = normalizeDropdownMenuValues(
        defaultSelectedOptions,
        resolvedOptions,
        allowUnknown = allowCreateEnabled
      )
    }
  }

  val selectedOption = normalizeDropdownMenuSingleValue(value ?: internalSelectedOption, resolvedOptions, allowCreateEnabled)
  val selectedOptions = normalizeDropdownMenuValues(
    values ?: internalSelectedOptions,
    resolvedOptions,
    allowUnknown = allowCreateEnabled
  )
  LaunchedEffect(allowCreateEnabled, baseResolvedOptions, selectedOption, selectedOptions) {
    if (!allowCreateEnabled) return@LaunchedEffect
    buildList {
      if (selectedOption.isNotEmpty()) add(selectedOption)
      addAll(selectedOptions)
    }
      .forEach(addCreatedOptionIfNeeded)
  }

  val enableFiltering = filterable
  LaunchedEffect(enableFiltering, selectedOption, expanded, multiple) {
    if (enableFiltering && !expanded) {
      if (multiple) {
        filterKeyword = ""
      } else {
        filterKeyword = selectedOption
      }
    }
    if (!enableFiltering) {
      filterKeyword = ""
    }
  }
  LaunchedEffect(expanded, enableFiltering, multiple, enabled) {
    if (expanded && enableFiltering && multiple && enabled) {
      multiFilterInputFocusRequester.requestFocus()
    }
  }
  val normalizedFilterKeyword = if (enableFiltering) filterKeyword.trim() else ""
  val resolvedFilterMethod = filterMethod ?: { keyword: String, option: String ->
    option.contains(keyword, ignoreCase = true)
  }
  val optionMatchesFilter: (String) -> Boolean = { option ->
    normalizedFilterKeyword.isEmpty() || resolvedFilterMethod(normalizedFilterKeyword, option)
  }
  val filteredOptionGroups = if (resolvedOptionGroups.isNotEmpty()) {
    resolvedOptionGroups
      .map { group ->
        if (normalizedFilterKeyword.isEmpty()) {
          group
        } else {
          group.copy(options = group.options.filter(optionMatchesFilter))
        }
      }
      .filter { it.options.isNotEmpty() }
  } else {
    emptyList()
  }
  val filteredOptions = if (resolvedOptionGroups.isNotEmpty()) {
    (filteredOptionGroups.flatMap { it.options } + createdOptions.filter(optionMatchesFilter)).distinct()
  } else {
    resolvedOptions.filter(optionMatchesFilter)
  }
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
    val normalizedInput = option.trim()
    if (normalizedInput.isNotEmpty()) {
      addCreatedOptionIfNeeded(normalizedInput)
    }
    val normalized = normalizeDropdownMenuSingleValue(normalizedInput, resolvedOptions, allowCreateEnabled)
    if (value == null) {
      internalSelectedOption = normalized
    }
    if (enableFiltering) {
      filterKeyword = normalized
    }
    onOptionSelected(normalized)
  }
  val updateMultiSelection: (List<String>) -> Unit = { selections ->
    if (allowCreateEnabled) {
      selections.forEach(addCreatedOptionIfNeeded)
    }
    val normalized = normalizeDropdownMenuValues(
      selections,
      resolvedOptions,
      allowUnknown = allowCreateEnabled
    )
    if (values == null) {
      internalSelectedOptions = normalized
    }
    onOptionsSelected(normalized)
  }
  val submitFilterKeywordSelection: () -> Unit = submit@{
    if (!enabled || !enableFiltering) return@submit
    val keyword = filterKeyword.trim()

    val exactOption = if (keyword.isNotEmpty()) {
      resolvedOptions.firstOrNull { it == keyword }
        ?.takeUnless { it in disabledOptions }
    } else {
      null
    }
    val firstFilteredOption = if (defaultFirstOption) {
      filteredOptions.firstOrNull { it !in disabledOptions }
    } else {
      null
    }
    val optionToSelect = exactOption ?: firstFilteredOption

    if (optionToSelect != null) {
      if (multiple) {
        if (optionToSelect !in selectedOptions) {
          updateMultiSelection(selectedOptions + optionToSelect)
        }
        filterKeyword = ""
        expanded = true
      } else {
        updateSingleSelection(optionToSelect)
        expanded = false
      }
      return@submit
    }

    if (keyword.isEmpty()) {
      return@submit
    }

    if (allowCreateEnabled && keyword !in disabledOptions) {
      if (multiple) {
        if (keyword !in selectedOptions) {
          updateMultiSelection(selectedOptions + keyword)
        }
        filterKeyword = ""
        expanded = true
      } else {
        updateSingleSelection(keyword)
        expanded = false
      }
    }
  }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = {
      if (suppressNextToggle) {
        suppressNextToggle = false
        return@ExposedDropdownMenuBox
      }
      if (enabled) {
        expanded = if (enableFiltering) true else !expanded
      }
    }
  ) {
    if (!multiple) {
      ZTextField(
        size = resolvedFormSize,
        value = if (enableFiltering) filterKeyword else selectedOption,
        onValueChange = { input ->
          if (enableFiltering) {
            filterKeyword = input
            if (enabled && !expanded) {
              expanded = true
            }
          }
        },
        enabled = enabled,
        readOnly = !enableFiltering,
        modifier = modifier
          .hoverable(hoverInteractionSource)
          .onPreviewKeyEvent { keyEvent ->
            if (
              enableFiltering &&
              keyEvent.type == KeyEventType.KeyDown &&
              keyEvent.key == Key.Enter
            ) {
              submitFilterKeywordSelection()
              true
            } else {
              false
            }
          },
        placeholder = if (enableFiltering) {
          placeholder
        } else {
          if (selectedOption.isEmpty()) placeholder else selectedOption
        },
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
                if (enableFiltering) {
                  filterKeyword = ""
                }
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
          .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.weight(1f)) {
            if (selectedOptions.isEmpty() && !enableFiltering) {
              Text(
                text = placeholder,
                style = compactFieldTextStyle,
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
                    textStyle = compactFieldTextStyle,
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
                      textStyle = compactFieldTextStyle,
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
                                  textStyle = compactFieldTextStyle,
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
                if (enableFiltering) {
                  BasicTextField(
                    value = filterKeyword,
                    onValueChange = { input ->
                      filterKeyword = input
                      if (enabled && !expanded) {
                        expanded = true
                      }
                    },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = compactFieldTextStyle.copy(color = fieldStyle.textColor),
                    cursorBrush = SolidColor(fieldStyle.textColor),
                    modifier = Modifier
                      .padding(
                        start = if (selectedOptions.isEmpty()) 4.dp else 0.dp,
                        top = 4.dp,
                        bottom = 4.dp
                      )
                      .widthIn(min = 48.dp, max = 220.dp)
                      .focusRequester(multiFilterInputFocusRequester)
                      .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                          submitFilterKeywordSelection()
                          true
                        } else {
                          false
                        }
                      },
                    decorationBox = { innerTextField ->
                      Box(contentAlignment = Alignment.CenterStart) {
                        innerTextField()
                        if (selectedOptions.isEmpty() && filterKeyword.isEmpty()) {
                          Text(
                            text = placeholder,
                            style = compactFieldTextStyle,
                            color = fieldStyle.placeholderColor
                          )
                        }
                      }
                    }
                  )
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

    val popupFocusable = dropdownFooter != null && !enableFiltering
    DropdownMenu(
      expanded = expanded && enabled,
      onDismissRequest = { expanded = false },
      modifier = Modifier.exposedDropdownSize(),
      properties = PopupProperties(focusable = popupFocusable)
    ) {
      val hasHeader = dropdownHeader != null
      val hasFooter = dropdownFooter != null
      val hasOptions = filteredOptions.isNotEmpty()
      val groupLabelColor = if (isDarkTheme) Color(0xff8d9095) else Color(0xff909399)

      if (hasHeader) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 8.dp)
        ) {
          dropdownHeader?.invoke()
        }
        Divider()
      }
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            top = if (hasHeader && hasOptions) 8.dp else 0.dp,
            bottom = if (hasFooter && hasOptions) 8.dp else 0.dp
          )
      ) {
        val optionItemContent: @Composable (String) -> Unit = { option ->
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
                if (enableFiltering) {
                  filterKeyword = ""
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
        if (filteredOptionGroups.isNotEmpty()) {
          filteredOptionGroups.forEach { group ->
            Text(
              text = group.label,
              style = compactFieldTextStyle,
              color = groupLabelColor,
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
            )
            group.options.forEach { option ->
              optionItemContent(option)
            }
          }
        } else {
          filteredOptions.forEach { option ->
            optionItemContent(option)
          }
        }
      }
      if (hasFooter) {
        if (hasOptions) {
          Divider()
        }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 0.dp)
        ) {
          dropdownFooter?.invoke()
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
  options: List<String>,
  allowUnknown: Boolean = false
): List<String> {
  if (selections.isEmpty()) return emptyList()
  val normalizedSelections = selections
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .distinct()
  if (normalizedSelections.isEmpty()) return emptyList()
  if (options.isEmpty()) {
    return if (allowUnknown) normalizedSelections else emptyList()
  }
  val selectedSet = normalizedSelections.toSet()
  val knownSelections = options.filter { it in selectedSet }
  if (!allowUnknown) return knownSelections
  val knownSelectionSet = knownSelections.toSet()
  val unknownSelections = normalizedSelections.filter { it !in knownSelectionSet }
  return knownSelections + unknownSelections
}

private fun normalizeDropdownMenuSingleValue(
  selection: String?,
  options: List<String>,
  allowUnknown: Boolean
): String {
  val normalizedSelection = selection?.trim().orEmpty()
  if (normalizedSelection.isEmpty()) return ""
  if (normalizedSelection in options) return normalizedSelection
  return if (allowUnknown) normalizedSelection else ""
}

private fun resolveDropdownMenuOptions(
  options: List<String>,
  optionGroups: List<ZDropdownMenuOptionGroup>
): List<String> {
  if (optionGroups.isEmpty()) {
    return options
  }
  return optionGroups
    .flatMap { it.options }
    .distinct()
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
