package top.zhjh.zui.composable

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronRight
import kotlinx.coroutines.delay

enum class ZMenuMode {
  Vertical,
  Horizontal
}

sealed interface ZMenuNode

data class ZMenuItem(
  val index: String,
  val title: String? = null,
  val enabled: Boolean = true,
  val icon: (@Composable () -> Unit)? = null,
  val trailing: (@Composable BoxScope.() -> Unit)? = null,
  val content: (@Composable () -> Unit)? = null
) : ZMenuNode

data class ZSubMenu(
  val index: String,
  val title: String? = null,
  val enabled: Boolean = true,
  val icon: (@Composable () -> Unit)? = null,
  val titleContent: (@Composable () -> Unit)? = null,
  val children: List<ZMenuNode>
) : ZMenuNode

data class ZMenuGroup(
  val title: String? = null,
  val titleContent: (@Composable () -> Unit)? = null,
  val children: List<ZMenuNode>
) : ZMenuNode

@Composable
fun ZMenu(
  items: List<ZMenuNode>,
  modifier: Modifier = Modifier,
  mode: ZMenuMode = ZMenuMode.Vertical,
  activeIndex: String? = null,
  defaultActive: String = "",
  defaultOpeneds: List<String> = emptyList(),
  uniqueOpened: Boolean = false,
  collapse: Boolean = false,
  backgroundColor: Color = Color(0xffffffff),
  textColor: Color = Color(0xff303133),
  activeTextColor: Color = Color(0xff409eff),
  showHorizontalDivider: Boolean = true,
  horizontalFillMaxWidth: Boolean = true,
  popperOffset: Dp = 6.dp,
  onSelect: (String) -> Unit = {}
) {
  var internalActiveIndex by remember(defaultActive) {
    mutableStateOf(defaultActive)
  }
  val resolvedActiveIndex = activeIndex ?: internalActiveIndex
  val palette = remember(backgroundColor, textColor, activeTextColor) {
    resolveZMenuPalette(
      backgroundColor = backgroundColor,
      textColor = textColor,
      activeTextColor = activeTextColor
    )
  }
  val openState = remember(items, defaultOpeneds) {
    mutableStateMapOf<String, Boolean>().apply {
      defaultOpeneds.forEach { opened ->
        this[opened] = true
      }
    }
  }
  var closePopupSignal by remember { mutableIntStateOf(0) }

  val onItemSelect: (String) -> Unit = { index ->
    if (activeIndex == null) {
      internalActiveIndex = index
    }
    closePopupSignal += 1
    onSelect(index)
  }
  val setInlineSubMenuExpanded: (String, Boolean) -> Unit = { index, expanded ->
    if (expanded && uniqueOpened) {
      openState.keys.forEach { key ->
        openState[key] = false
      }
    }
    openState[index] = expanded
  }

  val rootModifier = modifier.background(palette.backgroundColor)

  val rootContent: @Composable () -> Unit = {
    if (mode == ZMenuMode.Horizontal) {
      val horizontalModifier = if (horizontalFillMaxWidth) {
        Modifier.fillMaxWidth()
      } else {
        Modifier
      }
      Row(
        modifier = horizontalModifier
          .height(ZMenuDefaults.HorizontalItemHeight),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items.forEach { node ->
          when (node) {
            is ZMenuGroup -> {
              ZMenuInlineGroup(
                group = node,
                mode = mode,
                collapse = collapse,
                level = 0,
                inPopup = false,
                isTopLevel = true,
                resolvedActiveIndex = resolvedActiveIndex,
                palette = palette,
                openState = openState,
                setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                onItemSelect = onItemSelect,
                popperOffset = popperOffset,
                closePopupSignal = closePopupSignal
              )
            }

            is ZMenuItem -> {
              ZMenuItemNode(
                item = node,
                mode = mode,
                collapse = collapse,
                level = 0,
                inPopup = false,
                isTopLevel = true,
                resolvedActiveIndex = resolvedActiveIndex,
                palette = palette,
                onItemSelect = onItemSelect
              )
            }

            is ZSubMenu -> {
              ZSubMenuNode(
                submenu = node,
                mode = mode,
                collapse = collapse,
                level = 0,
                inPopup = false,
                isTopLevel = true,
                resolvedActiveIndex = resolvedActiveIndex,
                palette = palette,
                openState = openState,
                setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                onItemSelect = onItemSelect,
                popperOffset = popperOffset,
                closePopupSignal = closePopupSignal
              )
            }
          }
        }
      }
    } else {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        items.forEach { node ->
          when (node) {
            is ZMenuGroup -> {
              ZMenuInlineGroup(
                group = node,
                mode = mode,
                collapse = collapse,
                level = 0,
                inPopup = false,
                isTopLevel = true,
                resolvedActiveIndex = resolvedActiveIndex,
                palette = palette,
                openState = openState,
                setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                onItemSelect = onItemSelect,
                popperOffset = popperOffset,
                closePopupSignal = closePopupSignal
              )
            }

            is ZMenuItem -> {
              ZMenuItemNode(
                item = node,
                mode = mode,
                collapse = collapse,
                level = 0,
                inPopup = false,
                isTopLevel = true,
                resolvedActiveIndex = resolvedActiveIndex,
                palette = palette,
                onItemSelect = onItemSelect
              )
            }

            is ZSubMenu -> {
              ZSubMenuNode(
                submenu = node,
                mode = mode,
                collapse = collapse,
                level = 0,
                inPopup = false,
                isTopLevel = true,
                resolvedActiveIndex = resolvedActiveIndex,
                palette = palette,
                openState = openState,
                setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                onItemSelect = onItemSelect,
                popperOffset = popperOffset,
                closePopupSignal = closePopupSignal
              )
            }
          }
        }
      }
    }
  }

  Column(
    modifier = rootModifier
  ) {
    rootContent()
    if (mode == ZMenuMode.Horizontal && showHorizontalDivider) {
      Divider(color = palette.borderColor)
    }
  }
}

