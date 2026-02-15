package top.zhjh.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronRight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.csaf.json.JsonUtil
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZText
import top.zhjh.zui.enums.ZColorType
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.ArrayDeque
import java.util.Locale

/**
 * 树形 JSON 视图配色常量。
 *
 * 颜色语义与源码高亮保持一致，降低用户在“源码视图 <-> 树形视图”之间切换的认知成本。
 */
private val ColorKey = Color(0xFF9876AA)
private val ColorString = Color(0xFF6A8759)
private val ColorNumber = Color(0xFF6897BB)
private val ColorBoolean = Color(0xFFCC7832)
private val ColorNull = Color(0xFFFFC66D)

/** 大文本阈值（字符数），超过后先弹出性能提示而不是直接渲染树。 */
private const val LARGE_JSON_THRESHOLD = 500_000
/** 树最大展开深度，防止极端嵌套数据导致 UI 卡顿或栈/内存压力。 */
private const val MAX_TREE_DEPTH = 200
/** 每一级缩进宽度。 */
private const val INDENT_STEP_DP = 20

/**
 * 解析成功后的中间数据。
 *
 * @property root 最终用于渲染树的根节点（Map/List/基础值）。
 * @property isEscapedView 是否来自“转义 JSON 字符串”二次解析。
 */
private data class ParsedJson(
  val root: Any?,
  val isEscapedView: Boolean
)

/**
 * 异步解析状态。
 *
 * - Loading：后台解析中；
 * - Ready：解析完成并可渲染；
 * - Error：解析失败，展示错误提示。
 */
private sealed class ParseState {
  object Loading : ParseState()
  data class Ready(val data: ParsedJson) : ParseState()
  data class Error(val message: String) : ParseState()
}

/**
 * 树列表行模型。
 *
 * 设计原因：
 * - `LazyColumn` 渲染的是“扁平行”，不是直接渲染递归节点；
 * - 将树预先“拍平”为行，有利于虚拟化和点击处理。
 */
private sealed class TreeRow {
  /**
   * 普通节点行（对象字段、数组元素、或基础类型）。
   */
  data class Node(
    val path: String,
    val depth: Int,
    val key: String?,
    val value: Any?,
    val isContainer: Boolean,
    val containerSize: Int,
    val isExpanded: Boolean
  ) : TreeRow()

  /**
   * 深度限制提示行。
   * 当展开到 [MAX_TREE_DEPTH] 后插入，用于明确告知用户“并非数据结束，而是保护性截断”。
   */
  data class DepthLimit(val depth: Int) : TreeRow()
}

/**
 * 非递归深度优先遍历时使用的栈帧。
 *
 * `path` 作为稳定键用于：
 * - 记录展开/收缩状态；
 * - 批量展开时快速命中。
 */
private data class NodeFrame(
  val path: String,
  val depth: Int,
  val key: String?,
  val value: Any?
)

/**
 * JSON 树形视图。
 *
 * 核心能力：
 * 1. 支持普通 JSON 与“转义 JSON 字符串”自动识别；
 * 2. 支持节点展开/收缩、全展开/全收缩；
 * 3. 支持值点击复制、键名点击复制；
 * 4. 对超大文本先做风险提示，避免直接卡住 UI。
 *
 * @param jsonString 输入 JSON 文本（通常来自右侧输出或左侧原文兜底）。
 * @param isUnicodeDisplay 是否强制把字符串值显示为 Unicode 转义形式。
 * @param expandState 外部发来的展开指令：0 无动作，1 展开全部，2 收缩全部。
 */
