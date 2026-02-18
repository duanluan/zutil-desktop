package top.zhjh.zui.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.X
import kotlinx.coroutines.launch
import top.zhjh.zui.theme.isAppInDarkTheme

enum class ZTabsType {
  LINE,
  CARD,
  BORDER_CARD
}

enum class ZTabsPosition {
  TOP,
  RIGHT,
  BOTTOM,
  LEFT
}

enum class ZTabsEditAction {
  ADD,
  REMOVE
}

data class ZTabPane(
  val label: String,
  val name: String,
  val enabled: Boolean = true,
  val closable: Boolean = false,
  val lazy: Boolean = false,
  val content: @Composable () -> Unit
)

@Composable
fun ZTabs(
  tabs: List<ZTabPane>,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = ZTabsDefaults.ContentPadding,
  activeName: String? = null,
  defaultActiveName: String? = null,
  type: ZTabsType = ZTabsType.LINE,
  tabPosition: ZTabsPosition = ZTabsPosition.TOP,
  closable: Boolean = false,
  addable: Boolean = false,
  editable: Boolean = false,
  stretch: Boolean = false,
  beforeLeave: ((newTabName: String, oldTabName: String) -> Boolean)? = null,
  onActiveNameChange: (String) -> Unit = {},
  onTabClick: ((ZTabPane) -> Unit)? = null,
  onTabChange: ((String) -> Unit)? = null,
  onTabAdd: (() -> Unit)? = null,
  onTabRemove: ((String) -> Unit)? = null,
  onEdit: ((targetName: String?, action: ZTabsEditAction) -> Unit)? = null
) {
  val isDarkTheme = isAppInDarkTheme()
  val tabsStyle = getZTabsStyle(isDarkTheme)
  val isHorizontal = tabPosition == ZTabsPosition.TOP || tabPosition == ZTabsPosition.BOTTOM
  val showAddButton = addable || editable
  val canStretch = stretch && isHorizontal && !showAddButton

  var internalActiveName by remember(tabs, defaultActiveName) {
    mutableStateOf(resolveInitialActiveName(tabs, defaultActiveName))
  }
  val currentActiveName = activeName ?: internalActiveName
  val resolvedActiveName = resolveInitialActiveName(tabs, currentActiveName)

  LaunchedEffect(resolvedActiveName, activeName) {
    if (activeName == null && resolvedActiveName != internalActiveName) {
      internalActiveName = resolvedActiveName
    }
  }

  val visitedTabs = remember { mutableStateMapOf<String, Boolean>() }
  LaunchedEffect(resolvedActiveName) {
    if (resolvedActiveName != null) {
      visitedTabs[resolvedActiveName] = true
    }
  }

  fun requestActiveChange(targetName: String) {
    if (targetName == resolvedActiveName) return
    if (tabs.none { it.name == targetName && it.enabled }) return
    val old = resolvedActiveName
    if (old != null && beforeLeave != null && !beforeLeave(targetName, old)) return

    if (activeName == null) {
      internalActiveName = targetName
    }
    onActiveNameChange(targetName)
    onTabChange?.invoke(targetName)
  }

  fun removeTab(tab: ZTabPane) {
    onTabRemove?.invoke(tab.name)
    onEdit?.invoke(tab.name, ZTabsEditAction.REMOVE)

    if (tab.name != resolvedActiveName) return
    val nextActive = resolveNextActiveNameAfterRemove(tabs, tab.name)
    if (activeName == null) {
      internalActiveName = nextActive
    }
    if (nextActive != null) {
      onActiveNameChange(nextActive)
      onTabChange?.invoke(nextActive)
    }
  }

  val navContent: @Composable () -> Unit = {
    ZTabsNav(
      tabs = tabs,
      activeName = resolvedActiveName,
      type = type,
      tabPosition = tabPosition,
      tabsStyle = tabsStyle,
      closable = closable,
      editable = editable,
      showAddButton = showAddButton,
      stretch = canStretch,
      onTabClick = { tab ->
        onTabClick?.invoke(tab)
        requestActiveChange(tab.name)
      },
      onTabRemove = ::removeTab,
      onTabAdd = {
        onTabAdd?.invoke()
        onEdit?.invoke(null, ZTabsEditAction.ADD)
      }
    )
  }

  val panelContent: @Composable () -> Unit = {
    ZTabsPanel(
      tabs = tabs,
      activeName = resolvedActiveName,
      visitedTabs = visitedTabs,
      type = type,
      tabsStyle = tabsStyle,
      contentPadding = contentPadding
    )
  }

  val rootModifier = modifier.then(
    if (type == ZTabsType.BORDER_CARD) {
      Modifier
        .clip(ZTabsDefaults.BorderCardShape)
        .border(1.dp, tabsStyle.borderColor, ZTabsDefaults.BorderCardShape)
        .background(tabsStyle.contentBackground)
    } else {
      Modifier
    }
  )

  when (tabPosition) {
    ZTabsPosition.TOP -> {
      Column(modifier = rootModifier.fillMaxWidth()) {
        navContent()
        panelContent()
      }
    }

    ZTabsPosition.BOTTOM -> {
      Column(modifier = rootModifier.fillMaxWidth()) {
        panelContent()
        navContent()
      }
    }

    ZTabsPosition.LEFT -> {
      Row(modifier = rootModifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .width(ZTabsDefaults.VerticalNavWidth)
            .fillMaxHeight()
        ) {
          navContent()
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          panelContent()
        }
      }
    }

    ZTabsPosition.RIGHT -> {
      Row(modifier = rootModifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          panelContent()
        }
        Box(
          modifier = Modifier
            .width(ZTabsDefaults.VerticalNavWidth)
            .fillMaxHeight()
        ) {
          navContent()
        }
      }
    }
  }
}