@Composable
private fun ZMenuInlineGroup(
  group: ZMenuGroup,
  mode: ZMenuMode,
  collapse: Boolean,
  level: Int,
  inPopup: Boolean,
  isTopLevel: Boolean,
  resolvedActiveIndex: String,
  palette: ZMenuPalette,
  openState: Map<String, Boolean>,
  setInlineSubMenuExpanded: (String, Boolean) -> Unit,
  onItemSelect: (String) -> Unit,
  popperOffset: Dp,
  closePopupSignal: Int
) {
  val flattenAsHorizontalTopLevelGroup = mode == ZMenuMode.Horizontal && !inPopup && level == 0 && isTopLevel
  val shouldRenderGroupTitle = !flattenAsHorizontalTopLevelGroup &&
    !(mode == ZMenuMode.Vertical && collapse && !inPopup && level == 0)

  if (shouldRenderGroupTitle) {
    val groupPaddingStart = if (inPopup) 14.dp else (16.dp + (level * 12).dp)
    val groupTitleContent = group.titleContent
    if (!group.title.isNullOrEmpty() || groupTitleContent != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = groupPaddingStart, end = 14.dp, top = if (level == 0) 12.dp else 8.dp, bottom = 8.dp)
      ) {
        if (groupTitleContent != null) {
          groupTitleContent()
        } else {
          Text(
            text = group.title.orEmpty(),
            color = palette.groupTitleColor,
            fontSize = ZMenuDefaults.GroupTitleFontSize
          )
        }
      }
    }
  }

  group.children.forEach { child ->
    val childLevel = if (flattenAsHorizontalTopLevelGroup) 0 else level + 1
    val childIsTopLevel = flattenAsHorizontalTopLevelGroup
    when (child) {
      is ZMenuGroup -> {
        ZMenuInlineGroup(
          group = child,
          mode = mode,
          collapse = collapse,
          level = if (flattenAsHorizontalTopLevelGroup) 0 else level,
          inPopup = inPopup,
          isTopLevel = childIsTopLevel,
          resolvedActiveIndex = resolvedActiveIndex,
          palette = palette,
          openState = openState,
          setInlineSubMenuExpanded = setInlineSubMenuExpanded,
          onItemSelect = onItemSelect,
          popperOffset = popperOffset,
          closePopupSignal = closePopupSignal
        )
      }

      is ZMenuItem -> {
        ZMenuItemNode(
          item = child,
          mode = mode,
          collapse = collapse,
          level = childLevel,
          inPopup = inPopup,
          isTopLevel = childIsTopLevel,
          resolvedActiveIndex = resolvedActiveIndex,
          palette = palette,
          onItemSelect = onItemSelect
        )
      }

      is ZSubMenu -> {
        ZSubMenuNode(
          submenu = child,
          mode = mode,
          collapse = collapse,
          level = childLevel,
          inPopup = inPopup,
          isTopLevel = childIsTopLevel,
          resolvedActiveIndex = resolvedActiveIndex,
          palette = palette,
          openState = openState,
          setInlineSubMenuExpanded = setInlineSubMenuExpanded,
          onItemSelect = onItemSelect,
          popperOffset = popperOffset,
          closePopupSignal = closePopupSignal
        )
      }
    }
  }
}

