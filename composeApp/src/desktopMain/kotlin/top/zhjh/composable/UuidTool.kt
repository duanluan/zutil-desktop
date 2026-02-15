package top.zhjh.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Code
import compose.icons.feathericons.List
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.viewmodel.UuidViewModel
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZCard
import top.zhjh.zui.composable.ZDropdownMenu
import top.zhjh.zui.composable.ZText
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.composable.ZTextFieldType
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.isAppInDarkTheme
import java.awt.Cursor
import java.awt.FileDialog
import java.io.File
import javax.swing.JFrame

@Composable
fun UuidTool() {
  val viewModel: UuidViewModel = viewModel { UuidViewModel() }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ZCard(
        shadow = ZCardShadow.NEVER,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UuidField(
              label = "UUID 版本",
              content = {
                ZDropdownMenu(
                  options = viewModel.versionOptions,
                  defaultSelectedOption = viewModel.selectedVersion,
                  modifier = Modifier.width(170.dp),
                  onOptionSelected = { viewModel.selectedVersion = it }
                )
              }
            )

            UuidField(
              label = "生成数量",
              content = {
                ZTextField(
                  value = viewModel.generateCountInput,
                  onValueChange = { viewModel.generateCountInput = it },
                  modifier = Modifier.width(170.dp),
                  numericOnly = true
                )
              }
            )

            UuidField(
              label = "结果格式",
              content = {
                ZDropdownMenu(
                  options = viewModel.formatOptions,
                  defaultSelectedOption = viewModel.selectedFormat,
                  modifier = Modifier.width(170.dp),
                  onOptionSelected = { viewModel.selectedFormat = it }
                )
              }
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UuidRadioGroup(
              label = "大小写",
              leftLabel = "大写",
              leftSelected = viewModel.isUppercase,
              onLeftSelected = { viewModel.isUppercase = true },
              rightLabel = "小写",
              rightSelected = !viewModel.isUppercase,
              onRightSelected = { viewModel.isUppercase = false }
            )

            UuidRadioGroup(
              label = "中划线",
              leftLabel = "保留",
              leftSelected = viewModel.keepHyphen,
              onLeftSelected = { viewModel.keepHyphen = true },
              rightLabel = "去除",
              rightSelected = !viewModel.keepHyphen,
              onRightSelected = { viewModel.keepHyphen = false }
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.generate() }) {
              ZText("生成")
            }
            ZButton(type = ZColorType.DEFAULT, onClick = { viewModel.copyResult() }) {
              ZText("复制")
            }
            ZButton(
              type = ZColorType.DEFAULT,
              onClick = {
                if (viewModel.resultText.isBlank()) {
                  ToastManager.show("暂无可下载内容")
                  return@ZButton
                }
                val path = pickSaveFilePath("uuid-result.txt")
                if (path != null) {
                  if (viewModel.saveResultTo(path)) {
                    ToastManager.success("下载成功")
                  }
                }
              }
            ) {
              ZText("下载")
            }
            ZButton(type = ZColorType.DEFAULT, onClick = { viewModel.clearResult() }) {
              ZText("清空")
            }
          }
        }
      }

      ZCard(
        shadow = ZCardShadow.NEVER,
        modifier = Modifier.weight(1f).fillMaxWidth()
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ZText("结果", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            ZButton(
              type = if (viewModel.isListMode) ZColorType.PRIMARY else ZColorType.DEFAULT,
              icon = {
                Icon(
                  imageVector = if (viewModel.isListMode) FeatherIcons.Code else FeatherIcons.List,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
              },
              onClick = { viewModel.isListMode = !viewModel.isListMode }
            ) {
              ZText(if (viewModel.isListMode) "切换为文本" else "切换为列表")
            }
          }
          Spacer(modifier = Modifier.height(8.dp))

          if (viewModel.resultText.isBlank()) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              ZText(
                text = "生成后的 UUID 列表...",
                color = if (isAppInDarkTheme()) Color(0xFF8D9095) else Color(0xFFA8ABB2)
              )
            }
          } else if (!viewModel.isListMode) {
            ZTextField(
              value = viewModel.resultText,
              onValueChange = {},
              type = ZTextFieldType.TEXTAREA,
              readOnly = true,
              resize = false,
              modifier = Modifier.fillMaxSize(),
              textStyle = MaterialTheme.typography.body2.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
              )
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              itemsIndexed(viewModel.resultItems) { index, item ->
                UuidResultRow(
                  text = item,
                  copied = index in viewModel.copiedResultItemIndexes,
                  onCopy = { viewModel.copyResultItem(index) }
                )
              }
            }
          }
        }
      }
    }

    ToastContainer()
  }
}

