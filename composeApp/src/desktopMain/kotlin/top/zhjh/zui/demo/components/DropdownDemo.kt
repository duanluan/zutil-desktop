package top.zhjh.zui.demo.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.X
import kotlinx.coroutines.delay
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZColorType

@Composable
fun dropdownDemoContent(isDarkTheme: Boolean) {
  var dropdownClearableValue by remember { mutableStateOf("Option1") }
  var dropdownEmptyValueConfiguredOutput by remember { mutableStateOf<String?>("") }
  var dropdownFilterableValue by remember { mutableStateOf("") }
  var dropdownAllowCreateValues by remember { mutableStateOf(emptyList<String>()) }
  var dropdownLoadingIcon1 by remember { mutableStateOf(false) }
  var dropdownLoadingIcon2 by remember { mutableStateOf(false) }
  var dropdownLoadingIcon1Trigger by remember { mutableStateOf(0) }
  var dropdownLoadingIcon2Trigger by remember { mutableStateOf(0) }
  var dropdownMultiDefaultValue by remember { mutableStateOf(listOf("Option1", "Option2")) }
  var dropdownMultiCollapseValue by remember { mutableStateOf(listOf("Option1", "Option2", "Option3")) }
  var dropdownMultiCollapseTooltipValue by remember { mutableStateOf(listOf("Option1", "Option3", "Option4", "Option5")) }
  var dropdownMultiMaxCollapseValue by remember { mutableStateOf(listOf("Option1", "Option2", "Option3", "Option4")) }
  var dropdownCustomTagValues by remember { mutableStateOf(listOf("Option1")) }

  val dropdownHeaderCityOptions = remember { listOf("Beijing", "Shanghai", "Nanjing", "Chengdu", "Shenzhen", "Guangzhou") }
  var dropdownHeaderSelectedCities by remember { mutableStateOf(dropdownHeaderCityOptions) }
  val dropdownGroupedCityOptions = remember {
    listOf(
      ZDropdownMenuOptionGroup(
        label = "Popular cities",
        options = listOf("Shanghai", "Beijing")
      ),
      ZDropdownMenuOptionGroup(
        label = "City name",
        options = listOf("Chengdu", "Shenzhen", "Guangzhou", "Dalian")
      )
    )
  }
  val dropdownEmptyValueOptionItems = remember {
    listOf(
      ZDropdownMenuOption(
        label = "Empty string (value = \"\")",
        value = ""
      ),
      ZDropdownMenuOption(label = "Option1", value = "Option1"),
      ZDropdownMenuOption(label = "Option2", value = "Option2")
    )
  }
  var dropdownGroupedCityValue by remember { mutableStateOf("Shanghai") }
  var dropdownFooterOptions by remember { mutableStateOf(dropdownHeaderCityOptions) }
  var dropdownFooterValue by remember { mutableStateOf("") }
  var dropdownFooterInput by remember { mutableStateOf("") }
  var dropdownFooterEditing by remember { mutableStateOf(false) }
  val dropdownFooterInputFocusRequester = remember { FocusRequester() }

  LaunchedEffect(dropdownFooterEditing) {
    if (dropdownFooterEditing) {
      dropdownFooterInputFocusRequester.requestFocus()
    }
  }

  LaunchedEffect(dropdownLoadingIcon1Trigger) {
    if (dropdownLoadingIcon1Trigger > 0) {
      delay(3000)
      dropdownLoadingIcon1 = false
    }
  }
  LaunchedEffect(dropdownLoadingIcon2Trigger) {
    if (dropdownLoadingIcon2Trigger > 0) {
      delay(3000)
      dropdownLoadingIcon2 = false
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
  ) {
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(220.dp),
      disabledOptions = setOf("Option2")
    )
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      enabled = false,
      modifier = Modifier.width(220.dp)
    )
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(220.dp),
      clearable = true,
      value = dropdownClearableValue,
      onOptionSelected = { dropdownClearableValue = it.orEmpty() }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ZDropdownMenu(
        options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
        placeholder = "Select",
        size = ZDropdownMenuSize.Large,
        modifier = Modifier.width(160.dp)
      )
      ZDropdownMenu(
        options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
        placeholder = "Select",
        size = ZDropdownMenuSize.Default,
        modifier = Modifier.width(160.dp)
      )
      ZDropdownMenu(
        options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
        placeholder = "Select",
        size = ZDropdownMenuSize.Small,
        modifier = Modifier.width(160.dp)
      )
    }

    ZText("default")
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      multiple = true,
      values = dropdownMultiDefaultValue,
      onOptionsSelected = { dropdownMultiDefaultValue = it }
    )

    ZText("use collapse-tags")
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      multiple = true,
      collapseTags = true,
      values = dropdownMultiCollapseValue,
      onOptionsSelected = { dropdownMultiCollapseValue = it }
    )

    ZText("use collapse-tags-tooltip")
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      multiple = true,
      collapseTags = true,
      collapseTagsTooltip = true,
      values = dropdownMultiCollapseTooltipValue,
      onOptionsSelected = { dropdownMultiCollapseTooltipValue = it }
    )

    ZText("use max-collapse-tags")
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      multiple = true,
      maxCollapseTags = 3,
      values = dropdownMultiMaxCollapseValue,
      onOptionsSelected = { dropdownMultiMaxCollapseValue = it }
    )

    ZText("custom dropdown header")
    ZDropdownMenu(
      options = dropdownHeaderCityOptions,
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      multiple = true,
      clearable = true,
      collapseTags = true,
      values = dropdownHeaderSelectedCities,
      onOptionsSelected = { dropdownHeaderSelectedCities = it },
      dropdownHeader = {
        val allSelected = dropdownHeaderSelectedCities.size == dropdownHeaderCityOptions.size
        ZCheckbox(
          checked = allSelected,
          onCheckedChange = { checked ->
            dropdownHeaderSelectedCities = if (checked) {
              dropdownHeaderCityOptions
            } else {
              emptyList()
            }
          },
          modifier = Modifier
            .fillMaxWidth(),
          label = "All"
        )
      }
    )

    ZText("custom dropdown footer")
    ZDropdownMenu(
      options = dropdownFooterOptions,
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      value = dropdownFooterValue,
      onOptionSelected = { dropdownFooterValue = it.orEmpty() },
      dropdownFooter = {
        if (!dropdownFooterEditing) {
          ZButton(
            size = ZButtonSize.Small,
            onClick = { dropdownFooterEditing = true }
          ) {
            Text("Add an option")
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ZTextField(
              value = dropdownFooterInput,
              onValueChange = { dropdownFooterInput = it },
              placeholder = "input option name",
              modifier = Modifier
                .fillMaxWidth()
                .focusRequester(dropdownFooterInputFocusRequester)
            )
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ZButton(
                type = ZColorType.PRIMARY,
                size = ZButtonSize.Small,
                onClick = {
                  val newOption = dropdownFooterInput.trim()
                  if (newOption.isNotEmpty()) {
                    if (newOption !in dropdownFooterOptions) {
                      dropdownFooterOptions = dropdownFooterOptions + newOption
                    }
                    dropdownFooterValue = newOption
                  }
                  dropdownFooterInput = ""
                  dropdownFooterEditing = false
                }
              ) {
                Text("confirm")
              }
              ZButton(
                size = ZButtonSize.Small,
                onClick = {
                  dropdownFooterInput = ""
                  dropdownFooterEditing = false
                }
              ) {
                Text("cancel")
              }
            }
          }
        }
      }
    )

    ZText("grouped options")
    ZDropdownMenu(
      options = emptyList(),
      optionGroups = dropdownGroupedCityOptions,
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      value = dropdownGroupedCityValue,
      onOptionSelected = { dropdownGroupedCityValue = it.orEmpty() }
    )

    ZText("filterable")
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      filterable = true,
      filterMethod = { inputValue, option ->
        option.contains(inputValue, ignoreCase = true)
      },
      value = dropdownFilterableValue,
      onOptionSelected = { dropdownFilterableValue = it.orEmpty() }
    )

    ZText("allow-create and default-first-option")
    ZDropdownMenu(
      options = listOf("HTML", "CSS", "JavaScript"),
      placeholder = "Choose tags for your article",
      modifier = Modifier.width(360.dp),
      multiple = true,
      filterable = true,
      allowCreate = true,
      defaultFirstOption = true,
      values = dropdownAllowCreateValues,
      onOptionsSelected = { dropdownAllowCreateValues = it }
    )

    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ZText("loading icon1")
        ZDropdownMenu(
          options = emptyList(),
          placeholder = "Please enter a keyword",
          modifier = Modifier.width(360.dp),
          filterable = true,
          loading = dropdownLoadingIcon1,
          onExpandedChange = { expanded ->
            if (expanded) {
              dropdownLoadingIcon1 = true
              dropdownLoadingIcon1Trigger += 1
            }
          }
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ZText("loading icon2")
        ZDropdownMenu(
          options = emptyList(),
          placeholder = "Please enter a keyword",
          modifier = Modifier.width(360.dp),
          filterable = true,
          loading = dropdownLoadingIcon2,
          loadingIcon = { ZDropdownDotsLoadingIcon() },
          onExpandedChange = { expanded ->
            if (expanded) {
              dropdownLoadingIcon2 = true
              dropdownLoadingIcon2Trigger += 1
            }
          }
        )
      }
    }
    ZText("empty-values + value-on-clear")
    ZDropdownMenu(
      options = emptyList(),
      optionItems = dropdownEmptyValueOptionItems,
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      clearable = true,
      defaultSelectedOption = "",
      emptyValues = listOf(null),
      valueOnClear = null,
      onOptionSelected = { dropdownEmptyValueConfiguredOutput = it }
    )
    ZText(
      text = "Current value: ${dropdownEmptyValueConfiguredOutput?.let { "\"$it\"" } ?: "null"}",
      color = if (isDarkTheme) Color(0xffa3a6ad) else Color(0xff606266)
    )
    ZText("custom tag")
    ZDropdownMenu(
      options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
      placeholder = "Select",
      modifier = Modifier.width(360.dp),
      multiple = true,
      clearable = true,
      values = dropdownCustomTagValues,
      onOptionsSelected = { dropdownCustomTagValues = it },
      tagContent = { label, _, enabled, onRemove ->
        val tagBackgroundColor = if (isDarkTheme) Color(0xff2f3133) else Color(0xfff0f2f5)
        val tagBorderColor = if (isDarkTheme) Color(0xff4c4d4f) else Color(0xffe4e7ed)
        val tagTextColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266)
        val tagIconColor = if (enabled) {
          if (isDarkTheme) Color(0xffa3a6ad) else Color(0xff909399)
        } else {
          if (isDarkTheme) Color(0xff73767a) else Color(0xffc0c4cc)
        }
        Row(
          modifier = Modifier
            .background(tagBackgroundColor, ZCardDefaults.Shape)
            .border(1.dp, tagBorderColor, ZCardDefaults.Shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Label1: $label",
            color = tagTextColor,
            style = MaterialTheme.typography.body2
          )
          if (enabled) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = FeatherIcons.X,
              contentDescription = "Remove $label",
              tint = tagIconColor,
              modifier = Modifier
                .size(12.dp)
                .clickable { onRemove() }
            )
          }
        }
      }
    )
  }
}

@Composable
private fun ZDropdownDotsLoadingIcon() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val radius = size.minDimension * 0.16f
    val orbit = size.minDimension * 0.30f
    val center = center
    val dotCenters = listOf(
      Offset(center.x, center.y - orbit),
      Offset(center.x + orbit, center.y),
      Offset(center.x, center.y + orbit),
      Offset(center.x - orbit, center.y)
    )
    val dotColors = listOf(
      Color(0xff409eff),
      Color(0xff8bc2ff),
      Color(0xffb6d7ff),
      Color(0xff7cb9ff)
    )

    dotCenters.forEachIndexed { index, dotCenter ->
      drawCircle(
        color = dotColors[index],
        radius = radius,
        center = dotCenter
      )
    }
  }
}