@Composable
private fun ZMenuItemNode(
  item: ZMenuItem,
  mode: ZMenuMode,
  collapse: Boolean,
  level: Int,
  inPopup: Boolean,
  isTopLevel: Boolean,
  resolvedActiveIndex: String,
  palette: ZMenuPalette,
  onItemSelect: (String) -> Unit
) {
  val isActive = item.index == resolvedActiveIndex
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val shouldCollapseTopItem = mode == ZMenuMode.Vertical && collapse && !inPopup && level == 0
  val isEnabled = item.enabled

  val textColor = when {
    !isEnabled -> palette.disabledTextColor
    isActive -> palette.activeTextColor
    else -> palette.textColor
  }
  val isHorizontalTopLevel = mode == ZMenuMode.Horizontal && isTopLevel && !inPopup
  val itemHeight = if (isHorizontalTopLevel) {
    ZMenuDefaults.HorizontalItemHeight - ZMenuDefaults.HorizontalActiveLineHeight
  } else {
    ZMenuDefaults.VerticalItemHeight
  }
  val itemPaddingStart = when {
    mode == ZMenuMode.Horizontal && !inPopup -> 16.dp
    shouldCollapseTopItem -> 0.dp
    else -> (16.dp + (level * 12).dp)
  }
  val itemPaddingEnd = if (shouldCollapseTopItem) 0.dp else 14.dp

  val itemBackground = when {
    isHorizontalTopLevel -> Color.Transparent
    isHovered -> palette.hoverBackgroundColor
    isActive -> palette.activeBackgroundColor
    else -> Color.Transparent
  }
  val bottomActiveLineColor = if (isHorizontalTopLevel && isActive && isEnabled) {
    palette.activeTextColor
  } else {
    Color.Transparent
  }
  Column(
    modifier = if (isHorizontalTopLevel) {
      Modifier.width(IntrinsicSize.Max)
    } else {
      Modifier.fillMaxWidth()
    }
  ) {
    Row(
      modifier = Modifier
        .then(if (isHorizontalTopLevel) Modifier else Modifier.fillMaxWidth())
        .heightIn(min = itemHeight)
        .background(itemBackground)
        .then(
          if (isEnabled) {
            Modifier
              .hoverable(interactionSource = interactionSource)
              .clickable(
                interactionSource = interactionSource,
                indication = null
              ) {
                onItemSelect(item.index)
              }
          } else {
            Modifier
          }
        )
        .padding(start = itemPaddingStart, end = itemPaddingEnd, top = 0.dp, bottom = 0.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = if (shouldCollapseTopItem) Arrangement.Center else Arrangement.Start
    ) {
      val iconContent = item.icon
      if (iconContent != null) {
        Box(
          modifier = Modifier.size(ZMenuDefaults.IconSlotSize),
          contentAlignment = Alignment.Center
        ) {
          CompositionLocalProvider(LocalContentColor provides textColor) {
            iconContent()
          }
        }
        if (!shouldCollapseTopItem) {
          Spacer(modifier = Modifier.width(8.dp))
        }
      }

      if (!shouldCollapseTopItem) {
        val textContent = item.content
        if (textContent != null) {
          Box(
            modifier = if (isHorizontalTopLevel) Modifier else Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
          ) {
            textContent()
          }
        } else {
          Text(
            text = item.title.orEmpty(),
            color = textColor,
            fontSize = ZMenuDefaults.ItemFontSize,
            maxLines = if (isHorizontalTopLevel) 1 else Int.MAX_VALUE,
            softWrap = !isHorizontalTopLevel,
            modifier = if (isHorizontalTopLevel) Modifier else Modifier.weight(1f)
          )
        }

        if (item.trailing != null) {
          Box(
            contentAlignment = Alignment.CenterEnd,
            content = item.trailing
          )
        }
      } else if (iconContent == null) {
        Text(
          text = item.title?.take(1).orEmpty(),
          color = textColor,
          fontSize = ZMenuDefaults.ItemFontSize
        )
      }
    }

    if (isHorizontalTopLevel) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(ZMenuDefaults.HorizontalActiveLineHeight)
          .background(bottomActiveLineColor)
      )
    }
  }
}

