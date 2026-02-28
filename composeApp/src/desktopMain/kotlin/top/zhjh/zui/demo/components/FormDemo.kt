package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZColorType

@Composable
fun formDemoContent() {
  val activityZoneOptions = remember {
    listOf("Zone one", "Zone two", "Zone three", "Zone four")
  }
  val activityTypeOptions = remember {
    listOf(
      "Online activities",
      "Promotion activities",
      "Offline activities",
      "Simple brand exposure"
    )
  }

  var activityName by remember { mutableStateOf("") }
  var activityZone by remember { mutableStateOf("") }
  var instantDelivery by remember { mutableStateOf(false) }
  var selectedActivityTypes by remember { mutableStateOf(emptySet<String>()) }
  var selectedResource by remember { mutableStateOf<String?>(null) }
  var activityForm by remember { mutableStateOf("") }

  fun resetForm() {
    activityName = ""
    activityZone = ""
    instantDelivery = false
    selectedActivityTypes = emptySet()
    selectedResource = null
    activityForm = ""
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 780.dp)
  ) {
    ZForm(
      labelWidth = 130.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      ZFormItem(label = "Activity name") {
        ZTextField(
          value = activityName,
          onValueChange = { activityName = it },
          modifier = Modifier.fillMaxWidth()
        )
      }
      ZFormItem(label = "Activity zone") {
        ZDropdownMenu(
          options = activityZoneOptions,
          value = activityZone,
          onOptionSelected = { activityZone = it.orEmpty() },
          placeholder = "please select your zone",
          clearable = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
      ZFormItem(label = "Instant delivery") {
        ZSwitch(
          checked = instantDelivery,
          onCheckedChange = { instantDelivery = it }
        )
      }
      ZFormItem(label = "Activity type") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            activityTypeOptions.take(2).forEach { option ->
              Box(modifier = Modifier.width(210.dp)) {
                ZCheckbox(
                  checked = option in selectedActivityTypes,
                  onCheckedChange = { checked ->
                    selectedActivityTypes = if (checked) {
                      selectedActivityTypes + option
                    } else {
                      selectedActivityTypes - option
                    }
                  },
                  label = option
                )
              }
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            activityTypeOptions.drop(2).forEach { option ->
              Box(modifier = Modifier.width(210.dp)) {
                ZCheckbox(
                  checked = option in selectedActivityTypes,
                  onCheckedChange = { checked ->
                    selectedActivityTypes = if (checked) {
                      selectedActivityTypes + option
                    } else {
                      selectedActivityTypes - option
                    }
                  },
                  label = option
                )
              }
            }
          }
        }
      }
      ZFormItem(label = "Resources") {
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
          ZRadio(
            value = "Sponsor",
            selectedValue = selectedResource,
            onValueChange = { selectedResource = it },
            label = "Sponsor"
          )
          ZRadio(
            value = "Venue",
            selectedValue = selectedResource,
            onValueChange = { selectedResource = it },
            label = "Venue"
          )
        }
      }
      ZFormItem(label = "Activity form") {
        ZTextField(
          value = activityForm,
          onValueChange = { activityForm = it },
          type = ZTextFieldType.TEXTAREA,
          minLines = 4,
          placeholder = "Please input activity form",
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ZButton(
        type = ZColorType.PRIMARY,
        onClick = { ToastManager.success("Create success") }
      ) {
        Text("Create")
      }
      ZButton(
        onClick = {
          resetForm()
          ToastManager.success("Canceled")
        }
      ) {
        Text("Cancel")
      }
    }
  }
}
