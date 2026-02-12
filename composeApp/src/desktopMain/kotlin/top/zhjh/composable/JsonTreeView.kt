package top.zhjh.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import java.util.Locale

// 配色常量
private val ColorKey = Color(0xFF9876AA)
private val ColorString = Color(0xFF6A8759)
private val ColorNumber = Color(0xFF6897BB)
private val ColorBoolean = Color(0xFFCC7832)
private val ColorNull = Color(0xFFFFC66D)

private const val LARGE_JSON_THRESHOLD = 500_000

private data class ParsedJson(
  val root: Any?,
  val isEscapedView: Boolean
)

private sealed class ParseState {
  object Loading : ParseState()
  data class Ready(val data: ParsedJson) : ParseState()
  data class Error(val message: String) : ParseState()
}

@Composable
fun JsonTreeView(
  jsonString: String,
  isUnicodeDisplay: Boolean = false,
  expandState: Int = 0 // 0: 无动作, 1: 展开所有, 2: 收缩所有
) {
  val isLargeJson = jsonString.length >= LARGE_JSON_THRESHOLD
  var allowLarge by remember(jsonString) { mutableStateOf(false) }

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

      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        state = rememberLazyListState()
      ) {
        // 遍历顶层内容
        item {
          if (finalRoot is Map<*, *>) {
            Column(modifier = Modifier.padding(start = 0.dp)) {
              for ((k, v) in finalRoot) {
                JsonNode(k.toString(), v, false, isEscapedView, isUnicodeDisplay, expandState)
              }
            }
          } else if (finalRoot is List<*>) {
            Column(modifier = Modifier.padding(start = 0.dp)) {
              for (i in 0 until finalRoot.size) {
                JsonNode("$i", finalRoot[i], false, isEscapedView, isUnicodeDisplay, expandState)
              }
            }
          } else {
            JsonValueText(finalRoot, isUnicodeDisplay, isEscapedView)
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

@Composable
fun JsonNode(
  key: String?,
  value: Any?,
  isRoot: Boolean = false,
  isEscapedView: Boolean,
  isUnicodeDisplay: Boolean,
  expandState: Int // 传入展开状态
) {
  // 默认不展开
  var isExpanded by remember { mutableStateOf(isRoot) }

  // 监听外部展开/收缩信号
  LaunchedEffect(expandState) {
    if (expandState == 1) isExpanded = true
    if (expandState == 2) isExpanded = false
  }

  val isContainer = value is Map<*, *> || value is List<*>

  val finalKey = if (key != null) {
    if (isEscapedView && !isRoot) "\"\\\"$key\\\"\": " else "\"$key\": "
  } else ""

  Column {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clickable { if (isContainer) isExpanded = !isExpanded }
        .padding(vertical = 2.dp)
    ) {
      if (isContainer) {
        Icon(
          imageVector = if (isExpanded) FeatherIcons.ChevronDown else FeatherIcons.ChevronRight,
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
        if (!isExpanded) {
          val preview = when (value) {
            is Map<*, *> -> "{ ${value.size} fields }"
            is List<*> -> "[ ${value.size} items ]"
            else -> ""
          }
          Text(text = preview, color = Color.Gray, fontSize = 12.sp)
        }
      } else {
        JsonValueText(value, isUnicodeDisplay, isEscapedView)
      }
    }

    if (isContainer && isExpanded) {
      Column(modifier = Modifier.padding(start = 20.dp)) {
        if (value is Map<*, *>) {
          for ((k, v) in value) {
            JsonNode(k.toString(), v, false, isEscapedView, isUnicodeDisplay, expandState)
          }
        } else if (value is List<*>) {
          for (i in 0 until value.size) {
            JsonNode("$i", value[i], false, isEscapedView, isUnicodeDisplay, expandState)
          }
        }
      }
    }
  }
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