@Composable
private fun ZTabsNav(
  tabs: List<ZTabPane>,
  activeName: String?,
  type: ZTabsType,
  tabPosition: ZTabsPosition,
  tabsStyle: ZTabsStyle,
  closable: Boolean,
  editable: Boolean,
  showAddButton: Boolean,
  stretch: Boolean,
  onTabClick: (ZTabPane) -> Unit,
  onTabRemove: (ZTabPane) -> Unit,
  onTabAdd: () -> Unit
) {
  val isHorizontal = tabPosition == ZTabsPosition.TOP || tabPosition == ZTabsPosition.BOTTOM
  val headerModifier = Modifier
    .fillMaxWidth()
    .then(
      when (type) {
        ZTabsType.BORDER_CARD -> Modifier.background(tabsStyle.headerBackground)
        else -> Modifier
      }
    )
    .zTabsSideBorder(
      side = navBorderSide(tabPosition),
      color = tabsStyle.borderColor,
      width = 1.dp
    )

  if (isHorizontal) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val canScroll by remember(stretch) {
      derivedStateOf { !stretch && scrollState.maxValue > 0 }
    }

    Row(
      modifier = headerModifier,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (canScroll) {
        ZTabsNavScrollArrow(
          isLeft = true,
          tabsStyle = tabsStyle,
          enabled = scrollState.value > 0,
          onClick = {
            coroutineScope.launch {
              val target = (scrollState.value - ZTabsDefaults.NavScrollStepPx.toInt()).coerceAtLeast(0)
              scrollState.animateScrollTo(target)
            }
          }
        )
      }

      Row(
        modifier = Modifier
          .weight(1f)
          .clipToBounds()
          .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
      ) {
        tabs.forEach { tab ->
          val isActive = tab.name == activeName
          val showClose = tab.enabled && (editable || closable || tab.closable)
          val itemModifier = if (stretch) Modifier.weight(1f) else Modifier

          ZTabsNavItem(
            modifier = itemModifier,
            tab = tab,
            selected = isActive,
            type = type,
            tabPosition = tabPosition,
            tabsStyle = tabsStyle,
            showClose = showClose,
            centerContent = stretch,
            fillWidth = stretch,
            onClick = { onTabClick(tab) },
            onClose = { onTabRemove(tab) }
          )
        }
      }

      if (canScroll) {
        ZTabsNavScrollArrow(
          isLeft = false,
          tabsStyle = tabsStyle,
          enabled = scrollState.value < scrollState.maxValue,
          onClick = {
            coroutineScope.launch {
              val target = (scrollState.value + ZTabsDefaults.NavScrollStepPx.toInt())
                .coerceAtMost(scrollState.maxValue)
              scrollState.animateScrollTo(target)
            }
          }
        )
      }

      if (showAddButton) {
        ZTabsAddButton(
          tabsStyle = tabsStyle,
          type = type,
          onClick = onTabAdd
        )
      }
    }
  } else {
    Column(
      modifier = headerModifier,
      verticalArrangement = Arrangement.Top
    ) {
      tabs.forEach { tab ->
        val isActive = tab.name == activeName
        val showClose = tab.enabled && (editable || closable || tab.closable)

        ZTabsNavItem(
          modifier = Modifier.fillMaxWidth(),
          tab = tab,
          selected = isActive,
          type = type,
          tabPosition = tabPosition,
          tabsStyle = tabsStyle,
          showClose = showClose,
          centerContent = false,
          fillWidth = true,
          onClick = { onTabClick(tab) },
          onClose = { onTabRemove(tab) }
        )
      }

      if (showAddButton) {
        ZTabsAddButton(
          tabsStyle = tabsStyle,
          type = type,
          modifier = Modifier.fillMaxWidth(),
          onClick = onTabAdd
        )
      }
    }
  }
}

