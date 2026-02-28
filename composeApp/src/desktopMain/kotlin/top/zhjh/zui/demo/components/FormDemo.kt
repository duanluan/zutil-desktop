package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
  val formAlignOptions = remember {
    listOf(
      "Left" to ZFormLabelPosition.LEFT,
      "Right" to ZFormLabelPosition.RIGHT,
      "Top" to ZFormLabelPosition.TOP
    )
  }
  val formItemAlignOptions = remember {
    listOf(
      "Empty" to null,
      "Left" to ZFormLabelPosition.LEFT,
      "Right" to ZFormLabelPosition.RIGHT,
      "Top" to ZFormLabelPosition.TOP
    )
  }

  var activityName by remember { mutableStateOf("") }
  var activityZone by remember { mutableStateOf("") }
  var alignFormPosition by remember { mutableStateOf(ZFormLabelPosition.RIGHT) }
  var alignItemPosition by remember { mutableStateOf<ZFormLabelPosition?>(null) }
  var alignName by remember { mutableStateOf("") }
  var alignActivityZone by remember { mutableStateOf("") }
  var alignActivityForm by remember { mutableStateOf("") }
  var inlineApprovedBy by remember { mutableStateOf("") }
  var inlineActivityZone by remember { mutableStateOf("") }
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
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.widthIn(max = 780.dp)
    ) {
      ZText(
        text = "经典表单",
        size = ZTextSize.Large,
        fontWeight = FontWeight.SemiBold
      )
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

    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      ZText(
        text = "行内表单",
        size = ZTextSize.Large,
        fontWeight = FontWeight.SemiBold
      )
      ZForm(
        inline = true,
        labelPosition = ZFormLabelPosition.LEFT,
        labelWidth = null,
        modifier = Modifier.fillMaxWidth()
      ) {
        ZFormItem(label = "Approved by") {
          ZTextField(
            value = inlineApprovedBy,
            onValueChange = { inlineApprovedBy = it },
            placeholder = "Approved by",
            modifier = Modifier.width(280.dp)
          )
        }
        ZFormItem(label = "Activity zone") {
          ZDropdownMenu(
            options = activityZoneOptions,
            value = inlineActivityZone,
            onOptionSelected = { inlineActivityZone = it.orEmpty() },
            placeholder = "Activity zone",
            clearable = true,
            modifier = Modifier.width(280.dp)
          )
        }
        ZFormItem {
          ZButton(
            type = ZColorType.PRIMARY,
            onClick = { ToastManager.success("Query success") }
          ) {
            Text("Query")
          }
        }
      }
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.widthIn(max = 780.dp)
    ) {
      ZText(
        text = "对齐方式",
        size = ZTextSize.Large,
        fontWeight = FontWeight.SemiBold
      )
      LabelPositionSelectorRow(
        title = "Form Align",
        options = formAlignOptions,
        selected = alignFormPosition,
        onSelected = { alignFormPosition = it },
        buttonWidth = 74.dp
      )
      LabelPositionSelectorRow(
        title = "Form Item Align",
        options = formItemAlignOptions,
        selected = alignItemPosition,
        onSelected = { alignItemPosition = it },
        buttonWidth = 76.dp
      )
      ZForm(
        labelPosition = alignFormPosition,
        labelWidth = if (
          alignFormPosition == ZFormLabelPosition.TOP &&
          alignItemPosition == null
        ) {
          null
        } else {
          130.dp
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        ZFormItem(label = "Name", labelPosition = alignItemPosition) {
          ZTextField(
            value = alignName,
            onValueChange = { alignName = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(label = "Activity zone", labelPosition = alignItemPosition) {
          ZTextField(
            value = alignActivityZone,
            onValueChange = { alignActivityZone = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(label = "Activity form", labelPosition = alignItemPosition) {
          ZTextField(
            value = alignActivityForm,
            onValueChange = { alignActivityForm = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
private fun <T> LabelPositionSelectorRow(
  title: String,
  options: List<Pair<String, T>>,
  selected: T,
  onSelected: (T) -> Unit,
  buttonWidth: Dp
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ZText(
      text = title,
      modifier = Modifier.width(130.dp)
    )
    ZButtonGroup(itemSpacing = 0.dp) {
      options.forEach { (label, value) ->
        val isActive = value == selected
        ZButton(
          size = ZButtonSize.Small,
          type = if (isActive) ZColorType.PRIMARY else ZColorType.DEFAULT,
          plain = !isActive,
          onClick = { onSelected(value) },
          modifier = Modifier.width(buttonWidth)
        ) {
          Text(label)
        }
      }
    }
  }
}