@Composable
private fun UuidResultRow(
  text: String,
  copied: Boolean,
  onCopy: () -> Unit
) {
  val isDarkTheme = isAppInDarkTheme()
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()

  val defaultTextColor = if (isDarkTheme) Color(0xFFD8DEE9) else Color(0xFF2B2B2B)
  val hoverTextColor = if (isDarkTheme) Color(0xFF8DC5FF) else Color(0xFF1D67B2)
  val copiedTextColor = if (isDarkTheme) Color(0xFF7ED97E) else Color(0xFF2E7D32)

  val rowTextColor = when {
    copied -> copiedTextColor
    isHovered -> hoverTextColor
    else -> defaultTextColor
  }

  val rowBackgroundColor = when {
    copied && isHovered -> if (isDarkTheme) Color(0x1F67C23A) else Color(0x1A67C23A)
    copied -> if (isDarkTheme) Color(0x1467C23A) else Color(0x1467C23A)
    isHovered -> if (isDarkTheme) Color(0x1F409EFF) else Color(0x14409EFF)
    else -> Color.Transparent
  }

  val hintColor = when {
    copied -> copiedTextColor
    else -> if (isDarkTheme) Color(0xFFB0BAC5) else Color(0xFF909399)
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .background(color = rowBackgroundColor, shape = RoundedCornerShape(4.dp))
      .hoverable(interactionSource)
      .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onCopy
      )
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    ZText(
      text = text,
      color = rowTextColor,
      fontFamily = FontFamily.Monospace,
      fontSize = 14.sp,
      modifier = Modifier.weight(1f)
    )

    if (isHovered || copied) {
      ZText(
        text = if (copied) "已复制" else "点击复制",
        color = hintColor,
        fontSize = 12.sp
      )
    }
  }
}

@Composable
private fun UuidField(
  label: String,
  content: @Composable () -> Unit
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    ZButton(
      type = ZColorType.DEFAULT,
      enabled = false,
      modifier = Modifier.width(96.dp),
      onClick = {}
    ) {
      ZText(label)
    }
    Spacer(modifier = Modifier.width(8.dp))
    content()
  }
}

@Composable
private fun UuidRadioGroup(
  label: String,
  leftLabel: String,
  leftSelected: Boolean,
  onLeftSelected: () -> Unit,
  rightLabel: String,
  rightSelected: Boolean,
  onRightSelected: () -> Unit
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    ZButton(
      type = ZColorType.DEFAULT,
      enabled = false,
      modifier = Modifier.width(96.dp),
      onClick = {}
    ) {
      ZText(label)
    }
    Spacer(modifier = Modifier.width(8.dp))

    RadioButton(selected = leftSelected, onClick = onLeftSelected)
    ZText(leftLabel)
    Spacer(modifier = Modifier.width(12.dp))
    RadioButton(selected = rightSelected, onClick = onRightSelected)
    ZText(rightLabel)
  }
}

private fun pickSaveFilePath(defaultFileName: String): String? {
  val dialog = FileDialog(null as JFrame?, "保存 UUID 结果", FileDialog.SAVE)
  dialog.file = defaultFileName
  dialog.isVisible = true
  return if (dialog.directory != null && dialog.file != null) {
    File(dialog.directory, dialog.file).absolutePath
  } else {
    null
  }
}