@Composable
private fun ZTabsNavItem(
  tab: ZTabPane,
  selected: Boolean,
  type: ZTabsType,
  tabPosition: ZTabsPosition,
  tabsStyle: ZTabsStyle,
  showClose: Boolean,
  centerContent: Boolean,
  fillWidth: Boolean,
  onClick: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()

  val textColor = when {
    !tab.enabled -> tabsStyle.tabDisabledTextColor
    selected -> if (type == ZTabsType.LINE) tabsStyle.lineActiveTextColor else tabsStyle.tabActiveTextColor
    isHovered -> tabsStyle.tabHoverTextColor
    else -> tabsStyle.tabTextColor
  }

  val tabShape = resolveTabShape(type, tabPosition)
  val tabBackground = when (type) {
    ZTabsType.LINE -> Color.Transparent
    ZTabsType.CARD,
    ZTabsType.BORDER_CARD -> if (selected) tabsStyle.tabCardActiveBackground else tabsStyle.tabCardBackground
  }
  val tabBorderModifier = when (type) {
    ZTabsType.LINE -> Modifier
    ZTabsType.CARD,
    ZTabsType.BORDER_CARD -> Modifier
      .padding(horizontal = 2.dp, vertical = 2.dp)
      .border(1.dp, tabsStyle.borderColor, tabShape)
      .clip(tabShape)
      .background(tabBackground)
  }

  val clickableModifier = Modifier
    .hoverable(interactionSource = interactionSource)
    .clickable(
      enabled = tab.enabled,
      interactionSource = interactionSource,
      indication = null,
      onClick = onClick
    )

  Box(
    modifier = modifier
      .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
      .heightIn(min = ZTabsDefaults.TabMinHeight)
      .then(tabBorderModifier)
      .then(clickableModifier)
  ) {
    Row(
      modifier = Modifier
        .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
        .padding(ZTabsDefaults.TabContentPadding),
      horizontalArrangement = if (centerContent) Arrangement.Center else Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = tab.label,
        color = textColor,
        fontSize = 14.sp,
        softWrap = false,
        maxLines = 1
      )

      if (showClose) {
        Box(
          modifier = Modifier
            .padding(start = 8.dp)
            .size(14.dp)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              onClose()
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = FeatherIcons.X,
            contentDescription = "Close Tab",
            tint = textColor,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }

    if (type == ZTabsType.LINE && selected) {
      Box(
        modifier = Modifier
          .matchParentSize()
          .drawBehind {
            val strokeWidth = ZTabsDefaults.ActiveIndicatorThickness.toPx()
            when (tabPosition) {
              ZTabsPosition.TOP -> drawRect(
                color = tabsStyle.lineActiveBarColor,
                topLeft = Offset(0f, size.height - strokeWidth),
                size = Size(size.width, strokeWidth)
              )

              ZTabsPosition.BOTTOM -> drawRect(
                color = tabsStyle.lineActiveBarColor,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, strokeWidth)
              )

              ZTabsPosition.LEFT -> drawRect(
                color = tabsStyle.lineActiveBarColor,
                topLeft = Offset(size.width - strokeWidth, 0f),
                size = Size(strokeWidth, size.height)
              )

              ZTabsPosition.RIGHT -> drawRect(
                color = tabsStyle.lineActiveBarColor,
                topLeft = Offset(0f, 0f),
                size = Size(strokeWidth, size.height)
              )
            }
          }
      )
    }
  }
}

