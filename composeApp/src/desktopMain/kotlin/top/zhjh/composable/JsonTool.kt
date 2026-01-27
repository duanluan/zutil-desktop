package top.zhjh.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.viewmodel.JsonViewModel
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.isAppInDarkTheme

@Composable
fun JsonTool() {
  val viewModel: JsonViewModel = viewModel { JsonViewModel() }
  val focusManager = LocalFocusManager.current

  Box(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {

      // ==================== 1. 左侧：历史记录 ====================
      ZCard(
        shadow = ZCardShadow.NEVER,
        modifier = Modifier.width(180.dp).fillMaxHeight(),
        contentPadding = PaddingValues(10.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
          ) {
            Icon(FeatherIcons.Clock, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(Modifier.width(8.dp))
            ZText("历史记录", color = Color.Gray, fontSize = 14.sp)
          }

          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(viewModel.historyList) { json ->
              HistoryItem(json) {
                focusManager.clearFocus()
                viewModel.loadFromHistory(json)
              }
            }
          }

          if (viewModel.historyList.isNotEmpty()) {
            ZButton(
              type = ZColorType.DANGER,
              plain = true,
              modifier = Modifier.fillMaxWidth(),
              icon = { Icon(FeatherIcons.Trash2, null, modifier = Modifier.size(14.dp)) },
              onClick = { viewModel.historyList.clear() }
            ) {
              ZText("清空历史")
            }
          }
        }
      }

      // ==================== 2. 中间：输入区域 ====================
      Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
        // 顶部工具栏
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          ZButton(
            type = ZColorType.PRIMARY,
            icon = { Icon(FeatherIcons.AlignLeft, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              focusManager.clearFocus()
              viewModel.formatJson()
            }) {
            ZText("格式化")
          }
          ZButton(
            type = ZColorType.DEFAULT,
            icon = { Icon(FeatherIcons.Minimize2, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              focusManager.clearFocus()
              viewModel.compressJson()
            }) {
            ZText("压缩")
          }
          ZButton(
            type = ZColorType.DEFAULT,
            icon = { Icon(FeatherIcons.PlayCircle, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              focusManager.clearFocus()
              viewModel.loadDemo()
            }) {
            ZText("Demo")
          }
          Spacer(Modifier.weight(1f))

          ZButton(
            type = ZColorType.SUCCESS, plain = true,
            icon = { Icon(FeatherIcons.Copy, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
              clipboard.setContents(java.awt.datatransfer.StringSelection(viewModel.inputJson), null)
              ToastManager.success("已复制")
            }
          )

          ZButton(
            type = ZColorType.DANGER, plain = true,
            icon = { Icon(FeatherIcons.Trash2, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              focusManager.clearFocus()
              viewModel.clear()
            }
          )
        }


        Spacer(Modifier.height(10.dp))

        // 输入框容器
        ZCard(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          shadow = ZCardShadow.NEVER,
          contentPadding = PaddingValues(0.dp)
        ) {
          // 回归最纯粹的输入框，无行号干扰
          ZTextField(
            value = viewModel.inputJson,
            onValueChange = { viewModel.inputJson = it },
            type = ZTextFieldType.TEXTAREA,
            resize = false,
            modifier = Modifier.fillMaxSize()
              .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key == Key.Enter && (it.isCtrlPressed || it.isMetaPressed)) {
                  focusManager.clearFocus()
                  viewModel.formatJson()
                  true
                } else false
              },
            placeholder = "在此输入 JSON 字符串 (Ctrl+Enter 格式化)...",
            textStyle = LocalTextStyle.current.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 14.sp
            )
          )
        }

        Spacer(Modifier.height(10.dp))

        // 底部工具栏 (操作左侧)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          UtilButton("Unicode转中文") { focusManager.clearFocus(); viewModel.unicodeToChineseInput() }
          UtilButton("中转Unicode") { focusManager.clearFocus(); viewModel.chineseToUnicodeInput() }
          UtilButton("去转义") { focusManager.clearFocus(); viewModel.unescapeJsonInput() }
          UtilButton("加转义") { focusManager.clearFocus(); viewModel.escapeJsonInput() }
        }
      }

      // ==================== 2.5 中间穿梭按钮区 ====================
      Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center
      ) {
        ZButton(
          type = ZColorType.DEFAULT, plain = true,
          icon = { Icon(FeatherIcons.ChevronsRight, null, modifier = Modifier.size(16.dp)) },
          contentPadding = PaddingValues(0.dp),
          modifier = Modifier.width(24.dp).height(30.dp),
          onClick = {
            focusManager.clearFocus()
            viewModel.formatJson()
          }
        )

        Spacer(Modifier.height(10.dp))

        ZButton(
          type = ZColorType.DEFAULT, plain = true,
          icon = { Icon(FeatherIcons.ChevronsLeft, null, modifier = Modifier.size(16.dp)) },
          contentPadding = PaddingValues(0.dp),
          modifier = Modifier.width(24.dp).height(30.dp),
          onClick = {
            focusManager.clearFocus()
            viewModel.overwriteLeft()
          }
        )
      }

      // ==================== 3. 右侧：输出/树形区域 ====================
      Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
        // 顶部工具栏
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          ZButton(
            type = if (viewModel.isTreeMode) ZColorType.PRIMARY else ZColorType.DEFAULT,
            icon = { Icon(if (viewModel.isTreeMode) FeatherIcons.Code else FeatherIcons.List, null, modifier = Modifier.size(14.dp)) },
            onClick = { viewModel.isTreeMode = !viewModel.isTreeMode }
          ) {
            ZText(if (viewModel.isTreeMode) "切换为源码" else "切换为树形")
          }

          Spacer(Modifier.weight(1f))

          if (viewModel.isTreeMode) {
            ZButton(
              type = ZColorType.DEFAULT, plain = true,
              icon = { Icon(FeatherIcons.Minimize2, null, modifier = Modifier.size(14.dp)) },
              onClick = { viewModel.treeExpandState = 2 }
            )

            ZButton(
              type = ZColorType.DEFAULT, plain = true,
              icon = { Icon(FeatherIcons.Maximize2, null, modifier = Modifier.size(14.dp)) },
              onClick = { viewModel.treeExpandState = 1 }
            )
          }

          ZButton(
            type = ZColorType.SUCCESS, plain = true,
            icon = { Icon(FeatherIcons.Copy, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
              clipboard.setContents(java.awt.datatransfer.StringSelection(viewModel.outputJson), null)
              ToastManager.success("已复制结果")
            }
          )
        }


        if (viewModel.errorMessage != null) {
          Spacer(Modifier.height(5.dp))
          ZCard(
            shadow = ZCardShadow.NEVER,
            modifier = Modifier.background(Color(0xFFFFEBEE), ZCardDefaults.Shape),
            contentPadding = PaddingValues(8.dp)
          ) {
            ZText(
              text = viewModel.errorMessage!!,
              color = Color.Red,
              fontSize = 12.sp,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Spacer(Modifier.height(10.dp))

        // 结果显示区
        ZCard(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          shadow = ZCardShadow.NEVER,
          contentPadding = PaddingValues(0.dp)
        ) {
          if (viewModel.isTreeMode) {
            JsonTreeView(
              jsonString = viewModel.outputJson.ifEmpty { viewModel.inputJson },
              isUnicodeDisplay = viewModel.isUnicodeDisplay,
              expandState = viewModel.treeExpandState
            )
          } else {
            ZTextField(
              value = viewModel.outputJson,
              onValueChange = { viewModel.outputJson = it },
              type = ZTextFieldType.TEXTAREA,
              resize = false,
              modifier = Modifier.fillMaxSize(),
              placeholder = "解析结果...",
              textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = if (isAppInDarkTheme()) Color(0xFFA5C261) else Color(0xFF6A8759)
              )
            )
          }
        }

        Spacer(Modifier.height(10.dp))

        // 右侧底部工具栏
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          UtilButton("Unicode转中文") { focusManager.clearFocus(); viewModel.unicodeToChineseOutput() }
          UtilButton("中转Unicode") { focusManager.clearFocus(); viewModel.chineseToUnicodeOutput() }
          UtilButton("去转义") { focusManager.clearFocus(); viewModel.unescapeJsonOutput() }
          UtilButton("加转义") { focusManager.clearFocus(); viewModel.escapeJsonOutput() }
        }
      }
    }
    ToastContainer()
  }
}

// 辅助组件

@Composable
private fun HistoryItem(json: String, onClick: () -> Unit) {
  val preview = json.replace("\\s".toRegex(), " ").take(200)
  val isDark = isAppInDarkTheme()

  ZCard(
    shadow = ZCardShadow.HOVER,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    contentPadding = PaddingValues(8.dp)
  ) {
    ZText(
      text = preview,
      color = if (isDark) Color.LightGray else Color.DarkGray,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
      lineHeight = 14.sp
    )
  }
}

@Composable
private fun UtilButton(text: String, onClick: () -> Unit) {
  ZButton(
    type = ZColorType.DEFAULT, plain = true,
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    onClick = onClick
  ) {
    ZText(text, fontSize = 11.sp)
  }
}
