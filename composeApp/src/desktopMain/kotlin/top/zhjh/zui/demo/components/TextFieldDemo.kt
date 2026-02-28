package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.Search
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.composable.ZTextFieldType

@Composable
fun textFieldDemoContent() {
  var textFieldDefault by remember { mutableStateOf("") }
  var textFieldDisabled by remember { mutableStateOf("") }
  var textFieldIcon by remember { mutableStateOf("") }
  var textFieldPassword by remember { mutableStateOf("") }
  var textFieldTextarea by remember { mutableStateOf("") }
  var textFieldTextareaFixed by remember { mutableStateOf("") }

  Column {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZTextField(value = textFieldDefault, onValueChange = { textFieldDefault = it }, placeholder = "Please input")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZTextField(value = textFieldDisabled, onValueChange = { textFieldDisabled = it }, enabled = false, placeholder = "Please input")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZTextField(
        value = textFieldIcon,
        onValueChange = { textFieldIcon = it },
        placeholder = "Please input",
        leadingIcon = { Icon(FeatherIcons.Search, contentDescription = null) },
        trailingIcon = { Icon(FeatherIcons.ChevronDown, contentDescription = null) },
        singleLine = false
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZTextField(
        value = textFieldPassword,
        onValueChange = { textFieldPassword = it },
        type = ZTextFieldType.PASSWORD,
        placeholder = "Please input password",
        showPassword = true
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZTextField(
        value = textFieldTextarea,
        onValueChange = { textFieldTextarea = it },
        placeholder = "Please input",
        type = ZTextFieldType.TEXTAREA,
        leadingIcon = { Icon(FeatherIcons.Search, contentDescription = null) },
        trailingIcon = { Icon(FeatherIcons.ChevronDown, contentDescription = null) }
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZTextField(
        value = textFieldTextareaFixed,
        onValueChange = { textFieldTextareaFixed = it },
        placeholder = "Please input",
        type = ZTextFieldType.TEXTAREA,
        resize = false,
        minLines = 3,
        maxLines = 3
      )
    }
  }
}
