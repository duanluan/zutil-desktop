package top.zhjh.zui.demo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.FileText
import compose.icons.feathericons.Grid
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Settings
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZColorType

@Composable
fun menuDemoContent(isDarkTheme: Boolean) {
  var menuTopActiveIndex by remember { mutableStateOf("1") }
  var menuTopDarkActiveIndex by remember { mutableStateOf("1") }
  var menuRightActiveIndex by remember { mutableStateOf("1") }
  var menuVerticalActiveIndex by remember { mutableStateOf("2") }
  var menuCollapseActiveIndex by remember { mutableStateOf("1-1") }
  var menuCustomColorActiveIndex by remember { mutableStateOf("2") }
  var menuOffsetActiveIndex by remember { mutableStateOf("3") }
  var menuCollapsed by remember { mutableStateOf(true) }

  val menuTopItems = remember {
    listOf<ZMenuNode>(
      ZMenuGroup(
        children = listOf(
          ZMenuItem(index = "1", title = "Processing Center"),
          ZSubMenu(
            index = "2",
            title = "Workspace",
            children = listOf(
              ZMenuItem(index = "2-1", title = "item one"),
              ZMenuItem(index = "2-2", title = "item two"),
              ZMenuItem(index = "2-3", title = "item three"),
              ZSubMenu(
                index = "2-4",
                title = "item four",
                children = listOf(
                  ZMenuItem(index = "2-4-1", title = "item one"),
                  ZMenuItem(index = "2-4-2", title = "item two"),
                  ZMenuItem(index = "2-4-3", title = "item three")
                )
              )
            )
          )
        )
      ),
      ZMenuGroup(
        children = listOf(
          ZMenuItem(index = "3", title = "Info", enabled = false),
          ZMenuItem(index = "4", title = "Orders")
        )
      )
    )
  }
  val menuRightItems = remember {
    listOf<ZMenuNode>(
      ZMenuItem(index = "1", title = "Processing Center"),
      ZSubMenu(
        index = "2",
        title = "Workspace",
        children = listOf(
          ZMenuItem(index = "2-1", title = "item one"),
          ZMenuItem(index = "2-2", title = "item two"),
          ZMenuItem(index = "2-3", title = "item three"),
          ZSubMenu(
            index = "2-4",
            title = "item four",
            children = listOf(
              ZMenuItem(index = "2-4-1", title = "item one"),
              ZMenuItem(index = "2-4-2", title = "item two"),
              ZMenuItem(index = "2-4-3", title = "item three")
            )
          )
        )
      )
    )
  }
  val menuOffsetItems = remember {
    listOf<ZMenuNode>(
      ZMenuItem(index = "1", title = "Processing Center"),
      ZSubMenu(
        index = "2",
        title = "Workspace",
        children = listOf(
          ZMenuItem(index = "2-1", title = "item one"),
          ZMenuItem(index = "2-2", title = "item two"),
          ZMenuItem(index = "2-3", title = "item three")
        )
      ),
      ZSubMenu(
        index = "3",
        title = "Override Popper Offset",
        children = listOf(
          ZMenuItem(index = "3-1", title = "item one"),
          ZMenuItem(index = "3-2", title = "item two"),
          ZMenuItem(index = "3-3", title = "item three")
        )
      ),
      ZMenuItem(index = "4", title = "Orders")
    )
  }
  val menuVerticalItems = remember(isDarkTheme) {
    listOf<ZMenuNode>(
      ZSubMenu(
        index = "1",
        title = "Navigator One",
        icon = { Icon(FeatherIcons.MapPin, contentDescription = null) },
        children = listOf(
          ZMenuGroup(
            title = "Group One",
            children = listOf(
              ZMenuItem(index = "1-1", title = "item one"),
              ZMenuItem(index = "1-2", title = "item two")
            )
          ),
          ZMenuGroup(
            titleContent = {
              Text(
                text = "Group Two",
                style = MaterialTheme.typography.caption,
                color = if (isDarkTheme) Color(0xff8d9095) else Color(0xff909399)
              )
            },
            children = listOf(
              ZMenuItem(index = "1-3", title = "item three")
            )
          ),
          ZSubMenu(
            index = "1-4",
            title = "item four",
            children = listOf(
              ZMenuItem(index = "1-4-1", title = "item one")
            )
          )
        )
      ),
      ZMenuItem(
        index = "2",
        title = "Navigator Two",
        icon = { Icon(FeatherIcons.Grid, contentDescription = null) }
      ),
      ZMenuItem(
        index = "3",
        title = "Navigator Three",
        enabled = false,
        icon = { Icon(FeatherIcons.FileText, contentDescription = null) }
      ),
      ZMenuItem(
        index = "4",
        title = "Navigator Four",
        icon = { Icon(FeatherIcons.Settings, contentDescription = null) }
      )
    )
  }

  val panelBackgroundColor = if (isDarkTheme) Color(0xff0b0f15) else Color.White
  val panelBorderColor = if (isDarkTheme) Color(0xff4c4d4f) else Color(0xffdcdfe6)
  val customMenuBackground = Color(0xff545c64)

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
  ) {
    ZText("top bar menu")
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(panelBackgroundColor)
    ) {
      ZMenu(
        items = menuTopItems,
        mode = ZMenuMode.Horizontal,
        activeIndex = menuTopActiveIndex,
        onSelect = { menuTopActiveIndex = it },
        modifier = Modifier.fillMaxWidth()
      )
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(customMenuBackground)
    ) {
      ZMenu(
        items = menuTopItems,
        mode = ZMenuMode.Horizontal,
        activeIndex = menuTopDarkActiveIndex,
        backgroundColor = customMenuBackground,
        textColor = Color(0xfff4f4f5),
        activeTextColor = Color(0xffffd04b),
        onSelect = { menuTopDarkActiveIndex = it },
        modifier = Modifier.fillMaxWidth()
      )
    }

    ZText("menu item at right")
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(panelBackgroundColor)
        .drawWithContent {
          drawContent()
          val strokeWidth = 1.dp.toPx()
          val y = size.height - strokeWidth
          drawLine(
            color = panelBorderColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
          )
        }
        .padding(horizontal = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ZUI",
          color = if (isDarkTheme) Color(0xff79bbff) else Color(0xff409eff),
          style = MaterialTheme.typography.h6
        )
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterEnd
        ) {
          ZMenu(
            items = menuRightItems,
            mode = ZMenuMode.Horizontal,
            activeIndex = menuRightActiveIndex,
            horizontalFillMaxWidth = false,
            onSelect = { menuRightActiveIndex = it }
          )
        }
      }
    }

    ZText("vertical menu")
    Row(
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(260.dp)
      ) {
        Text(
          text = "Default colors",
          color = if (isDarkTheme) Color(0xffe5eaf3) else Color(0xff606266)
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(panelBackgroundColor)
        ) {
          ZMenu(
            items = menuVerticalItems,
            activeIndex = menuVerticalActiveIndex,
            defaultOpeneds = listOf("1"),
            onSelect = { menuVerticalActiveIndex = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(260.dp)
      ) {
        Text(
          text = "Custom colors",
          color = if (isDarkTheme) Color(0xffe5eaf3) else Color(0xff606266)
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(customMenuBackground)
        ) {
          ZMenu(
            items = menuVerticalItems,
            activeIndex = menuCustomColorActiveIndex,
            defaultOpeneds = listOf("1", "1-4"),
            backgroundColor = customMenuBackground,
            textColor = Color(0xffe5eaf3),
            activeTextColor = Color(0xffffd04b),
            onSelect = { menuCustomColorActiveIndex = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    ZText("vertical collapse")
    ZButtonGroup(itemSpacing = 0.dp) {
      ZButton(
        type = if (!menuCollapsed) ZColorType.PRIMARY else ZColorType.DEFAULT,
        onClick = { menuCollapsed = false }
      ) {
        Text("expand")
      }
      ZButton(
        type = if (menuCollapsed) ZColorType.PRIMARY else ZColorType.DEFAULT,
        onClick = { menuCollapsed = true }
      ) {
        Text("collapse")
      }
    }
    Box(
      modifier = Modifier
        .background(panelBackgroundColor)
    ) {
      ZMenu(
        items = menuVerticalItems,
        activeIndex = menuCollapseActiveIndex,
        defaultOpeneds = listOf("1"),
        collapse = menuCollapsed,
        onSelect = { menuCollapseActiveIndex = it },
        modifier = Modifier.width(if (menuCollapsed) 64.dp else 260.dp)
      )
    }

    ZText("popper offset")
    Column(
      verticalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "custom: 16.dp",
        color = if (isDarkTheme) Color(0xffa3a6ad) else Color(0xff606266)
      )
      Box(
        modifier = Modifier
          .width(550.dp)
          .background(panelBackgroundColor)
      ) {
        ZMenu(
          items = menuOffsetItems,
          mode = ZMenuMode.Horizontal,
          activeIndex = menuOffsetActiveIndex,
          popperOffset = 16.dp,
          onSelect = { menuOffsetActiveIndex = it },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
