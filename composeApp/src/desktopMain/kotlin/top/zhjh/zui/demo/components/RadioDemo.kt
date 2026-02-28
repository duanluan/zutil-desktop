package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZRadio
import top.zhjh.zui.composable.ZRadioSize
import top.zhjh.zui.composable.ZText

@Composable
fun radioDemoContent() {
  var radioStringValue by remember { mutableStateOf("option1") }
  var radioNumberValue by remember { mutableStateOf(1) }
  var radioBooleanValue by remember { mutableStateOf(true) }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ZText("Radio String")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZRadio(
        value = "option1",
        selectedValue = radioStringValue,
        onValueChange = { radioStringValue = it },
        label = "Option 1"
      )
      ZRadio(
        value = "option2",
        selectedValue = radioStringValue,
        onValueChange = { radioStringValue = it },
        label = "Option 2"
      )
      ZRadio(
        value = "option3",
        selectedValue = radioStringValue,
        onValueChange = { radioStringValue = it },
        label = "Option 3"
      )
    }

    ZText("Radio Number")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZRadio(
        value = 1,
        selectedValue = radioNumberValue,
        onValueChange = { radioNumberValue = it },
        label = "1"
      )
      ZRadio(
        value = 2,
        selectedValue = radioNumberValue,
        onValueChange = { radioNumberValue = it },
        label = "2"
      )
      ZRadio(
        value = 3,
        selectedValue = radioNumberValue,
        onValueChange = { radioNumberValue = it },
        label = "3"
      )
    }

    ZText("Radio Boolean")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZRadio(
        value = true,
        selectedValue = radioBooleanValue,
        onValueChange = { radioBooleanValue = it },
        label = "True"
      )
      ZRadio(
        value = false,
        selectedValue = radioBooleanValue,
        onValueChange = { radioBooleanValue = it },
        label = "False"
      )
    }

    ZText("Radio Size")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZRadio(
        value = "large",
        selectedValue = "large",
        onValueChange = {},
        label = "Large",
        size = ZRadioSize.Large
      )
      ZRadio(
        value = "default",
        selectedValue = "default",
        onValueChange = {},
        label = "Default",
        size = ZRadioSize.Default
      )
      ZRadio(
        value = "small",
        selectedValue = "small",
        onValueChange = {},
        label = "Small",
        size = ZRadioSize.Small
      )
    }
  }
}
