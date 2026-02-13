package top.zhjh.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
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
import top.zhjh.zui.enums.ZColorType
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.ArrayDeque
import java.util.Locale

// 配色常量
private val ColorKey = Color(0xFF9876AA)
private val ColorString = Color(0xFF6A8759)
private val ColorNumber = Color(0xFF6897BB)
private val ColorBoolean = Color(0xFFCC7832)
private val ColorNull = Color(0xFFFFC66D)

private const val LARGE_JSON_THRESHOLD = 500_000
private const val MAX_TREE_DEPTH = 200
private const val INDENT_STEP_DP = 20

private data class ParsedJson(
  val root: Any?,
  val isEscapedView: Boolean
)

private sealed class ParseState {
  object Loading : ParseState()
  data class Ready(val data: ParsedJson) : ParseState()
  data class Error(val message: String) : ParseState()
}

private sealed class TreeRow {
  data class Node(
    val path: String,
    val depth: Int,
    val key: String?,
    val value: Any?,
    val isContainer: Boolean,
    val containerSize: Int,
    val isExpanded: Boolean
  ) : TreeRow()

  data class DepthLimit(val depth: Int) : TreeRow()
}

private data class NodeFrame(
  val path: String,
  val depth: Int,
  val key: String?,
  val value: Any?
)

@Composable
fun JsonTreeView(
  jsonString: String,
  isUnicodeDisplay: Boolean = false,
  expandState: Int = 0 // 0: 无动作, 1: 展开所有, 2: 收缩所有
) {
  val isLargeJson = jsonString.length >= LARGE_JSON_THRESHOLD
  var allowLarge by remember(jsonString) { mutableStateOf(false) }
  val expandMap = remember { mutableStateMapOf<String, Boolean>() }
  var lastExpandSignal by remember { mutableStateOf(0) }

  LaunchedEffect(jsonString) {
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
      Text("Large JSON detected ($sizeLabel).")
      Text("Tree view may be slow. Consider using source view.")
      Spacer(Modifier.height(8.dp))
      ZButton(type = ZColorType.WARNING, onClick = { allowLarge = true }) {
        Text("Load tree")
      }
    }
    return
  }

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
        Text("Parsing JSON...")
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(modifier = Modifier.width(180.dp))
      }
      return
    }

    is ParseState.Error -> {
      Text(state.message, color = Color.Red, modifier = Modifier.padding(10.dp))
      return
    }

    is ParseState.Ready -> {
      val finalRoot = state.data.root
      val isEscapedView = state.data.isEscapedView
      val listState = rememberLazyListState()

      LaunchedEffect(finalRoot, expandState) {
        if (expandState != 0 && expandState != lastExpandSignal) {
          val targetExpanded = expandState == 1
          val containerPaths = collectContainerPaths(finalRoot)
          Snapshot.withMutableSnapshot {
            expandMap.clear()
            containerPaths.forEach { expandMap[it] = targetExpanded }
          }
          lastExpandSignal = expandState
        }
      }

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

private fun tryParse(raw: String): ParseAttempt {
  return try {
    ParseAttempt(JsonUtil.parse(raw), true)
  } catch (e: Exception) {
    ParseAttempt(null, false)
  }
}

private fun formatSize(chars: Int): String {
  val kb = chars / 1024.0
  return if (kb < 1024.0) {
    String.format(Locale.US, "%.1f KB", kb)
  } else {
    String.format(Locale.US, "%.2f MB", kb / 1024.0)
  }
}

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
  val indent = (row.depth * INDENT_STEP_DP).dp
  val isContainer = row.isContainer
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
      Text(
        text = finalKey,
        color = ColorKey,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp
      )
    }

    if (isContainer) {
      if (!row.isExpanded) {
        val preview = when {
          row.containerSize >= 0 && row.key != null -> when (row.value) {
            is Map<*, *> -> "{ ${row.containerSize} fields }"
            is List<*> -> "[ ${row.containerSize} items ]"
            else -> ""
          }
          else -> ""
        }
        Text(text = preview, color = Color.Gray, fontSize = 12.sp)
      }
    } else {
      JsonValueText(row.value, isUnicodeDisplay, isEscapedView)
    }
  }
}

@Composable
private fun DepthLimitRow(depth: Int) {
  val indent = (depth * INDENT_STEP_DP).dp
  Text(
    text = "... depth limit reached ...",
    color = Color.Gray,
    fontSize = 12.sp,
    modifier = Modifier.padding(start = indent, top = 2.dp, bottom = 2.dp)
  )
}

@Composable
fun JsonValueText(value: Any?, isUnicodeDisplay: Boolean, isEscapedView: Boolean) {
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

  Text(
    text = text,
    color = color,
    fontFamily = FontFamily.Monospace,
    fontSize = 13.sp,
    modifier = Modifier.clickable {
      val clipboard = Toolkit.getDefaultToolkit().systemClipboard
      clipboard.setContents(StringSelection(text), null)
      ToastManager.success("已复制值")
    }
  )
}
