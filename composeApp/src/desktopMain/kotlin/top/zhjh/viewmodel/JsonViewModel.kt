package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import top.csaf.json.JsonUtil
import top.zhjh.common.composable.ToastManager

class JsonViewModel : ViewModel() {

  // ==================== 状态 ====================
  var inputJson by mutableStateOf("")
  var outputJson by mutableStateOf("")
  var isTreeMode by mutableStateOf(false)

  var isUnicodeDisplay by mutableStateOf(false)

  // 0: 无动作, 1: 展开所有, 2: 收缩所有 (保留一级)
  var treeExpandState by mutableStateOf(0)

  // 历史记录
  val historyList = mutableStateListOf<String>()
  var errorMessage by mutableStateOf<String?>(null)

  // ==================== 核心功能 ====================

  /** 格式化 */
  fun formatJson(saveToHistory: Boolean = true) {
    if (inputJson.isBlank()) return
    try {
      outputJson = JsonUtil.format(inputJson)
      errorMessage = null
      isUnicodeDisplay = false
      if (saveToHistory) {
        addToHistory(inputJson)
      }
    } catch (e: Exception) {
      errorMessage = "Format Error: ${simplifyErrorMessage(e)}"
      outputJson = inputJson
    }
  }

  /** 将右侧内容覆盖到左侧 */
  fun overwriteLeft() {
    if (outputJson.isBlank()) return
    inputJson = outputJson
    ToastManager.success("已覆盖到左侧")
  }

  /** 智能压缩 */
  fun compressJson() {
    if (inputJson.isBlank()) {
      ToastManager.show("请先输入内容")
      return
    }

    try {
      outputJson = JsonUtil.minify(inputJson)
      errorMessage = null
      isUnicodeDisplay = false
      addToHistory(inputJson)
      ToastManager.success("压缩成功")
      return
    } catch (e: Exception) {
      // 忽略，尝试方案2
    }

    try {
      val raw = if (inputJson.startsWith("\"")) inputJson else "\"$inputJson\""
      val unescaped = JsonUtil.parseObject(raw, String::class.java)
      val minified = JsonUtil.minify(unescaped)
      var reEscaped = JsonUtil.toJsonNoFeature(minified)
      if (reEscaped.length >= 2) reEscaped = reEscaped.substring(1, reEscaped.length - 1)

      outputJson = reEscaped
      errorMessage = null
      isUnicodeDisplay = false
      addToHistory(inputJson)
      ToastManager.success("压缩成功 (转义模式)")
    } catch (e: Exception) {
      errorMessage = "Compress Error: ${simplifyErrorMessage(e)}"
    }
  }

  fun loadDemo() {
    inputJson = """
      {
        "project": "ZUtil",
        "version": 1.0,
        "features": ["JSON", "Fastjson2", "Compose"],
        "author": { "name": "duanluan" },
        "description_cn": "点击【格式化】见证奇迹！",
        'description_en': "Click 'Format' to see magic!"
      }
    """.trimIndent()
    formatJson()
  }

  // ==================== 通用逻辑 ====================

  private fun transformInput(needSync: Boolean = true, action: (String) -> String) {
    if (inputJson.isBlank()) {
      ToastManager.show("请先输入内容")
      return
    }
    try {
      val result = action(inputJson)
      if (result == inputJson) {
        ToastManager.show("内容无变化")
        return
      }
      addToHistory(inputJson)
      inputJson = result

      if (needSync) {
        try {
          outputJson = JsonUtil.format(inputJson)
          errorMessage = null
        } catch (e: Exception) {
          outputJson = inputJson
          errorMessage = null
        }
      }
      ToastManager.success("已转换")
    } catch (e: Exception) {
      errorMessage = "Conversion failed: ${simplifyErrorMessage(e)}"
    }
  }

  private fun transformOutput(action: (String) -> String) {
    if (outputJson.isBlank()) {
      ToastManager.show("输出栏为空")
      return
    }
    try {
      val result = action(outputJson)
      outputJson = result
      ToastManager.success("右侧已转换")
    } catch (e: Exception) {
      ToastManager.error("转换失败: ${simplifyErrorMessage(e)}")
    }
  }

  // ==================== 左侧功能 ====================

  fun escapeJsonInput() {
    isUnicodeDisplay = false
    transformInput(needSync = false) { str ->
      var res = JsonUtil.toJsonNoFeature(str)
      if (res.length >= 2) res = res.substring(1, res.length - 1)
      res
    }
  }

  fun unescapeJsonInput() {
    isUnicodeDisplay = false
    transformInput(needSync = true) { str ->
      try {
        val raw = if (str.startsWith("\"")) str else "\"$str\""
        JsonUtil.parseObject(raw, String::class.java)
      } catch (e: Exception) {
        str
      }
    }
  }

  fun unicodeToChineseInput() {
    isUnicodeDisplay = false
    transformInput(needSync = true) { str ->
      "(?i)\\\\u([0-9a-f]{4})".toRegex().replace(str) {
        it.groupValues[1].toInt(16).toChar().toString()
      }
    }
  }

  fun chineseToUnicodeInput() {
    isUnicodeDisplay = true
    transformInput(needSync = true) { str ->
      str.map { char ->
        if (char.code in 128..65535) "\\u${char.code.toString(16).padStart(4, '0')}" else char
      }.joinToString("")
    }
  }

  // ==================== 右侧功能 ====================

  fun escapeJsonOutput() {
    isUnicodeDisplay = false
    transformOutput { str ->
      var res = JsonUtil.toJsonNoFeature(str)
      if (res.length >= 2) res = res.substring(1, res.length - 1)
      res
    }
  }

  fun unescapeJsonOutput() {
    isUnicodeDisplay = false
    transformOutput { str ->
      try {
        val raw = if (str.startsWith("\"")) str else "\"$str\""
        JsonUtil.parseObject(raw, String::class.java)
      } catch (e: Exception) {
        ToastManager.show("无法去转义或格式错误")
        str
      }
    }
  }

  fun unicodeToChineseOutput() {
    isUnicodeDisplay = false
    transformOutput { str ->
      "(?i)\\\\u([0-9a-f]{4})".toRegex().replace(str) {
        it.groupValues[1].toInt(16).toChar().toString()
      }
    }
  }

  fun chineseToUnicodeOutput() {
    isUnicodeDisplay = true
    transformOutput { str ->
      str.map { char ->
        if (char.code in 128..65535) "\\u${char.code.toString(16).padStart(4, '0')}" else char
      }.joinToString("")
    }
  }

  // ==================== 辅助方法 ====================

  private fun addToHistory(json: String) {
    if (json.isBlank()) return
    if (historyList.contains(json)) historyList.remove(json)
    historyList.add(0, json)
    if (historyList.size > 20) historyList.removeLast()
  }

  fun loadFromHistory(json: String) {
    inputJson = json
    formatJson(saveToHistory = false)
  }

  fun clear() {
    inputJson = ""
    outputJson = ""
    errorMessage = null
    ToastManager.success("已清空")
  }

  /**
   * 简化异常信息
   */
  private fun simplifyErrorMessage(e: Throwable): String {
    var msg = e.message ?: "Unknown error"
    // 只保留异常信息第一行
    msg = msg.lineSequence().first()
    // 截取并在末尾加省略号
    if (msg.length > 100) {
      msg = msg.take(100) + "..."
    }
    return msg
  }
}
