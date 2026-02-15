package top.zhjh.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import top.csaf.json.JsonUtil
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.viewmodel.JsonViewModel
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.isAppInDarkTheme

/**
 * JSON 工具主界面。
 *
 * 布局分三列：
 * 1. 左侧：历史记录
 * 2. 中间：输入与左侧工具栏
 * 3. 右侧：结果（树形/源码）与右侧工具栏
 *
 * 关键交互：
 * - `Ctrl/Cmd + Enter` 快捷格式化
 * - 树形/源码模式切换
 * - 复制时可选择缩进风格（2 空格 / 4 空格 / 1 Tab）
 * - 源码模式启用 JSON 语法高亮
 *
 * 数据流约定：
 * - 左侧输入区（`inputJson`）是“原始编辑面”；
 * - 右侧结果区（`outputJson`）是“处理结果面”，可二次处理但默认不覆盖左侧；
 * - 中间穿梭按钮用于显式完成“左 -> 右 格式化”与“右 -> 左 覆盖”。
 *
 * 交互细节：
 * - 输入区支持 `Ctrl/Cmd + Enter` 快捷格式化；
 * - 点击历史记录会回填左侧并自动格式化；
 * - 树形视图下可展开/收缩节点，源码视图下可直接编辑结果文本；
 * - 复制按钮始终复制“当前右侧结果”，并按下拉缩进策略重排后再写入剪贴板。
 */
@Composable
fun JsonTool() {
  // 页面级状态由 ViewModel 托管，避免界面重组丢失关键数据
  val viewModel: JsonViewModel = viewModel { JsonViewModel() }
  // 用于主动清理输入焦点（点击按钮时收起光标状态）
  val focusManager = LocalFocusManager.current
  // 复制时使用的缩进选项，默认 2 空格
  val selectedIndentOption = remember { mutableStateOf(JSON_INDENT_OPTION_2_SPACES) }

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
              // 复制左侧原始输入，不做格式化处理
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
          // 输入区只承担“文本编辑”职责，不做语法上色，保证输入时光标与内容绝对一致。
          ZTextField(
            value = viewModel.inputJson,
            onValueChange = { viewModel.inputJson = it },
            type = ZTextFieldType.TEXTAREA,
            resize = false,
            modifier = Modifier.fillMaxSize()
              .onPreviewKeyEvent {
                // 统一拦截快捷键：按下 Ctrl/Cmd + Enter 等同点击“格式化”。
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
          UtilButton("Unicode 转中文") { focusManager.clearFocus(); viewModel.unicodeToChineseInput() }
          UtilButton("中文转 Unicode") { focusManager.clearFocus(); viewModel.chineseToUnicodeInput() }
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
            // 文案显示的是“下一步动作”，不是当前模式，点击意图更直接。
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

          ZDropdownMenu(
            // 复制缩进风格选择，仅影响“复制到剪贴板”的结果文本
            options = JSON_INDENT_OPTIONS,
            modifier = Modifier.width(88.dp),
            lineHeight = 20,
            fontSize = 12.sp,
            defaultSelectedOption = selectedIndentOption.value,
            placeholder = "缩进",
            onOptionSelected = { selectedIndentOption.value = it }
          )

          ZButton(
            type = ZColorType.SUCCESS, plain = true,
            icon = { Icon(FeatherIcons.Copy, null, modifier = Modifier.size(14.dp)) },
            onClick = {
              // 复制前按选中缩进重新排版，避免外部粘贴时仍是默认 Tab 风格
              val raw = viewModel.outputJson
              val indentUnit = indentUnitFromOption(selectedIndentOption.value)
              val copied = reindentJsonForCopy(raw, indentUnit, viewModel.isUnicodeDisplay)
              val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
              clipboard.setContents(java.awt.datatransfer.StringSelection(copied), null)
              ToastManager.success("已复制结果（${selectedIndentOption.value}）")
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
            // 树形模式优先使用输出文本，若输出为空则回退到输入文本，保证始终可查看结构。
            JsonTreeView(
              jsonString = viewModel.outputJson.ifEmpty { viewModel.inputJson },
              isUnicodeDisplay = viewModel.isUnicodeDisplay,
              expandState = viewModel.treeExpandState
            )
          } else {
            // 源码模式：使用视觉转换做语法着色，不改变真实文本内容
            val isDarkTheme = isAppInDarkTheme()
            val syntaxHighlight = remember(isDarkTheme) { JsonSourceSyntaxHighlighter(isDarkTheme) }
            ZTextField(
              value = viewModel.outputJson,
              onValueChange = { viewModel.outputJson = it },
              type = ZTextFieldType.TEXTAREA,
              resize = false,
              modifier = Modifier.fillMaxSize(),
              placeholder = "解析结果...",
              visualTransformation = syntaxHighlight,
              textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = if (isDarkTheme) Color(0xFFD8DEE9) else Color(0xFF2B2B2B)
              )
            )
          }
        }

        Spacer(Modifier.height(10.dp))

        // 右侧底部工具栏
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          UtilButton("Unicode 转中文") { focusManager.clearFocus(); viewModel.unicodeToChineseOutput() }
          UtilButton("中文转 Unicode") { focusManager.clearFocus(); viewModel.chineseToUnicodeOutput() }
          UtilButton("去转义") { focusManager.clearFocus(); viewModel.unescapeJsonOutput() }
          UtilButton("加转义") { focusManager.clearFocus(); viewModel.escapeJsonOutput() }
        }
      }
    }
    ToastContainer()
  }
}

