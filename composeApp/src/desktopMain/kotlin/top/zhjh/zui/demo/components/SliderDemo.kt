package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.*
import kotlin.math.roundToInt

@Composable
fun sliderDemoContent() {
  var defaultSliderValue by remember { mutableStateOf(0f) }
  var customizedSliderValue by remember { mutableStateOf(50f) }
  var discreteSliderValue by remember { mutableStateOf(20f) }
  var discreteStopsSliderValue by remember { mutableStateOf(20f) }
  var hideTooltipSliderValue by remember { mutableStateOf(30f) }
  var formattedTooltipSliderValue by remember { mutableStateOf(35f) }
  var inputSliderValue by remember { mutableStateOf(49f) }
  var largeSizeSliderValue by remember { mutableStateOf(59f) }
  var defaultSizeSliderValue by remember { mutableStateOf(59f) }
  var smallSizeSliderValue by remember { mutableStateOf(59f) }
  var topPlacementSliderValue by remember { mutableStateOf(0f) }
  var bottomPlacementSliderValue by remember { mutableStateOf(0f) }
  var rightPlacementSliderValue by remember { mutableStateOf(0f) }
  var leftPlacementSliderValue by remember { mutableStateOf(0f) }
  var rangeStopsSliderValue by remember { mutableStateOf(floatArrayOf(40f, 80f)) }
  var verticalSliderValue by remember { mutableStateOf(0f) }
  var verticalRangeSliderValue by remember { mutableStateOf(floatArrayOf(20f, 70f)) }
  var verticalMarksSliderValue by remember { mutableStateOf(30f) }
  var marksSliderValue by remember { mutableStateOf(floatArrayOf(30f, 60f)) }
  var capsuleSliderValue by remember { mutableStateOf(36f) }
  val disabledSliderValue = 40f
  val labelSliderSpacing = 10.dp
  var labelWidth = 200.dp
  val marks = remember {
    mapOf(
      0f to ZSliderMark("0°C"),
      8f to ZSliderMark("8°C"),
      37f to ZSliderMark("37°C"),
      50f to ZSliderMark(
        label = "50%",
        color = Color(0xff1989FA)
      )
    )
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 5.dp, bottom = 5.dp)
  ) {
    ZText(
      text = "基础用法",
      style= MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Default value",
        modifier = Modifier.width(labelWidth)
      )
      ZSlider(
        value = defaultSliderValue,
        onValueChange = { defaultSliderValue = it },
        valueRange = 0f..100f,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Customized initial value",
        modifier = Modifier.width(labelWidth),
        truncated = true
      )
      ZSlider(
        value = customizedSliderValue,
        onValueChange = { customizedSliderValue = it },
        valueRange = 0f..100f,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Hide Tooltip",
        modifier = Modifier.width(labelWidth)
      )
      ZSlider(
        value = hideTooltipSliderValue,
        onValueChange = { hideTooltipSliderValue = it },
        valueRange = 0f..100f,
        showTooltip = false,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Format Tooltip (0~1)",
        modifier = Modifier.width(labelWidth)
      )
      ZSlider(
        value = formattedTooltipSliderValue,
        onValueChange = { formattedTooltipSliderValue = it.roundToInt().toFloat() },
        valueRange = 0f..100f,
        formatTooltip = { String.format("%.2f", it / 100f) },
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Disabled",
        modifier = Modifier.width(labelWidth)
      )
      ZSlider(
        value = disabledSliderValue,
        onValueChange = {},
        valueRange = 0f..100f,
        enabled = false,
        modifier = Modifier.weight(1f)
      )
    }

    ZText(
      text = "离散值",
      style= MaterialTheme.typography.h3
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Breakpoints not displayed",
        modifier = Modifier.width(labelWidth),
        truncated = true
      )
      ZSlider(
        value = discreteSliderValue,
        onValueChange = { discreteSliderValue = it },
        valueRange = 0f..100f,
        step = 10f,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Breakpoints displayed",
        modifier = Modifier.width(labelWidth),
        truncated = true
      )
      ZSlider(
        value = discreteStopsSliderValue,
        onValueChange = { discreteStopsSliderValue = it },
        valueRange = 0f..100f,
        step = 10f,
        showStops = true,
        modifier = Modifier.weight(1f)
      )
    }

    labelWidth = 100.dp

    ZText(
      text = "带有输入框的滑块",
      style= MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZText(
        text = "Show Input",
        modifier = Modifier.width(labelWidth)
      )
      ZSlider(
        value = inputSliderValue,
        onValueChange = { inputSliderValue = it.roundToInt().toFloat() },
        valueRange = 0f..100f,
        showInput = true,
        modifier = Modifier.weight(1f)
      )
    }

    ZText(
      text = "不同尺寸",
      style= MaterialTheme.typography.h3
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ZText(
          text = "Large",
          modifier = Modifier.width(labelWidth)
        )
        ZSlider(
          value = largeSizeSliderValue,
          onValueChange = { largeSizeSliderValue = it.roundToInt().toFloat() },
          valueRange = 0f..100f,
          showInput = true,
          size = ZFormSize.LARGE,
          modifier = Modifier.weight(1f)
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ZText(
          text = "Default",
          modifier = Modifier.width(labelWidth)
        )
        ZSlider(
          value = defaultSizeSliderValue,
          onValueChange = { defaultSizeSliderValue = it.roundToInt().toFloat() },
          valueRange = 0f..100f,
          showInput = true,
          size = ZFormSize.DEFAULT,
          modifier = Modifier.weight(1f)
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ZText(
          text = "Small",
          modifier = Modifier.width(labelWidth)
        )
        ZSlider(
          value = smallSizeSliderValue,
          onValueChange = { smallSizeSliderValue = it.roundToInt().toFloat() },
          valueRange = 0f..100f,
          showInput = true,
          size = ZFormSize.SMALL,
          modifier = Modifier.weight(1f)
        )
      }
    }

    ZText(
      text = "位置",
      style= MaterialTheme.typography.h3
    )
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ZSlider(
        value = topPlacementSliderValue,
        onValueChange = { topPlacementSliderValue = it.roundToInt().toFloat() },
        valueRange = 0f..100f,
        modifier = Modifier.fillMaxWidth()
      )
      ZSlider(
        value = bottomPlacementSliderValue,
        onValueChange = { bottomPlacementSliderValue = it.roundToInt().toFloat() },
        valueRange = 0f..100f,
        placement = ZSliderTooltipPlacement.BOTTOM,
        modifier = Modifier.fillMaxWidth()
      )
      ZSlider(
        value = rightPlacementSliderValue,
        onValueChange = { rightPlacementSliderValue = it.roundToInt().toFloat() },
        valueRange = 0f..100f,
        placement = ZSliderTooltipPlacement.RIGHT,
        modifier = Modifier.fillMaxWidth()
      )
      ZSlider(
        value = leftPlacementSliderValue,
        onValueChange = { leftPlacementSliderValue = it.roundToInt().toFloat() },
        valueRange = 0f..100f,
        placement = ZSliderTooltipPlacement.LEFT,
        modifier = Modifier.fillMaxWidth()
      )
    }

    ZText(
      text = "范围选择",
      style= MaterialTheme.typography.h3
    )
    ZSlider(
      value = rangeStopsSliderValue,
      onValueChange = { rangeStopsSliderValue = it },
      valueRange = 0f..100f,
      range = true,
      step = 10f,
      showStops = true,
      modifier = Modifier.fillMaxWidth()
    )

    ZText(
      text = "垂直模式",
      style= MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp),
      horizontalArrangement = Arrangement.spacedBy(48.dp),
      verticalAlignment = Alignment.Top
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        ZText(text = "Basic")
        ZSlider(
          value = verticalSliderValue,
          onValueChange = { verticalSliderValue = it.roundToInt().toFloat() },
          valueRange = 0f..100f,
          vertical = true,
          modifier = Modifier.height(200.dp)
        )
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        ZText(text = "Range")
        ZSlider(
          value = verticalRangeSliderValue,
          onValueChange = { verticalRangeSliderValue = it },
          valueRange = 0f..100f,
          range = true,
          vertical = true,
          step = 10f,
          showStops = true,
          modifier = Modifier.height(200.dp)
        )
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        ZText(text = "Marks")
        ZSlider(
          value = verticalMarksSliderValue,
          onValueChange = { verticalMarksSliderValue = it.roundToInt().toFloat() },
          valueRange = 0f..100f,
          vertical = true,
          marks = marks,
          modifier = Modifier.height(200.dp)
        )
      }
    }

    ZText(
      text = "显示标记",
      style= MaterialTheme.typography.h3
    )
    ZSlider(
      value = marksSliderValue,
      onValueChange = { marksSliderValue = it },
      valueRange = 0f..100f,
      range = true,
      marks = marks,
      modifier = Modifier.fillMaxWidth()
    )

    ZText(
      text = "胶囊滑块",
      style= MaterialTheme.typography.h3
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(labelSliderSpacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZSlider(
        value = capsuleSliderValue,
        onValueChange = { capsuleSliderValue = it },
        valueRange = 0f..100f,
        thumbMode = ZSliderThumbMode.CAPSULE,
        modifier = Modifier.weight(1f)
      )
    }
  }
}