@Composable
private fun ZTabsNavScrollArrow(
  isLeft: Boolean,
  tabsStyle: ZTabsStyle,
  enabled: Boolean,
  onClick: () -> Unit
) {
  val iconColor = if (enabled) tabsStyle.tabTextColor else tabsStyle.tabDisabledTextColor
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = Modifier
      .width(ZTabsDefaults.NavArrowWidth)
      .heightIn(min = ZTabsDefaults.TabMinHeight)
      .clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = if (isLeft) FeatherIcons.ChevronLeft else FeatherIcons.ChevronRight,
      contentDescription = if (isLeft) "Scroll Left" else "Scroll Right",
      tint = iconColor,
      modifier = Modifier.size(14.dp)
    )
  }
}

@Composable
private fun ZTabsAddButton(
  tabsStyle: ZTabsStyle,
  type: ZTabsType,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val background = if (type == ZTabsType.BORDER_CARD) tabsStyle.headerBackground else Color.Transparent
  Box(
    modifier = modifier
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .size(ZTabsDefaults.AddButtonSize)
      .clip(ZTabsDefaults.AddButtonShape)
      .background(background)
      .border(1.dp, tabsStyle.addButtonBorderColor, ZTabsDefaults.AddButtonShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    ZText(
      text = "+",
      color = tabsStyle.addButtonTextColor,
      fontSize = 18.sp
    )
  }
}

@Composable
private fun ZTabsPanel(
  tabs: List<ZTabPane>,
  activeName: String?,
  visitedTabs: Map<String, Boolean>,
  type: ZTabsType,
  tabsStyle: ZTabsStyle,
  contentPadding: PaddingValues
) {
  val activeTab = tabs.firstOrNull { it.name == activeName }
  val canRender = activeTab != null && (
    !activeTab.lazy || visitedTabs[activeTab.name] == true || activeTab.name == activeName
    )

  val panelModifier = Modifier
    .fillMaxWidth()
    .then(
      if (type == ZTabsType.BORDER_CARD) {
        Modifier
          .background(tabsStyle.contentBackground)
          .padding(contentPadding)
      } else {
        Modifier.padding(contentPadding)
      }
    )

  Box(modifier = panelModifier) {
    if (canRender && activeTab != null) {
      activeTab.content()
    }
  }
}

private data class ZTabsStyle(
  val borderColor: Color,
  val headerBackground: Color,
  val contentBackground: Color,
  val tabTextColor: Color,
  val tabHoverTextColor: Color,
  val tabActiveTextColor: Color,
  val tabDisabledTextColor: Color,
  val lineActiveTextColor: Color,
  val lineActiveBarColor: Color,
  val tabCardBackground: Color,
  val tabCardActiveBackground: Color,
  val addButtonTextColor: Color,
  val addButtonBorderColor: Color
)

private fun getZTabsStyle(isDarkTheme: Boolean): ZTabsStyle {
  return if (isDarkTheme) {
    ZTabsStyle(
      borderColor = Color(0xff414243),
      headerBackground = Color(0xff1f2022),
      contentBackground = Color(0xff1d1e1f),
      tabTextColor = Color(0xffa3a6ad),
      tabHoverTextColor = Color(0xff79bbff),
      tabActiveTextColor = Color(0xffe5eaf3),
      tabDisabledTextColor = Color(0xff6b6d71),
      lineActiveTextColor = Color(0xff79bbff),
      lineActiveBarColor = Color(0xff409eff),
      tabCardBackground = Color(0xff1f2022),
      tabCardActiveBackground = Color(0xff1d1e1f),
      addButtonTextColor = Color(0xffcfd3dc),
      addButtonBorderColor = Color(0xff5a5d62)
    )
  } else {
    ZTabsStyle(
      borderColor = Color(0xffe4e7ed),
      headerBackground = Color(0xfff5f7fa),
      contentBackground = Color.White,
      tabTextColor = Color(0xff606266),
      tabHoverTextColor = Color(0xff409eff),
      tabActiveTextColor = Color(0xff303133),
      tabDisabledTextColor = Color(0xffc0c4cc),
      lineActiveTextColor = Color(0xff409eff),
      lineActiveBarColor = Color(0xff409eff),
      tabCardBackground = Color(0xfff5f7fa),
      tabCardActiveBackground = Color.White,
      addButtonTextColor = Color(0xff606266),
      addButtonBorderColor = Color(0xffdcdfe6)
    )
  }
}

private fun resolveTabShape(type: ZTabsType, tabPosition: ZTabsPosition): Shape {
  if (type == ZTabsType.LINE) return RoundedCornerShape(0.dp)
  return when (tabPosition) {
    ZTabsPosition.TOP -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
    ZTabsPosition.BOTTOM -> RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
    ZTabsPosition.LEFT,
    ZTabsPosition.RIGHT -> RoundedCornerShape(4.dp)
  }
}

private fun resolveInitialActiveName(tabs: List<ZTabPane>, preferred: String?): String? {
  if (tabs.isEmpty()) return null
  if (preferred != null && tabs.any { it.name == preferred }) return preferred
  return tabs.firstOrNull { it.enabled }?.name ?: tabs.first().name
}

private fun resolveNextActiveNameAfterRemove(tabs: List<ZTabPane>, removedName: String): String? {
  if (tabs.isEmpty()) return null
  val currentIndex = tabs.indexOfFirst { it.name == removedName }
  if (currentIndex == -1) return resolveInitialActiveName(tabs, null)

  val candidates = tabs.filter { it.name != removedName && it.enabled }
  if (candidates.isEmpty()) return null

  val next = tabs.drop(currentIndex + 1).firstOrNull { it.name != removedName && it.enabled }
  if (next != null) return next.name
  val previous = tabs.take(currentIndex).lastOrNull { it.name != removedName && it.enabled }
  return previous?.name
}

private enum class ZTabsBorderSide {
  TOP,
  RIGHT,
  BOTTOM,
  LEFT
}

private fun navBorderSide(tabPosition: ZTabsPosition): ZTabsBorderSide {
  return when (tabPosition) {
    ZTabsPosition.TOP -> ZTabsBorderSide.BOTTOM
    ZTabsPosition.RIGHT -> ZTabsBorderSide.LEFT
    ZTabsPosition.BOTTOM -> ZTabsBorderSide.TOP
    ZTabsPosition.LEFT -> ZTabsBorderSide.RIGHT
  }
}

private fun Modifier.zTabsSideBorder(
  side: ZTabsBorderSide,
  color: Color,
  width: Dp
): Modifier = this.then(
  Modifier.drawBehind {
    val stroke = width.toPx()
    when (side) {
      ZTabsBorderSide.TOP -> drawRect(
        color = color,
        topLeft = Offset(0f, 0f),
        size = Size(size.width, stroke)
      )

      ZTabsBorderSide.RIGHT -> drawRect(
        color = color,
        topLeft = Offset(size.width - stroke, 0f),
        size = Size(stroke, size.height)
      )

      ZTabsBorderSide.BOTTOM -> drawRect(
        color = color,
        topLeft = Offset(0f, size.height - stroke),
        size = Size(size.width, stroke)
      )

      ZTabsBorderSide.LEFT -> drawRect(
        color = color,
        topLeft = Offset(0f, 0f),
        size = Size(stroke, size.height)
      )
    }
  }
)

object ZTabsDefaults {
  val BorderCardShape = RoundedCornerShape(4.dp)
  val AddButtonShape = RoundedCornerShape(2.dp)

  val VerticalNavWidth = 160.dp
  val TabMinHeight = 40.dp
  val TabContentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
  val ContentPadding = PaddingValues(15.dp)
  val NavArrowWidth = 24.dp
  const val NavScrollStepPx = 120f
  val AddButtonSize = 28.dp
  val ActiveIndicatorThickness = 2.dp
}
