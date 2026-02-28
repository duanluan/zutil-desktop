package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZCheckbox
import top.zhjh.zui.composable.ZCheckboxSize
import top.zhjh.zui.composable.ZText

@Composable
fun checkboxDemoContent() {
  var checkboxLargeSelectedOptions by remember { mutableStateOf(setOf("Option 1")) }
  var checkboxDefaultSelectedOptions by remember { mutableStateOf(emptySet<String>()) }
  var checkboxSmallSelectedOptions by remember { mutableStateOf(emptySet<String>()) }

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
      ZCheckbox(
        checked = "Option 1" in checkboxLargeSelectedOptions,
        onCheckedChange = { checked ->
          checkboxLargeSelectedOptions = if (checked) {
            checkboxLargeSelectedOptions + "Option 1"
          } else {
            checkboxLargeSelectedOptions - "Option 1"
          }
        },
        label = "Option 1",
        size = ZCheckboxSize.Large
      )
      ZCheckbox(
        checked = "Option 2" in checkboxLargeSelectedOptions,
        onCheckedChange = { checked ->
          checkboxLargeSelectedOptions = if (checked) {
            checkboxLargeSelectedOptions + "Option 2"
          } else {
            checkboxLargeSelectedOptions - "Option 2"
          }
        },
        label = "Option 2",
        size = ZCheckboxSize.Large
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
      ZCheckbox(
        checked = "Option 1" in checkboxDefaultSelectedOptions,
        onCheckedChange = { checked ->
          checkboxDefaultSelectedOptions = if (checked) {
            checkboxDefaultSelectedOptions + "Option 1"
          } else {
            checkboxDefaultSelectedOptions - "Option 1"
          }
        },
        label = "Option 1",
        size = ZCheckboxSize.Default
      )
      ZCheckbox(
        checked = "Option 2" in checkboxDefaultSelectedOptions,
        onCheckedChange = { checked ->
          checkboxDefaultSelectedOptions = if (checked) {
            checkboxDefaultSelectedOptions + "Option 2"
          } else {
            checkboxDefaultSelectedOptions - "Option 2"
          }
        },
        label = "Option 2",
        size = ZCheckboxSize.Default
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
      ZCheckbox(
        checked = "Option 1" in checkboxSmallSelectedOptions,
        onCheckedChange = { checked ->
          checkboxSmallSelectedOptions = if (checked) {
            checkboxSmallSelectedOptions + "Option 1"
          } else {
            checkboxSmallSelectedOptions - "Option 1"
          }
        },
        label = "Option 1",
        size = ZCheckboxSize.Small
      )
      ZCheckbox(
        checked = "Option 2" in checkboxSmallSelectedOptions,
        onCheckedChange = { checked ->
          checkboxSmallSelectedOptions = if (checked) {
            checkboxSmallSelectedOptions + "Option 2"
          } else {
            checkboxSmallSelectedOptions - "Option 2"
          }
        },
        label = "Option 2",
        size = ZCheckboxSize.Small
      )
    }
  }
}
