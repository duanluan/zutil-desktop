package top.zhjh.zui.demo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZButtonSize
import top.zhjh.zui.composable.ZPopconfirm
import top.zhjh.zui.composable.ZText
import top.zhjh.zui.enums.ZColorType

@Composable
fun popconfirmDemoContent() {
  val topPlacements = listOf("top-start", "top", "top-end")
  val leftPlacements = listOf("left-start", "left", "left-end")
  val rightPlacements = listOf("right-start", "right", "right-end")
  val bottomPlacements = listOf("bottom-start", "bottom", "bottom-end")

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 5.dp, bottom = 5.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    ZText(
      text = "展示位置",
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
          PopconfirmPlacementButton(placement = placement)
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
            PopconfirmPlacementButton(placement = placement)
          }
        }

        Box(modifier = Modifier.weight(1f))

        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.End
        ) {
          rightPlacements.forEach { placement ->
            PopconfirmPlacementButton(placement = placement)
          }
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        bottomPlacements.forEach { placement ->
          PopconfirmPlacementButton(placement = placement)
        }
      }
    }

    ZText(
      text = "自定义图标和操作区",
      style = MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center
    ) {
      PopconfirmCustomActionDemo()
    }
  }
}

@Composable
private fun PopconfirmPlacementButton(placement: String) {
  ZPopconfirm(
    title = placementToTitle(placement),
    placement = placement
  ) { showPopconfirm ->
    ZButton(
      plain = true,
      onClick = showPopconfirm,
      modifier = Modifier.widthIn(min = 96.dp)
    ) {
      ZText(placement)
    }
  }
}

@Composable
private fun PopconfirmCustomActionDemo() {
  var clicked by remember { mutableStateOf(false) }

  ZPopconfirm(
    title = "Are you sure to delete this?",
    placement = "bottom",
    width = 220.dp,
    icon = { iconColor ->
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(color = iconColor),
        contentAlignment = Alignment.Center
      ) {
        ZText(
          text = "i",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          lineHeight = 12.sp
        )
      }
    },
    iconColor = Color(0xff626AEF),
    onCancel = { clicked = true },
    actions = { confirm, cancel ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          ZButton(
            size = ZButtonSize.Small,
            onClick = cancel
          ) {
            ZText("No!")
          }
          ZButton(
            size = ZButtonSize.Small,
            type = ZColorType.DANGER,
            enabled = clicked,
            onClick = confirm
          ) {
            ZText("Yes?")
          }
        }
      }
    }
  ) { showPopconfirm ->
    ZButton(
      plain = true,
      onClick = showPopconfirm
    ) {
      ZText("Delete")
    }
  }
}

private fun placementToTitle(placement: String): String {
  val direction = placement
    .substringBefore("-")
    .replaceFirstChar { it.uppercase() }
  val align = when (placement.substringAfter("-", missingDelimiterValue = "center")) {
    "start" -> "Start"
    "end" -> "End"
    else -> "Center"
  }
  return "$direction $align prompts info"
}
