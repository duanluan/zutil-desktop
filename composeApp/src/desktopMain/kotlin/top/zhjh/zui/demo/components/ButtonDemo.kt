package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZColorType

@Composable
fun buttonDemoContent() {
  var buttonGroupDirectionValue by remember { mutableStateOf("horizontal") }

  Column {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(onClick = {}) {}
      ZButton(onClick = { ToastManager.success("Default") }) { Text("Default") }
      ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
      ZButton(type = ZColorType.SUCCESS, onClick = { ToastManager.success("Success") }) { Text("Success") }
      ZButton(type = ZColorType.INFO, onClick = { ToastManager.success("Info") }) { Text("Info") }
      ZButton(type = ZColorType.WARNING, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
      ZButton(type = ZColorType.DANGER, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(plain = true, onClick = { ToastManager.success("Plain") }) { Text("Plain") }
      ZButton(type = ZColorType.PRIMARY, plain = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
      ZButton(type = ZColorType.SUCCESS, plain = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
      ZButton(type = ZColorType.INFO, plain = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
      ZButton(type = ZColorType.WARNING, plain = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
      ZButton(type = ZColorType.DANGER, plain = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(round = true, onClick = { ToastManager.success("Round") }) { Text("Round") }
      ZButton(type = ZColorType.PRIMARY, round = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
      ZButton(type = ZColorType.SUCCESS, round = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
      ZButton(type = ZColorType.INFO, round = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
      ZButton(type = ZColorType.WARNING, round = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
      ZButton(type = ZColorType.DANGER, round = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(circle = true, icon = { Icon(FeatherIcons.Search, contentDescription = null) }, onClick = { ToastManager.success("Circle") }) {}
      ZButton(type = ZColorType.PRIMARY, circle = true, icon = { Icon(FeatherIcons.Search, contentDescription = null) }, onClick = { ToastManager.success("Primary") }) {}
      ZButton(type = ZColorType.SUCCESS, circle = true, icon = { Icon(FeatherIcons.Edit, contentDescription = null) }, onClick = { ToastManager.success("Success") }) {}
      ZButton(type = ZColorType.INFO, circle = true, icon = { Icon(FeatherIcons.Check, contentDescription = null) }, onClick = { ToastManager.success("Info") }) {}
      ZButton(type = ZColorType.WARNING, circle = true, icon = { Icon(FeatherIcons.Mail, contentDescription = null) }, onClick = { ToastManager.success("Warning") }) {}
      ZButton(type = ZColorType.DANGER, circle = true, icon = { Icon(FeatherIcons.Trash2, contentDescription = null) }, onClick = { ToastManager.success("Danger") }) {}
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(enabled = false, onClick = { ToastManager.success("Default") }) { Text("Default") }
      ZButton(type = ZColorType.PRIMARY, enabled = false, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
      ZButton(type = ZColorType.SUCCESS, enabled = false, onClick = { ToastManager.success("Success") }) { Text("Success") }
      ZButton(type = ZColorType.INFO, enabled = false, onClick = { ToastManager.success("Info") }) { Text("Info") }
      ZButton(type = ZColorType.WARNING, enabled = false, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
      ZButton(type = ZColorType.DANGER, enabled = false, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(enabled = false, plain = true, onClick = { ToastManager.success("Plain") }) { Text("Plain") }
      ZButton(type = ZColorType.PRIMARY, enabled = false, plain = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
      ZButton(type = ZColorType.SUCCESS, enabled = false, plain = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
      ZButton(type = ZColorType.INFO, enabled = false, plain = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
      ZButton(type = ZColorType.WARNING, enabled = false, plain = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
      ZButton(type = ZColorType.DANGER, enabled = false, plain = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)) {
      ZButtonGroup {
        ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Previous Page") }) {
          Icon(FeatherIcons.ChevronLeft, contentDescription = null)
          Spacer(modifier = Modifier.width(1.dp))
          Text("Previous Page")
        }
        ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Next Page") }) {
          Text("Next Page")
          Spacer(modifier = Modifier.width(1.dp))
          Icon(FeatherIcons.ChevronRight, contentDescription = null)
        }
      }
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(28.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
      ZRadio(
        value = "horizontal",
        selectedValue = buttonGroupDirectionValue,
        onValueChange = { buttonGroupDirectionValue = it },
        label = "Horizontal"
      )
      ZRadio(
        value = "vertical",
        selectedValue = buttonGroupDirectionValue,
        onValueChange = { buttonGroupDirectionValue = it },
        label = "Vertical"
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 4.dp, bottom = 5.dp)) {
      ZButtonGroup(
        direction = if (buttonGroupDirectionValue == "horizontal") {
          ZButtonGroupDirection.Horizontal
        } else {
          ZButtonGroupDirection.Vertical
        }
      ) {
        ZButton(
          type = ZColorType.PRIMARY,
          icon = { Icon(FeatherIcons.Home, contentDescription = null) },
          modifier = Modifier.width(45.dp),
          onClick = { ToastManager.success("Home") }
        ) {}
        ZButton(
          type = ZColorType.PRIMARY,
          icon = { Icon(FeatherIcons.Sliders, contentDescription = null) },
          modifier = Modifier.width(45.dp),
          onClick = { ToastManager.success("Sliders") }
        ) {}
        ZButton(
          type = ZColorType.PRIMARY,
          icon = { Icon(FeatherIcons.Copy, contentDescription = null) },
          modifier = Modifier.width(45.dp),
          onClick = { ToastManager.success("Copy") }
        ) {}
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)) {
      ZButton(type = ZColorType.PRIMARY, loading = true, loadingIconSpacing = 5.dp, onClick = { ToastManager.success("Loading") }) {
        Text("Loading")
      }
      ZButton(
        type = ZColorType.PRIMARY,
        loading = true,
        loadingIcon = { Icon(FeatherIcons.Tool, contentDescription = null) },
        loadingIconSpacing = 5.dp,
        onClick = { ToastManager.success("Loading") }
      ) {
        Text("Loading")
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)) {
      ZButton(size = ZButtonSize.Large, onClick = { ToastManager.success("Large") }) { Text("Large") }
      ZButton(size = ZButtonSize.Default, onClick = { ToastManager.success("Default") }) { Text("Default") }
      ZButton(size = ZButtonSize.Small, onClick = { ToastManager.success("Small") }) { Text("Small") }
      ZButton(
        size = ZButtonSize.Large,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Search") }
      ) { Text("Search") }
      ZButton(
        size = ZButtonSize.Default,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Search") }
      ) { Text("Search") }
      ZButton(
        size = ZButtonSize.Small,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Search") }
      ) { Text("Search") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(size = ZButtonSize.Large, round = true, onClick = { ToastManager.success("Large Round") }) { Text("Large") }
      ZButton(size = ZButtonSize.Default, round = true, onClick = { ToastManager.success("Default Round") }) { Text("Default") }
      ZButton(size = ZButtonSize.Small, round = true, onClick = { ToastManager.success("Small Round") }) { Text("Small") }
      ZButton(
        size = ZButtonSize.Large,
        round = true,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Search Round") }
      ) { Text("Search") }
      ZButton(
        size = ZButtonSize.Default,
        round = true,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Search Round") }
      ) { Text("Search") }
      ZButton(
        size = ZButtonSize.Small,
        round = true,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Search Round") }
      ) { Text("Search") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
      ZButton(
        size = ZButtonSize.Large,
        circle = true,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Large Circle") }
      ) {}
      ZButton(
        size = ZButtonSize.Default,
        circle = true,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Default Circle") }
      ) {}
      ZButton(
        size = ZButtonSize.Small,
        circle = true,
        icon = { Icon(FeatherIcons.Search, contentDescription = null) },
        onClick = { ToastManager.success("Small Circle") }
      ) {}
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)) {
      ZButton(
        type = ZColorType.PRIMARY,
        href = "https://github.com/duanluan/zutil-desktop",
        icon = { Icon(FeatherIcons.Link, contentDescription = null) }
      ) {
        Text("zutil-desktop")
      }
    }
  }
}
