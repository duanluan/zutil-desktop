package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZSwitch
import top.zhjh.zui.composable.ZText

@Composable
fun switchDemoContent() {
  var switchDefaultValue by remember { mutableStateOf(true) }
  var switchCustomColorValue by remember { mutableStateOf(true) }
  var switchCustomBackgroundValue by remember { mutableStateOf(false) }

  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
  ) {
    ZText("basic")
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ZSwitch(
        checked = switchDefaultValue,
        onCheckedChange = { switchDefaultValue = it }
      )
      ZSwitch(
        checked = switchCustomColorValue,
        onCheckedChange = { switchCustomColorValue = it },
        activeColor = Color(0xff13ce66)
      )
    }

    ZText("custom background color")
    ZSwitch(
      checked = switchCustomBackgroundValue,
      onCheckedChange = { switchCustomBackgroundValue = it },
      activeColor = Color(0xff13ce66),
      inactiveColor = Color(0xffff4949)
    )
  }
}