@Composable
private fun ZSubMenuNode(
  submenu: ZSubMenu,
  mode: ZMenuMode,
  collapse: Boolean,
  level: Int,
  inPopup: Boolean,
  isTopLevel: Boolean,
  resolvedActiveIndex: String,
  palette: ZMenuPalette,
  openState: Map<String, Boolean>,
  setInlineSubMenuExpanded: (String, Boolean) -> Unit,
  onItemSelect: (String) -> Unit,
  popperOffset: Dp,
  closePopupSignal: Int,
  onPopupVisibleChange: ((Boolean) -> Unit)? = null
) {
  val usePopup = mode == ZMenuMode.Horizontal || inPopup || (mode == ZMenuMode.Vertical && collapse && level == 0)
  if (usePopup) {
    val placement = when {
      mode == ZMenuMode.Horizontal && !inPopup && isTopLevel -> ZMenuPopupPlacement.Below
      mode == ZMenuMode.Vertical && collapse && level == 0 && !inPopup -> ZMenuPopupPlacement.Right
      else -> ZMenuPopupPlacement.Right
    }
    ZSubMenuPopupTrigger(
      submenu = submenu,
      mode = mode,
      collapse = collapse,
      level = level,
      inPopup = inPopup,
      isTopLevel = isTopLevel,
      placement = placement,
      resolvedActiveIndex = resolvedActiveIndex,
      palette = palette,
      openState = openState,
      setInlineSubMenuExpanded = setInlineSubMenuExpanded,
      onItemSelect = onItemSelect,
      popperOffset = popperOffset,
      closePopupSignal = closePopupSignal,
      onPopupVisibleChange = onPopupVisibleChange
    )
  } else {
    val expanded = openState[submenu.index] == true
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isActive = isMenuSubtreeActive(submenu.children, resolvedActiveIndex)
    val itemBackground = when {
      isHovered -> palette.hoverBackgroundColor
      isActive -> palette.activeBackgroundColor
      else -> Color.Transparent
    }
    val textColor = when {
      !submenu.enabled -> palette.disabledTextColor
      isActive -> palette.activeTextColor
      else -> palette.textColor
    }
    val titlePaddingStart = (16.dp + (level * 12).dp)

    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(ZMenuDefaults.VerticalItemHeight)
          .background(itemBackground)
          .then(
            if (submenu.enabled) {
              Modifier
                .hoverable(interactionSource)
                .clickable(
                  interactionSource = interactionSource,
                  indication = null
                ) {
                  setInlineSubMenuExpanded(submenu.index, !expanded)
                }
            } else {
              Modifier
            }
          )
          .padding(start = titlePaddingStart, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (submenu.icon != null) {
          Box(
            modifier = Modifier.size(ZMenuDefaults.IconSlotSize),
            contentAlignment = Alignment.Center
          ) {
            CompositionLocalProvider(LocalContentColor provides textColor) {
              submenu.icon.invoke()
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        }

        if (submenu.titleContent != null) {
          Box(modifier = Modifier.weight(1f)) {
            submenu.titleContent.invoke()
          }
        } else {
          Text(
            text = submenu.title.orEmpty(),
            color = textColor,
            fontSize = ZMenuDefaults.ItemFontSize,
            modifier = Modifier.weight(1f)
          )
        }

        Icon(
          imageVector = if (expanded) FeatherIcons.ChevronDown else FeatherIcons.ChevronRight,
          contentDescription = "submenu",
          tint = textColor,
          modifier = Modifier.size(14.dp)
        )
      }

      if (expanded) {
        Column(
          modifier = Modifier.fillMaxWidth()
        ) {
          submenu.children.forEach { child ->
            when (child) {
              is ZMenuGroup -> {
                ZMenuInlineGroup(
                  group = child,
                  mode = mode,
                  collapse = collapse,
                  level = level + 1,
                  inPopup = false,
                  isTopLevel = false,
                  resolvedActiveIndex = resolvedActiveIndex,
                  palette = palette,
                  openState = openState,
                  setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                  onItemSelect = onItemSelect,
                  popperOffset = popperOffset,
                  closePopupSignal = closePopupSignal
                )
              }

              is ZMenuItem -> {
                ZMenuItemNode(
                  item = child,
                  mode = mode,
                  collapse = collapse,
                  level = level + 1,
                  inPopup = false,
                  isTopLevel = false,
                  resolvedActiveIndex = resolvedActiveIndex,
                  palette = palette,
                  onItemSelect = onItemSelect
                )
              }

              is ZSubMenu -> {
                ZSubMenuNode(
                  submenu = child,
                  mode = mode,
                  collapse = collapse,
                  level = level + 1,
                  inPopup = false,
                  isTopLevel = false,
                  resolvedActiveIndex = resolvedActiveIndex,
                  palette = palette,
                  openState = openState,
                  setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                  onItemSelect = onItemSelect,
                  popperOffset = popperOffset,
                  closePopupSignal = closePopupSignal
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ZSubMenuPopupTrigger(
  submenu: ZSubMenu,
  mode: ZMenuMode,
  collapse: Boolean,
  level: Int,
  inPopup: Boolean,
  isTopLevel: Boolean,
  placement: ZMenuPopupPlacement,
  resolvedActiveIndex: String,
  palette: ZMenuPalette,
  openState: Map<String, Boolean>,
  setInlineSubMenuExpanded: (String, Boolean) -> Unit,
  onItemSelect: (String) -> Unit,
  popperOffset: Dp,
  closePopupSignal: Int,
  onPopupVisibleChange: ((Boolean) -> Unit)? = null
) {
  var popupVisible by remember(submenu.index) { mutableStateOf(false) }
  val childPopupVisibility = remember(submenu.index) { mutableStateMapOf<String, Boolean>() }
  val hasVisibleChildPopup by remember {
    derivedStateOf { childPopupVisibility.values.any { it } }
  }
  val triggerInteractionSource = remember { MutableInteractionSource() }
  val popupInteractionSource = remember { MutableInteractionSource() }
  val isTriggerHovered by triggerInteractionSource.collectIsHoveredAsState()
  val isPopupHovered by popupInteractionSource.collectIsHoveredAsState()

  LaunchedEffect(closePopupSignal) {
    popupVisible = false
    childPopupVisibility.clear()
  }
  LaunchedEffect(popupVisible) {
    onPopupVisibleChange?.invoke(popupVisible)
    if (!popupVisible) {
      childPopupVisibility.clear()
    }
  }
  LaunchedEffect(isTriggerHovered, isPopupHovered, hasVisibleChildPopup) {
    if (isTriggerHovered || isPopupHovered || hasVisibleChildPopup) {
      popupVisible = true
    } else {
      delay(ZMenuDefaults.PopupHideDelayMillis)
      if (!isTriggerHovered && !isPopupHovered && !hasVisibleChildPopup) {
        popupVisible = false
      }
    }
  }

  val isActive = isMenuSubtreeActive(submenu.children, resolvedActiveIndex)
  val textColor = when {
    !submenu.enabled -> palette.disabledTextColor
    isActive -> palette.activeTextColor
    else -> palette.textColor
  }
  val isHorizontalTopLevel = mode == ZMenuMode.Horizontal && isTopLevel && !inPopup
  val itemHeight = if (isHorizontalTopLevel) {
    ZMenuDefaults.HorizontalItemHeight - ZMenuDefaults.HorizontalActiveLineHeight
  } else {
    ZMenuDefaults.VerticalItemHeight
  }
  val shouldCollapseTopItem = mode == ZMenuMode.Vertical && collapse && !inPopup && level == 0
  val titlePaddingStart = when {
    mode == ZMenuMode.Horizontal && !inPopup -> 16.dp
    shouldCollapseTopItem -> 0.dp
    else -> (16.dp + (level * 12).dp)
  }
  val titlePaddingEnd = if (shouldCollapseTopItem) 0.dp else 14.dp
  val itemBackground = if (isHorizontalTopLevel) {
    Color.Transparent
  } else if (isTriggerHovered) {
    palette.hoverBackgroundColor
  } else {
    Color.Transparent
  }
  val showRightArrow = placement == ZMenuPopupPlacement.Right && !shouldCollapseTopItem
  val showDownArrow = placement == ZMenuPopupPlacement.Below && !shouldCollapseTopItem
  val arrowModifier = if (isHorizontalTopLevel && showDownArrow) {
    Modifier
      .padding(start = 8.dp)
      .size(14.dp)
  } else {
    Modifier.size(14.dp)
  }
  val bottomActiveLineColor = if (isHorizontalTopLevel && isActive && submenu.enabled) {
    palette.activeTextColor
  } else {
    Color.Transparent
  }

  Box {
    Column(
      modifier = if (isHorizontalTopLevel) {
        Modifier.width(IntrinsicSize.Max)
      } else {
        Modifier.fillMaxWidth()
      }
    ) {
      Row(
        modifier = Modifier
          .then(if (isHorizontalTopLevel) Modifier else Modifier.fillMaxWidth())
          .height(itemHeight)
          .background(itemBackground)
          .then(
            if (submenu.enabled) {
              Modifier
                .hoverable(triggerInteractionSource)
                .clickable(
                  interactionSource = triggerInteractionSource,
                  indication = null
                ) {
                  popupVisible = !popupVisible
                }
            } else {
              Modifier
            }
          )
          .padding(start = titlePaddingStart, end = titlePaddingEnd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (shouldCollapseTopItem) Arrangement.Center else Arrangement.Start
      ) {
        if (submenu.icon != null) {
          Box(
            modifier = Modifier.size(ZMenuDefaults.IconSlotSize),
            contentAlignment = Alignment.Center
          ) {
            CompositionLocalProvider(LocalContentColor provides textColor) {
              submenu.icon.invoke()
            }
          }
          if (!shouldCollapseTopItem) {
            Spacer(modifier = Modifier.width(8.dp))
          }
        }

        if (!shouldCollapseTopItem) {
          if (submenu.titleContent != null) {
            Box(
              modifier = if (isHorizontalTopLevel) Modifier else Modifier.weight(1f)
            ) {
              submenu.titleContent.invoke()
            }
          } else {
            Text(
              text = submenu.title.orEmpty(),
              color = textColor,
              fontSize = ZMenuDefaults.ItemFontSize,
              lineHeight = if (isHorizontalTopLevel) ZMenuDefaults.ItemFontSize else TextUnit.Unspecified,
              maxLines = if (isHorizontalTopLevel) 1 else Int.MAX_VALUE,
              softWrap = !isHorizontalTopLevel,
              modifier = if (isHorizontalTopLevel) Modifier else Modifier.weight(1f)
            )
          }
        } else if (submenu.icon == null) {
          Text(
            text = submenu.title?.take(1).orEmpty(),
            color = textColor,
            fontSize = ZMenuDefaults.ItemFontSize
          )
        }

        if (showDownArrow) {
          Icon(
            imageVector = FeatherIcons.ChevronDown,
            contentDescription = "submenu",
            tint = textColor,
            modifier = arrowModifier
          )
        } else if (showRightArrow) {
          Icon(
            imageVector = FeatherIcons.ChevronRight,
            contentDescription = "submenu",
            tint = textColor,
            modifier = arrowModifier
          )
        }
      }

      if (isHorizontalTopLevel) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(ZMenuDefaults.HorizontalActiveLineHeight)
            .background(bottomActiveLineColor)
        )
      }
    }

    if (popupVisible && submenu.enabled) {
      val popupGapPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        (if (placement == ZMenuPopupPlacement.Right) 0.dp else popperOffset).roundToPx()
      }
      Popup(
        popupPositionProvider = zMenuPopupPositionProvider(
          placement = placement,
          gapPx = popupGapPx
        ),
        properties = PopupProperties(
          focusable = false,
          dismissOnBackPress = false,
          dismissOnClickOutside = false
        )
      ) {
        Surface(
          color = palette.popupBackgroundColor,
          shape = RoundedCornerShape(4.dp),
          elevation = 8.dp,
          modifier = Modifier.hoverable(popupInteractionSource)
        ) {
          Column(
            modifier = Modifier
              .width(IntrinsicSize.Min)
              .widthIn(min = 180.dp)
              .padding(vertical = 6.dp)
          ) {
            submenu.children.forEach { child ->
              when (child) {
                is ZMenuGroup -> {
                  ZMenuInlineGroup(
                    group = child,
                    mode = mode,
                    collapse = collapse,
                    level = 0,
                    inPopup = true,
                    isTopLevel = false,
                    resolvedActiveIndex = resolvedActiveIndex,
                    palette = palette,
                    openState = openState,
                    setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                    onItemSelect = onItemSelect,
                    popperOffset = popperOffset,
                    closePopupSignal = closePopupSignal
                  )
                }

                is ZMenuItem -> {
                  ZMenuItemNode(
                    item = child,
                    mode = mode,
                    collapse = false,
                    level = 0,
                    inPopup = true,
                    isTopLevel = false,
                    resolvedActiveIndex = resolvedActiveIndex,
                    palette = palette,
                    onItemSelect = onItemSelect
                  )
                }

                is ZSubMenu -> {
                  ZSubMenuNode(
                    submenu = child,
                    mode = mode,
                    collapse = false,
                    level = 0,
                    inPopup = true,
                    isTopLevel = false,
                    resolvedActiveIndex = resolvedActiveIndex,
                    palette = palette,
                    openState = openState,
                    setInlineSubMenuExpanded = setInlineSubMenuExpanded,
                    onItemSelect = onItemSelect,
                    popperOffset = popperOffset,
                    closePopupSignal = closePopupSignal,
                    onPopupVisibleChange = { isVisible ->
                      if (isVisible) {
                        childPopupVisibility[child.index] = true
                      } else {
                        childPopupVisibility.remove(child.index)
                      }
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

private enum class ZMenuPopupPlacement {
  Below,
  Right
}

private fun zMenuPopupPositionProvider(
  placement: ZMenuPopupPlacement,
  gapPx: Int
): PopupPositionProvider {
  return object : PopupPositionProvider {
    override fun calculatePosition(
      anchorBounds: IntRect,
      windowSize: IntSize,
      layoutDirection: LayoutDirection,
      popupContentSize: IntSize
    ): IntOffset {
      val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
      val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
      val resolvedX: Int
      val resolvedY: Int

      when (placement) {
        ZMenuPopupPlacement.Below -> {
          val belowLeftX = anchorBounds.left
          val belowRightX = anchorBounds.right - popupContentSize.width
          resolvedX = when {
            belowLeftX + popupContentSize.width <= windowSize.width && belowLeftX >= 0 -> belowLeftX
            belowRightX >= 0 -> belowRightX
            else -> belowLeftX.coerceIn(0, maxX)
          }

          val belowY = anchorBounds.bottom + gapPx
          val aboveY = anchorBounds.top - gapPx - popupContentSize.height
          resolvedY = when {
            belowY + popupContentSize.height <= windowSize.height -> belowY
            aboveY >= 0 -> aboveY
            else -> belowY.coerceIn(0, maxY)
          }
        }

        ZMenuPopupPlacement.Right -> {
          val rightX = anchorBounds.right + gapPx
          val leftX = anchorBounds.left - gapPx - popupContentSize.width
          resolvedX = when {
            rightX + popupContentSize.width <= windowSize.width -> rightX
            leftX >= 0 -> leftX
            else -> rightX.coerceIn(0, maxX)
          }

          val topY = anchorBounds.top
          val bottomAlignedY = anchorBounds.bottom - popupContentSize.height
          resolvedY = when {
            topY + popupContentSize.height <= windowSize.height && topY >= 0 -> topY
            bottomAlignedY >= 0 -> bottomAlignedY
            else -> topY.coerceIn(0, maxY)
          }
        }
      }

      return IntOffset(resolvedX, resolvedY)
    }
  }
}

private fun isMenuSubtreeActive(
  nodes: List<ZMenuNode>,
  activeIndex: String
): Boolean {
  nodes.forEach { node ->
    when (node) {
      is ZMenuItem -> if (node.index == activeIndex) return true
      is ZSubMenu -> {
        if (node.index == activeIndex) return true
        if (isMenuSubtreeActive(node.children, activeIndex)) return true
      }

      is ZMenuGroup -> {
        if (isMenuSubtreeActive(node.children, activeIndex)) return true
      }
    }
  }
  return false
}

private data class ZMenuPalette(
  val backgroundColor: Color,
  val popupBackgroundColor: Color,
  val textColor: Color,
  val activeTextColor: Color,
  val disabledTextColor: Color,
  val hoverBackgroundColor: Color,
  val activeBackgroundColor: Color,
  val groupTitleColor: Color,
  val borderColor: Color
)

private fun resolveZMenuPalette(
  backgroundColor: Color,
  textColor: Color,
  activeTextColor: Color
): ZMenuPalette {
  val darkBackground = backgroundColor.luminance() < 0.5f
  val popupBackgroundColor = backgroundColor
  val hoverBackgroundColor = if (darkBackground) {
    lerp(backgroundColor, Color.White, 0.08f)
  } else {
    Color(0xffecf5ff)
  }
  val activeBackgroundColor = if (darkBackground) {
    lerp(backgroundColor, Color.White, 0.12f)
  } else {
    Color(0xffecf5ff)
  }
  val disabledTextColor = if (darkBackground) {
    textColor.copy(alpha = 0.38f)
  } else {
    Color(0xffc0c4cc)
  }
  val groupTitleColor = if (darkBackground) {
    textColor.copy(alpha = 0.55f)
  } else {
    Color(0xff909399)
  }
  val borderColor = if (darkBackground) {
    Color(0xff4c4d4f)
  } else {
    Color(0xffdcdfe6)
  }
  return ZMenuPalette(
    backgroundColor = backgroundColor,
    popupBackgroundColor = popupBackgroundColor,
    textColor = textColor,
    activeTextColor = activeTextColor,
    disabledTextColor = disabledTextColor,
    hoverBackgroundColor = hoverBackgroundColor,
    activeBackgroundColor = activeBackgroundColor,
    groupTitleColor = groupTitleColor,
    borderColor = borderColor
  )
}

object ZMenuDefaults {
  val VerticalItemHeight = 44.dp
  val HorizontalItemHeight = 56.dp
  val HorizontalActiveLineHeight = 2.dp
  val IconSlotSize = 18.dp
  val ItemFontSize = 14.sp
  val GroupTitleFontSize = 14.sp
  const val PopupHideDelayMillis = 320L
}