/**
 * 历史记录单项卡片。
 * - 展示 JSON 的压缩预览（去空白后截断）
 * - 点击后回填到左侧输入区并触发格式化逻辑
 *
 * 额外约束：
 * - 预览最大 200 字符 + 最多 3 行，避免超长记录压缩列表可读性；
 * - 使用等宽字体，便于快速识别 JSON 结构片段。
 */
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

/**
 * 右/左侧底部的轻量工具按钮（统一尺寸与样式）。
 */
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

/**
 * JSON 源码高亮器。
 *
 * 通过 `VisualTransformation` 在显示层做着色：
 * - 不修改真实文本
 * - 光标/选择位置保持一一对应（`OffsetMapping.Identity`）
 */
private class JsonSourceSyntaxHighlighter(
  private val isDarkTheme: Boolean
) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    return TransformedText(
      text = highlightJson(text.text, isDarkTheme),
      offsetMapping = OffsetMapping.Identity
    )
  }
}

/**
 * 对 JSON 文本做词法级着色。
 * 规则：
 * - 键名：紫色
 * - 字符串：绿色
 * - 数字：蓝色
 * - 布尔值：橙色
 * - null：浅黄色
 *
 * 说明：
 * - 这是“近似词法高亮”，不做完整语法树校验；
 * - 目标是提升可读性，而不是替代严格 JSON 校验；
 * - 即使输入不合法，也尽量给出稳定高亮，不中断编辑。
 */
private fun highlightJson(raw: String, isDarkTheme: Boolean): AnnotatedString {
  if (raw.isEmpty()) return AnnotatedString("")

  val defaultColor = if (isDarkTheme) Color(0xFFD8DEE9) else Color(0xFF2B2B2B)
  val keyColor = Color(0xFF9876AA)
  val stringColor = if (isDarkTheme) Color(0xFFA5C261) else Color(0xFF6A8759)
  val numberColor = Color(0xFF6897BB)
  val booleanColor = Color(0xFFCC7832)
  val nullColor = Color(0xFFFFC66D)

  val builder = AnnotatedString.Builder(raw)
  builder.addStyle(SpanStyle(color = defaultColor), 0, raw.length)

  var index = 0
  while (index < raw.length) {
    when {
      raw[index] == '"' -> {
        val end = findStringTokenEnd(raw, index)
        val color = if (isJsonKey(raw, end)) keyColor else stringColor
        builder.addStyle(SpanStyle(color = color), index, end)
        index = end
      }
      raw[index] == '-' || raw[index].isDigit() -> {
        val end = findNumberTokenEnd(raw, index)
        if (end > index) {
          builder.addStyle(SpanStyle(color = numberColor), index, end)
          index = end
        } else {
          index++
        }
      }
      raw.startsWith("true", index) && isKeywordBoundary(raw, index, 4) -> {
        builder.addStyle(SpanStyle(color = booleanColor), index, index + 4)
        index += 4
      }
      raw.startsWith("false", index) && isKeywordBoundary(raw, index, 5) -> {
        builder.addStyle(SpanStyle(color = booleanColor), index, index + 5)
        index += 5
      }
      raw.startsWith("null", index) && isKeywordBoundary(raw, index, 4) -> {
        builder.addStyle(SpanStyle(color = nullColor), index, index + 4)
        index += 4
      }
      else -> index++
    }
  }

  return builder.toAnnotatedString()
}

