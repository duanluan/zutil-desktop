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
  val activityCountOptions = remember {
    listOf("Count one", "Count two", "Count three", "Count four")
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
  val validateFormState = rememberZFormState()
  val validateRules = remember {
    mapOf(
      "activityName" to listOf(
        ZFormRule(required = true, message = "Please input activity name")
      ),
      "activityZone" to listOf(
        ZFormRule(required = true, message = "Please select activity zone")
      ),
      "activityCount" to listOf(
        ZFormRule(required = true, message = "Please select activity count")
      ),
      "activityType" to listOf(
        ZFormRule(
          required = true,
          type = ZFormRuleType.ARRAY,
          message = "Please select at least one activity type"
        )
      ),
      "resource" to listOf(
        ZFormRule(required = true, message = "Please select a resource")
      ),
      "activityForm" to listOf(
        ZFormRule(required = true, message = "Please input activity form")
      )
    )
  }
  var validateActivityName by remember { mutableStateOf("Hello") }
  var validateActivityZone by remember { mutableStateOf("") }
  var validateActivityCount by remember { mutableStateOf("") }
  var validateInstantDelivery by remember { mutableStateOf(false) }
  var validateActivityTypes by remember { mutableStateOf(emptySet<String>()) }
  var validateResource by remember { mutableStateOf<String?>(null) }
  var validateActivityForm by remember { mutableStateOf("") }
  val customRuleFormState = rememberZFormState()
  val customRuleRules = remember {
    mapOf(
      "pass" to listOf(
        ZFormRule(
          validator = { value, _ ->
            val pass = (value as? String).orEmpty()
            if (pass.isBlank()) "Please input the password" else null
          }
        )
      ),
      "checkPass" to listOf(
        ZFormRule(
          validator = { value, model ->
            val checkPass = (value as? String).orEmpty()
            val pass = (model["pass"] as? String).orEmpty()
            when {
              checkPass.isBlank() -> "Please input the password again"
              checkPass != pass -> "Two inputs don't match!"
              else -> null
            }
          }
        )
      ),
      "age" to listOf(
        ZFormRule(
          validator = { value, _ ->
            val ageText = (value as? String).orEmpty()
            val ageValue = ageText.toIntOrNull()
            when {
              ageText.isBlank() -> "Please input the age"
              ageValue == null -> "Please input digits"
              ageValue < 18 -> "Age must be greater than 18"
              else -> null
            }
          }
        )
      )
    )
  }
  var customPass by remember { mutableStateOf("") }
  var customCheckPass by remember { mutableStateOf("") }
  var customAge by remember { mutableStateOf("") }

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
        itemSpacing = 18.dp,
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

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 2.dp)
      ) {
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
        itemSpacing = 18.dp,
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
        itemSpacing = 18.dp,
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

    Column(
      verticalArrangement = Arrangement.spacedBy(0.dp),
      modifier = Modifier.widthIn(max = 780.dp)
    ) {
      ZText(
        text = "表单校验",
        size = ZTextSize.Large,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(Modifier.height(16.dp))
      ZForm(
        state = validateFormState,
        model = mapOf(
          "activityName" to validateActivityName,
          "activityZone" to validateActivityZone,
          "activityCount" to validateActivityCount,
          "instantDelivery" to validateInstantDelivery,
          "activityType" to validateActivityTypes,
          "resource" to validateResource,
          "activityForm" to validateActivityForm
        ),
        rules = validateRules,
        itemSpacing = 0.dp,
        messageReserveHeight = 18.dp,
        labelWidth = 130.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        ZFormItem(
          label = "Activity name",
          prop = "activityName",
          value = validateActivityName,
          onReset = { validateActivityName = "" }
        ) {
          ZTextField(
            value = validateActivityName,
            onValueChange = { validateActivityName = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(
          label = "Activity zone",
          prop = "activityZone",
          value = validateActivityZone,
          onReset = { validateActivityZone = "" }
        ) {
          ZDropdownMenu(
            options = activityZoneOptions,
            value = validateActivityZone,
            onOptionSelected = { validateActivityZone = it.orEmpty() },
            placeholder = "Activity zone",
            clearable = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(
          label = "Activity count",
          prop = "activityCount",
          value = validateActivityCount,
          onReset = { validateActivityCount = "" }
        ) {
          ZDropdownMenu(
            options = activityCountOptions,
            value = validateActivityCount,
            onOptionSelected = { validateActivityCount = it.orEmpty() },
            placeholder = "Activity count",
            clearable = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(
          label = "Instant delivery",
          prop = "instantDelivery",
          value = validateInstantDelivery,
          onReset = { validateInstantDelivery = false }
        ) {
          ZSwitch(
            checked = validateInstantDelivery,
            onCheckedChange = { validateInstantDelivery = it }
          )
        }
        ZFormItem(
          label = "Activity type",
          prop = "activityType",
          value = validateActivityTypes,
          onReset = { validateActivityTypes = emptySet() }
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
              activityTypeOptions.take(2).forEach { option ->
                Box(modifier = Modifier.width(210.dp)) {
                  ZCheckbox(
                    checked = option in validateActivityTypes,
                    onCheckedChange = { checked ->
                      validateActivityTypes = if (checked) {
                        validateActivityTypes + option
                      } else {
                        validateActivityTypes - option
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
                    checked = option in validateActivityTypes,
                    onCheckedChange = { checked ->
                      validateActivityTypes = if (checked) {
                        validateActivityTypes + option
                      } else {
                        validateActivityTypes - option
                      }
                    },
                    label = option
                  )
                }
              }
            }
          }
        }
        ZFormItem(
          label = "Resources",
          prop = "resource",
          value = validateResource,
          onReset = { validateResource = null }
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            ZRadio(
              value = "Sponsorship",
              selectedValue = validateResource,
              onValueChange = { validateResource = it },
              label = "Sponsorship"
            )
            ZRadio(
              value = "Venue",
              selectedValue = validateResource,
              onValueChange = { validateResource = it },
              label = "Venue"
            )
          }
        }
        ZFormItem(
          label = "Activity form",
          prop = "activityForm",
          value = validateActivityForm,
          onReset = { validateActivityForm = "" }
        ) {
          ZTextField(
            value = validateActivityForm,
            onValueChange = { validateActivityForm = it },
            type = ZTextFieldType.TEXTAREA,
            minLines = 3,
            placeholder = "Please input activity form",
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ZButton(
          type = ZColorType.PRIMARY,
          onClick = {
            val valid = validateFormState.validate()
            if (valid) {
              ToastManager.success("Create success")
            } else {
              ToastManager.error("Please complete required fields")
            }
          }
        ) {
          Text("Create")
        }
        ZButton(
          onClick = {
            validateFormState.resetFields()
            validateFormState.clearValidate()
          }
        ) {
          Text("Reset")
        }
      }
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(0.dp),
      modifier = Modifier.widthIn(max = 600.dp)
    ) {
      ZText(
        text = "自定义校验规则",
        size = ZTextSize.Large,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(Modifier.height(16.dp))
      ZForm(
        state = customRuleFormState,
        model = mapOf(
          "pass" to customPass,
          "checkPass" to customCheckPass,
          "age" to customAge
        ),
        rules = customRuleRules,
        statusIcon = true,
        itemSpacing = 0.dp,
        messageReserveHeight = 18.dp,
        labelPosition = ZFormLabelPosition.LEFT,
        labelWidth = 100.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        ZFormItem(
          label = "Password",
          prop = "pass",
          value = customPass,
          onReset = { customPass = "" }
        ) {
          ZTextField(
            value = customPass,
            onValueChange = {
              customPass = it
              if (customCheckPass.isNotBlank()) {
                customRuleFormState.validateField("checkPass", ZFormValidateTrigger.CHANGE)
              }
            },
            type = ZTextFieldType.PASSWORD,
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(
          label = "Confirm",
          prop = "checkPass",
          value = customCheckPass,
          onReset = { customCheckPass = "" }
        ) {
          ZTextField(
            value = customCheckPass,
            onValueChange = { customCheckPass = it },
            type = ZTextFieldType.PASSWORD,
            modifier = Modifier.fillMaxWidth()
          )
        }
        ZFormItem(
          label = "Age",
          prop = "age",
          value = customAge,
          onReset = { customAge = "" }
        ) {
          ZTextField(
            value = customAge,
            onValueChange = { customAge = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ZButton(
          type = ZColorType.PRIMARY,
          onClick = {
            val valid = customRuleFormState.validate()
            if (valid) {
              ToastManager.success("Submit success")
            } else {
              ToastManager.error("Please check form items")
            }
          }
        ) {
          Text("Submit")
        }
        ZButton(
          onClick = {
            customRuleFormState.resetFields()
            customRuleFormState.clearValidate()
          }
        ) {
          Text("Reset")
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