@Composable
fun JsonTreeView(
  jsonString: String,
  isUnicodeDisplay: Boolean = false,
  expandState: Int = 0 // 0: 无动作, 1: 展开所有, 2: 收缩所有
) {
  // 大文本保护：先提示风险，避免直接进入解析+树构建导致卡顿。
  val isLargeJson = jsonString.length >= LARGE_JSON_THRESHOLD
  // 每次 jsonString 变化都重置“是否继续加载大文件”的确认状态。
  var allowLarge by remember(jsonString) { mutableStateOf(false) }
  // 路径 -> 展开态。使用 mutableStateMap 让单节点切换也能触发列表重组。
  val expandMap = remember { mutableStateMapOf<String, Boolean>() }
  // 记录上次已消费的 expandState，避免同一个外部指令被重复应用。
  var lastExpandSignal by remember { mutableStateOf(0) }

  LaunchedEffect(jsonString) {
    // 输入文本变化后，旧树的展开态不再可靠，全部清空重建。
    expandMap.clear()
    lastExpandSignal = 0
  }

  if (isLargeJson && !allowLarge) {
    val sizeLabel = formatSize(jsonString.length)
    Column(
      modifier = Modifier.fillMaxSize().padding(12.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      ZText("Large JSON detected ($sizeLabel).")
      ZText("Tree view may be slow. Consider using source view.")
      Spacer(Modifier.height(8.dp))
      ZButton(type = ZColorType.WARNING, onClick = { allowLarge = true }) {
        ZText("Load tree")
      }
    }
    return
  }

  // 解析放到后台线程，避免阻塞主线程绘制与交互。
  val parseState by produceState<ParseState>(initialValue = ParseState.Loading, jsonString) {
    value = ParseState.Loading
    value = withContext(Dispatchers.Default) { parseJsonForTree(jsonString) }
  }

  when (val state = parseState) {
    ParseState.Loading -> {
      Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        ZText("Parsing JSON...")
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(modifier = Modifier.width(180.dp))
      }
      return
    }

    is ParseState.Error -> {
      ZText(state.message, color = Color.Red, modifier = Modifier.padding(10.dp))
      return
    }

    is ParseState.Ready -> {
      val finalRoot = state.data.root
      val isEscapedView = state.data.isEscapedView
      val listState = rememberLazyListState()

      LaunchedEffect(finalRoot, expandState) {
        // 处理外部“全展开/全收缩”指令。
        if (expandState != 0 && expandState != lastExpandSignal) {
          val targetExpanded = expandState == 1
          val containerPaths = collectContainerPaths(finalRoot)
          Snapshot.withMutableSnapshot {
            // 批量更新快照，降低多次写入带来的重组抖动。
            expandMap.clear()
            containerPaths.forEach { expandMap[it] = targetExpanded }
          }
          lastExpandSignal = expandState
        }
      }

      // 派生状态：仅当 root 或 expandMap 变化时重建可见行。
      val rows by remember(finalRoot, expandMap) {
        derivedStateOf { buildVisibleRows(finalRoot, expandMap) }
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        state = listState
      ) {
        items(rows) { row ->
          when (row) {
            is TreeRow.Node -> {
              JsonTreeRow(
                row = row,
                isEscapedView = isEscapedView,
                isUnicodeDisplay = isUnicodeDisplay,
                onToggle = { path ->
                  val current = expandMap[path] ?: false
                  expandMap[path] = !current
                }
              )
            }

            is TreeRow.DepthLimit -> {
              DepthLimitRow(row.depth)
            }
          }
        }
      }
    }
  }
}

private data class ParseAttempt(
  val value: Any?,
  val success: Boolean
)

/**
 * 解析树视图输入。
 *
 * 兜底顺序：
 * 1. 直接按 JSON 解析；
 * 2. 若失败，尝试把整段文本包成 JSON 字符串再解析（兼容带转义内容）；
 * 3. 若结果是字符串且内容看起来像对象/数组，再做一层深解析。
 *
 * 返回 `Ready` 时会带上 `isEscapedView`，用于渲染时决定字符串是否显示额外转义引号。
 */
private fun parseJsonForTree(raw: String): ParseState {
  var isEscapedView = false
  var parsedSuccess = false
  var root: Any? = null

  val first = tryParse(raw)
  if (first.success) {
    parsedSuccess = true
    root = first.value
  } else {
    val wrapped = tryParse("\"$raw\"")
    if (wrapped.success && wrapped.value is String) {
      val deep = tryParse(wrapped.value)
      if (deep.success) {
        parsedSuccess = true
        isEscapedView = true
        root = deep.value
      }
    }
  }

  if (root is String) {
    val trimmed = root.trim()
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
      val deep = tryParse(trimmed)
      if (deep.success) {
        parsedSuccess = true
        isEscapedView = true
        root = deep.value
      }
    }
  }

  return if (!parsedSuccess) {
    ParseState.Error("Invalid JSON for Tree View")
  } else {
    ParseState.Ready(ParsedJson(root, isEscapedView))
  }
}

/**
 * 解析尝试包装器。
 * 捕获解析异常并返回成功标记，减少上层 try/catch 嵌套。
 */
private fun tryParse(raw: String): ParseAttempt {
  return try {
    ParseAttempt(JsonUtil.parse(raw), true)
  } catch (e: Exception) {
    ParseAttempt(null, false)
  }
}