/**
 * 从给定双引号起点开始，找到字符串 token 的结束位置（不含越界）。
 * 会正确跳过转义字符（例如 `\"`、`\\`）。
 *
 * 该函数是高亮正确性的关键：
 * - 若不跳过转义字符，`\"` 会被误判为字符串结束，导致后续着色整体错位。
 */
private fun findStringTokenEnd(raw: String, start: Int): Int {
  var i = start + 1
  while (i < raw.length) {
    val ch = raw[i]
    if (ch == '\\') {
      i += if (i + 1 < raw.length) 2 else 1
      continue
    }
    if (ch == '"') {
      return i + 1
    }
    i++
  }
  return raw.length
}

/**
 * 判断刚结束的字符串 token 是否为“对象键名”：
 * - 跳过后续空白
 * - 紧随冒号 `:`
 */
private fun isJsonKey(raw: String, tokenEndExclusive: Int): Boolean {
  var i = tokenEndExclusive
  while (i < raw.length && raw[i].isWhitespace()) {
    i++
  }
  return i < raw.length && raw[i] == ':'
}

/**
 * 尝试识别数字 token 的结束位置。
 * 支持：
 * - 负号
 * - 整数/小数
 * - 科学计数法（`e`/`E`）
 *
 * 返回约定：
 * - 返回值 > start：识别成功，返回 token 结束下标（exclusive）；
 * - 返回值 == start：识别失败，由调用方按普通字符处理。
 */
private fun findNumberTokenEnd(raw: String, start: Int): Int {
  var i = start
  if (raw[i] == '-') i++
  if (i >= raw.length) return start

  if (raw[i] == '0') {
    i++
  } else if (raw[i].isDigit()) {
    while (i < raw.length && raw[i].isDigit()) {
      i++
    }
  } else {
    return start
  }

  if (i < raw.length && raw[i] == '.') {
    val dotIndex = i
    i++
    if (i >= raw.length || !raw[i].isDigit()) return dotIndex
    while (i < raw.length && raw[i].isDigit()) {
      i++
    }
  }

  if (i < raw.length && (raw[i] == 'e' || raw[i] == 'E')) {
    val expIndex = i
    i++
    if (i < raw.length && (raw[i] == '+' || raw[i] == '-')) {
      i++
    }
    if (i >= raw.length || !raw[i].isDigit()) return expIndex
    while (i < raw.length && raw[i].isDigit()) {
      i++
    }
  }

  return i
}

/**
 * 关键字（true/false/null）边界判断。
 * 避免把 `trueValue` 这种字符串前缀误高亮为关键字。
 *
 * 示例：
 * - `true,` 视为关键字；
 * - `xtrue` 不视为关键字（前边界不成立）；
 * - `trueValue` 不视为关键字（后边界不成立）。
 */
private fun isKeywordBoundary(raw: String, start: Int, tokenLength: Int): Boolean {
  val prevChar = if (start > 0) raw[start - 1] else null
  val nextIndex = start + tokenLength
  val nextChar = if (nextIndex < raw.length) raw[nextIndex] else null
  return isTokenBoundary(prevChar) && isTokenBoundary(nextChar)
}

/**
 * 判断字符是否属于 token 分隔边界。
 */
private fun isTokenBoundary(ch: Char?): Boolean {
  if (ch == null) return true
  return ch.isWhitespace() || ch == ',' || ch == ':' || ch == '{' || ch == '}' || ch == '[' || ch == ']'
}

