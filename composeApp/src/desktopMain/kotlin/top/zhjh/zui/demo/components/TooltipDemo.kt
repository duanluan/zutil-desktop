package top.zhjh.zui.demo.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZText
import top.zhjh.zui.composable.ZTooltip
import top.zhjh.zui.composable.ZTooltipDefaults
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

@Composable
fun tooltipDemoContent() {
  var tooltipEnabled by remember { mutableStateOf(true) }
  var controlledVisible by remember { mutableStateOf(false) }
  var virtualVisible by remember { mutableStateOf(false) }
  var virtualAnchorBounds by remember { mutableStateOf<IntRect?>(null) }
  var singletonVisible by remember { mutableStateOf(false) }
  var singletonActiveIndex by remember { mutableStateOf<Int?>(null) }
  val controlledInteractionSource = remember { MutableInteractionSource() }
  val controlledHovered by controlledInteractionSource.collectIsHoveredAsState()
  val topPlacements = listOf(ZTooltipDefaults.TopStart, ZTooltipDefaults.Top, ZTooltipDefaults.TopEnd)
  val leftPlacements = listOf(ZTooltipDefaults.LeftStart, ZTooltipDefaults.Left, ZTooltipDefaults.LeftEnd)
  val rightPlacements = listOf(ZTooltipDefaults.RightStart, ZTooltipDefaults.Right, ZTooltipDefaults.RightEnd)
  val bottomPlacements = listOf(ZTooltipDefaults.BottomStart, ZTooltipDefaults.Bottom, ZTooltipDefaults.BottomEnd)

  LaunchedEffect(controlledHovered) {
    controlledVisible = controlledHovered
  }

  DisposableEffect(Unit) {
    val listener = AWTEventListener { event ->
      val mouseEvent = event as? MouseEvent ?: return@AWTEventListener
      if (mouseEvent.id != MouseEvent.MOUSE_MOVED && mouseEvent.id != MouseEvent.MOUSE_DRAGGED) {
        return@AWTEventListener
      }
      val component = mouseEvent.component ?: return@AWTEventListener
      val window = SwingUtilities.getWindowAncestor(component) ?: return@AWTEventListener
      val windowLocation = runCatching { window.locationOnScreen }.getOrNull() ?: return@AWTEventListener
      val x = mouseEvent.xOnScreen - windowLocation.x
      val y = mouseEvent.yOnScreen - windowLocation.y
      virtualAnchorBounds = IntRect(
        left = x,
        top = y,
        right = x + 1,
        bottom = y + 1
      )
    }
    Toolkit.getDefaultToolkit().addAWTEventListener(
      listener,
      AWTEvent.MOUSE_MOTION_EVENT_MASK
    )
    onDispose {
      Toolkit.getDefaultToolkit().removeAWTEventListener(listener)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 5.dp, bottom = 5.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    ZText(
      text = "基础用法",
      style = MaterialTheme.typography.h3
    )

    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        topPlacements.forEach { placement ->
          TooltipPlacementButton(placement = placement)
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 1200.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.Start
        ) {
          leftPlacements.forEach { placement ->
            TooltipPlacementButton(placement = placement)
          }
        }

        Box(modifier = Modifier.weight(1f))

        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.End
        ) {
          rightPlacements.forEach { placement ->
            TooltipPlacementButton(placement = placement)
          }
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        bottomPlacements.forEach { placement ->
          TooltipPlacementButton(placement = placement)
        }
      }
    }

    ZText(
      text = "主题",
      style = MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZTooltip(
        content = "Top center",
        placement = ZTooltipDefaults.Top
      ) {
        ZButton(onClick = {}) {
          ZText("Dark")
        }
      }

      ZTooltip(
        content = "Bottom center",
        placement = ZTooltipDefaults.Bottom,
        effect = ZTooltipDefaults.EffectLight
      ) {
        ZButton(onClick = {}) {
          ZText("Light")
        }
      }

      ZTooltip(
        content = "Bottom center",
        placement = ZTooltipDefaults.Bottom,
        backgroundBrush = Brush.linearGradient(
          colors = listOf(
            Color(0xff9FE597),
            Color(0xffCCE581)
          )
        ),
        textColor = Color(0xff303133),
        borderColor = Color(0xffb2e68d)
      ) {
        ZButton(onClick = {}) {
          ZText("Customized theme")
        }
      }
    }

    ZText(
      text = "更多内容的文字提示",
      style = MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZTooltip(
        content = "第一行：支持多行文本\n第二行：可以直接换行\n第三行：适合较长说明",
        placement = ZTooltipDefaults.Top
      ) {
        ZButton(onClick = {}) {
          ZText("多行文本")
        }
      }

      ZTooltip(
        placement = ZTooltipDefaults.Top,
        tooltipContent = { tooltipTextColor ->
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ZText(
              text = "删除提示",
              color = tooltipTextColor,
              fontWeight = FontWeight.Bold
            )
            ZText(
              text = "删除后不可恢复，请谨慎操作。",
              color = tooltipTextColor
            )
            ZText(
              text = "建议先备份数据",
              color = Color(0xfff56c6c),
              fontWeight = FontWeight.Medium
            )
          }
        }
      ) {
        ZButton(onClick = {}) {
          ZText("格式化内容")
        }
      }
    }

    ZText(
      text = "显示控制",
      style = MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZTooltip(
        content = "click to close tooltip function",
        placement = ZTooltipDefaults.Bottom,
        effect = ZTooltipDefaults.EffectLight,
        enabled = tooltipEnabled
      ) {
        ZButton(onClick = { tooltipEnabled = !tooltipEnabled }) {
          ZText("click to ${if (tooltipEnabled) "close" else "active"} tooltip function")
        }
      }
    }

    ZText(
      text = "虚拟触发",
      style = MaterialTheme.typography.h3
    )
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZTooltip(
        content = "Bottom center",
        placement = ZTooltipDefaults.Bottom,
        effect = ZTooltipDefaults.EffectLight,
        visible = virtualVisible,
        virtualTriggering = true,
        virtualAnchorBounds = virtualAnchorBounds,
        onVisibleChange = { virtualVisible = it }
      ) {
        ZButton(onClick = { virtualVisible = !virtualVisible }) {
          ZText("click to ${if (virtualVisible) "close" else "active"} tooltip function")
        }
      }
    }

    ZText(
      text = "单例模式",
      style = MaterialTheme.typography.h3
    )
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        (1..3).forEach { index ->
          ZTooltip(
            placement = ZTooltipDefaults.Bottom,
            effect = ZTooltipDefaults.EffectLight,
            visible = singletonVisible && singletonActiveIndex == index,
            onVisibleChange = { nextVisible ->
              if (!nextVisible && singletonActiveIndex == index) {
                singletonVisible = false
              }
            },
            tooltipContent = {
              ZText("Some content")
            }
          ) {
            ZButton(
              onClick = {
                val clickCurrent = singletonVisible && singletonActiveIndex == index
                if (clickCurrent) {
                  singletonVisible = false
                } else {
                  singletonActiveIndex = index
                  singletonVisible = true
                }
              }
            ) {
              ZText("Click to open tooltip")
            }
          }
        }
      }
    }

    ZText(
      text = "受控模式",
      style = MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZTooltip(
        placement = ZTooltipDefaults.Bottom,
        visible = controlledVisible,
        onVisibleChange = { controlledVisible = it },
        tooltipContent = { tooltipTextColor ->
          ZText(
            text = "Content",
            color = tooltipTextColor
          )
        }
      ) {
        ZButton(
          modifier = Modifier.hoverable(controlledInteractionSource),
          onClick = {}
        ) {
          ZText("Hover me")
        }
      }
    }

    ZText(
      text = "自定义动画",
      style = MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZTooltip(
        content = "I am an el-tooltip",
        placement = ZTooltipDefaults.Top,
        transition = ZTooltipDefaults.TransitionSlideFade
      ) {
        ZButton(onClick = {}) {
          ZText("trigger me")
        }
      }
    }
  }
}

@Composable
private fun TooltipPlacementButton(placement: String) {
  ZTooltip(
    content = placementToTooltipContent(placement),
    placement = placement
  ) {
    ZButton(
      plain = true,
      onClick = {},
      modifier = Modifier.widthIn(min = 96.dp)
    ) {
      ZText(placement)
    }
  }
}

private fun placementToTooltipContent(placement: String): String {
  val direction = placement
    .substringBefore("-")
    .replaceFirstChar { it.uppercase() }
  val align = when (placement.substringAfter("-", missingDelimiterValue = "center")) {
    "start" -> "Start"
    "end" -> "End"
    else -> "Center"
  }
  return "$direction $align tooltip"
}