/**
 * 将字符长度格式化为易读尺寸文案（KB/MB）。
 * 用于大文本提示。
 */
private fun formatSize(chars: Int): String {
  val kb = chars / 1024.0
  return if (kb < 1024.0) {
    String.format(Locale.US, "%.1f KB", kb)
  } else {
    String.format(Locale.US, "%.2f MB", kb / 1024.0)
  }
}

/**
 * 把树结构拍平成可供 `LazyColumn` 渲染的行列表。
 *
 * 算法要点：
 * - 使用显式栈 `ArrayDeque` 做 DFS，避免递归层级过深；
 * - 子节点倒序入栈，保证出栈后的视觉顺序仍是原始顺序；
 * - 仅当节点“是容器且处于展开态”时才推入其子节点。
 */
private fun buildVisibleRows(root: Any?, expandMap: Map<String, Boolean>): List<TreeRow> {
  val rows = mutableListOf<TreeRow>()
  val stack = ArrayDeque<NodeFrame>()

  fun pushChildren(container: Any?, parentPath: String, depth: Int) {
    when (container) {
      is Map<*, *> -> {
        val entries = container.entries.toList()
        for (i in entries.indices.reversed()) {
          val entry = entries[i]
          val key = entry.key?.toString() ?: "null"
          stack.addLast(
            NodeFrame(
              path = buildChildPath(parentPath, key),
              depth = depth,
              key = key,
              value = entry.value
            )
          )
        }
      }
      is List<*> -> {
        for (i in container.indices.reversed()) {
          val key = i.toString()
          stack.addLast(
            NodeFrame(
              path = buildChildPath(parentPath, key),
              depth = depth,
              key = key,
              value = container[i]
            )
          )
        }
      }
    }
  }

  when (root) {
    is Map<*, *> -> pushChildren(root, "", 0)
    is List<*> -> pushChildren(root, "", 0)
    else -> {
      rows.add(
        TreeRow.Node(
          path = "",
          depth = 0,
          key = null,
          value = root,
          isContainer = false,
          containerSize = -1,
          isExpanded = false
        )
      )
      return rows
    }
  }

  while (stack.isNotEmpty()) {
    val frame = stack.removeLast()
    val value = frame.value
    val isContainer = value is Map<*, *> || value is List<*>
    val containerSize = when (value) {
      is Map<*, *> -> value.size
      is List<*> -> value.size
      else -> -1
    }
    val isExpanded = if (isContainer) expandMap[frame.path] ?: false else false

    rows.add(
      TreeRow.Node(
        path = frame.path,
        depth = frame.depth,
        key = frame.key,
        value = value,
        isContainer = isContainer,
        containerSize = containerSize,
        isExpanded = isExpanded
      )
    )

    if (isContainer && isExpanded) {
      if (frame.depth >= MAX_TREE_DEPTH) {
        rows.add(TreeRow.DepthLimit(frame.depth + 1))
      } else {
        pushChildren(value, frame.path, frame.depth + 1)
      }
    }
  }

  return rows
}

/**
 * 收集所有容器节点路径。
 *
 * 主要用于“展开全部/收缩全部”时一次性构建目标路径集，
 * 避免在 UI 层逐行触发多次状态更新。
 */
private fun collectContainerPaths(root: Any?): List<String> {
  val paths = mutableListOf<String>()
  val stack = ArrayDeque<NodeFrame>()

  fun pushChildren(container: Any?, parentPath: String, depth: Int) {
    when (container) {
      is Map<*, *> -> {
        val entries = container.entries.toList()
        for (i in entries.indices.reversed()) {
          val entry = entries[i]
          val key = entry.key?.toString() ?: "null"
          stack.addLast(
            NodeFrame(
              path = buildChildPath(parentPath, key),
              depth = depth,
              key = key,
              value = entry.value
            )
          )
        }
      }
      is List<*> -> {
        for (i in container.indices.reversed()) {
          val key = i.toString()
          stack.addLast(
            NodeFrame(
              path = buildChildPath(parentPath, key),
              depth = depth,
              key = key,
              value = container[i]
            )
          )
        }
      }
    }
  }

  when (root) {
    is Map<*, *> -> pushChildren(root, "", 0)
    is List<*> -> pushChildren(root, "", 0)
    else -> return paths
  }

  while (stack.isNotEmpty()) {
    val frame = stack.removeLast()
    val value = frame.value
    val isContainer = value is Map<*, *> || value is List<*>
    if (isContainer) {
      paths.add(frame.path)
      if (frame.depth < MAX_TREE_DEPTH) {
        pushChildren(value, frame.path, frame.depth + 1)
      }
    }
  }

  return paths
}