/**
 * 复制缩进选项（右侧复制按钮左侧下拉）。
 *
 * 注意：这组常量只影响“复制动作”的文本重排，不会直接改动右侧编辑器中的显示内容。
 */
private const val JSON_INDENT_OPTION_2_SPACES = "2 空格"
private const val JSON_INDENT_OPTION_4_SPACES = "4 空格"
private const val JSON_INDENT_OPTION_1_TAB = "1 Tab"

private val JSON_INDENT_OPTIONS = listOf(
  JSON_INDENT_OPTION_2_SPACES,
  JSON_INDENT_OPTION_4_SPACES,
  JSON_INDENT_OPTION_1_TAB
)

/**
 * 将下拉选项映射为实际缩进字符串。
 */
private fun indentUnitFromOption(option: String): String {
  return when (option) {
    JSON_INDENT_OPTION_4_SPACES -> "    "
    JSON_INDENT_OPTION_1_TAB -> "\t"
    else -> "  "
  }
}

/**
 * 复制前重排 JSON 缩进。
 * - 解析失败时兜底返回原文本，避免复制动作中断。
 * - 当 `forceUnicodeEscaped` 为 true 时，会把非 ASCII 字符转为 `\uXXXX`。
 *
 * 这样设计的目的：
 * - 不污染编辑区内容（用户仍可按自己习惯编辑）；
 * - 复制到外部系统时再统一风格，兼顾“编辑自由”和“输出规范”。
 */
private fun reindentJsonForCopy(raw: String, indentUnit: String, forceUnicodeEscaped: Boolean): String {
  if (raw.isBlank()) return raw
  val pretty = runCatching {
    val parsed = JsonUtil.parse(raw)
    prettyJsonValue(parsed, indentUnit, 0)
  }.getOrElse { raw }
  return if (forceUnicodeEscaped) toUnicodeEscapedText(pretty) else pretty
}

/**
 * 把文本里的非 ASCII 字符转换为 `\uXXXX` 形式。
 */
private fun toUnicodeEscapedText(raw: String): String {
  if (raw.isEmpty()) return raw
  val sb = StringBuilder(raw.length)
  raw.forEach { ch ->
    if (ch.code in 128..65535) {
      sb.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
    } else {
      sb.append(ch)
    }
  }
  return sb.toString()
}

/**
 * 递归渲染任意 JSON 值为带指定缩进的文本。
 *
 * 与直接 `JsonUtil.format` 的区别：
 * - 这里支持“自定义缩进字符串”（例如 1 Tab）；
 * - 用于复制链路，避免影响页面现有显示。
 */
private fun prettyJsonValue(value: Any?, indentUnit: String, depth: Int): String {
  return when (value) {
    is Map<*, *> -> prettyJsonObject(value, indentUnit, depth)
    is List<*> -> prettyJsonArray(value, indentUnit, depth)
    is String -> JsonUtil.toJsonNoFeature(value)
    is Number, is Boolean -> value.toString()
    null -> "null"
    else -> JsonUtil.toJsonNoFeature(value)
  }
}

/**
 * 按指定缩进渲染 JSON 对象。
 */
private fun prettyJsonObject(value: Map<*, *>, indentUnit: String, depth: Int): String {
  if (value.isEmpty()) return "{}"
  val indent = indentUnit.repeat(depth)
  val childIndent = indentUnit.repeat(depth + 1)
  val items = value.entries.joinToString(",\n") { entry ->
    val key = JsonUtil.toJsonNoFeature(entry.key?.toString() ?: "null")
    val rendered = prettyJsonValue(entry.value, indentUnit, depth + 1)
    "$childIndent$key: $rendered"
  }
  return "{\n$items\n$indent}"
}

/**
 * 按指定缩进渲染 JSON 数组。
 */
private fun prettyJsonArray(value: List<*>, indentUnit: String, depth: Int): String {
  if (value.isEmpty()) return "[]"
  val indent = indentUnit.repeat(depth)
  val childIndent = indentUnit.repeat(depth + 1)
  val items = value.joinToString(",\n") { item ->
    val rendered = prettyJsonValue(item, indentUnit, depth + 1)
    "$childIndent$rendered"
  }
  return "[\n$items\n$indent]"
}