/**
 * 生成子节点路径。
 * 采用 `/` 分隔的轻量路径方案，例如：`user/address/city`、`items/0/id`。
 */
private fun buildChildPath(parent: String, child: String): String {
  return if (parent.isEmpty()) child else "$parent/$child"
}

@Composable
private fun JsonTreeRow(
  row: TreeRow.Node,
  isEscapedView: Boolean,
  isUnicodeDisplay: Boolean,
  onToggle: (String) -> Unit
) {
  // 缩进由深度决定，保持视觉层级。
  val indent = (row.depth * INDENT_STEP_DP).dp
  val isContainer = row.isContainer
  // 转义视图下，key 也按“被转义字符串”的视觉格式渲染。
  val finalKey = if (row.key != null) {
    if (isEscapedView) "\"\\\"${row.key}\\\"\": " else "\"${row.key}\": "
  } else ""

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { if (isContainer) onToggle(row.path) }
      .padding(start = indent, top = 2.dp, bottom = 2.dp)
  ) {
    if (isContainer) {
      Icon(
        imageVector = if (row.isExpanded) FeatherIcons.ChevronDown else FeatherIcons.ChevronRight,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = Color.Gray
      )
    } else {
      Spacer(modifier = Modifier.size(16.dp))
    }

    if (finalKey.isNotEmpty()) {
      ZText(
        text = finalKey,
        color = ColorKey,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        modifier = Modifier.clickable {
          // 只复制 key 本身，不包含引号与冒号
          row.key?.let { copyToClipboard(it, "已复制 Key") }
        }
      )
    }

    if (isContainer) {
      // 折叠态显示简要预览，避免空行且帮助判断节点规模。
      if (!row.isExpanded) {
        val preview = when {
          row.containerSize >= 0 && row.key != null -> when (row.value) {
            is Map<*, *> -> "{ ${row.containerSize} fields }"
            is List<*> -> "[ ${row.containerSize} items ]"
            else -> ""
          }
          else -> ""
        }
        ZText( preview, color = Color.Gray, fontSize = 12.sp)
      }
    } else {
      JsonValueText(row.value, isUnicodeDisplay, isEscapedView)
    }
  }
}

/**
 * 深度保护提示行。
 * 当树过深时明确提示用户当前是“保护性截断”，不是数据缺失。
 */
@Composable
private fun DepthLimitRow(depth: Int) {
  val indent = (depth * INDENT_STEP_DP).dp
  ZText(
    text = "... depth limit reached ...",
    color = Color.Gray,
    fontSize = 12.sp,
    modifier = Modifier.padding(start = indent, top = 2.dp, bottom = 2.dp)
  )
}

/**
 * 渲染并着色基础值节点（字符串/数字/布尔/null）。
 *
 * 交互：
 * - 点击值可直接复制展示文本（包含字符串引号）。
 */
@Composable
fun JsonValueText(value: Any?, isUnicodeDisplay: Boolean, isEscapedView: Boolean) {
  // 仅对字符串执行 Unicode 视觉转换，非字符串保持原值。
  val displayValue = if (isUnicodeDisplay && value is String) {
    value.map { char ->
      if (char.code in 128..65535) "\\u${char.code.toString(16).padStart(4, '0')}" else char
    }.joinToString("")
  } else {
    value
  }

  val quote = if (isEscapedView) "\\\"" else "\""

  val (text, color) = when (value) {
    is String -> "$quote$displayValue$quote" to ColorString
    is Number -> displayValue.toString() to ColorNumber
    is Boolean -> displayValue.toString() to ColorBoolean
    null -> "null" to ColorNull
    else -> displayValue.toString() to Color.Gray
  }

  ZText(
    text = text,
    color = color,
    fontFamily = FontFamily.Monospace,
    fontSize = 13.sp,
    modifier = Modifier.clickable {
      copyToClipboard(text, "已复制值")
    }
  )
}

/**
 * 统一剪贴板复制入口。
 * 集中维护成功提示文案，避免分散在多处点击回调里重复实现。
 */
private fun copyToClipboard(content: String, successText: String) {
  val clipboard = Toolkit.getDefaultToolkit().systemClipboard
  clipboard.setContents(StringSelection(content), null)
  ToastManager.success(successText)
}
