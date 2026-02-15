package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import top.csaf.json.JsonUtil
import top.zhjh.common.composable.ToastManager

/**
 * JSON 工具页的状态与业务逻辑中心。
 *
 * 设计原则：
 * 1. UI 仅做展示和事件转发，所有“可复用业务规则”放在 ViewModel。
 * 2. 所有可观察状态均使用 Compose State，确保状态变更自动驱动重组。
 * 3. 对“失败场景”给出温和降级：
 *    - 能继续展示就继续展示（例如失败时保留原文）；
 *    - 需要提示时给简短错误信息，避免过长异常堆栈影响阅读。
 *
 * 页面数据流（简化）：
 * - 左侧输入 `inputJson` 经过格式化/压缩/转义等处理后，输出到 `outputJson`；
 * - 右侧可继续做二次转换，但默认不反向改写左侧（除非点击“覆盖到左侧”）；
 * - 关键输入会进入 `historyList`，支持快速回填与复现。
 */
class JsonViewModel : ViewModel() {

  // ==================== 状态 ====================
  /** 左侧输入框原始文本。 */
  var inputJson by mutableStateOf("")

  /** 右侧结果文本。 */
  var outputJson by mutableStateOf("")

  /** 右侧展示模式：`true`=树形，`false`=源码。 */
  var isTreeMode by mutableStateOf(false)

  /**
   * Unicode 显示开关。
   *
   * 约定：
   * - `true`：优先展示 `\uXXXX` 形式（尤其用于树形值展示和复制预期）；
   * - `false`：按正常字符显示。
   */
  var isUnicodeDisplay by mutableStateOf(false)

  /**
   * 树形展开指令信号。
   *
   * 约定值：
   * - `0`：无动作（默认）
   * - `1`：展开所有可展开节点
   * - `2`：收缩所有节点（根级保留由树组件自行决定）
   *
   * 说明：这是“命令信号”而不是持久状态，树组件会消费该值来更新自身展开映射。
   */
  var treeExpandState by mutableStateOf(0)

  /**
   * 最近输入历史（最多保留 20 条）。
   * - 新记录插入到头部；
   * - 若已存在则先删除旧位置再插入头部（去重 + 最近使用优先）。
   */
  val historyList = mutableStateListOf<String>()

  /**
   * 右侧错误提示文案。
   * - `null` 表示当前无错误；
   * - 非空时由 UI 以红色卡片展示。
   */
  var errorMessage by mutableStateOf<String?>(null)

  // ==================== 核心功能 ====================

  /**
   * 格式化左侧输入 JSON，并把结果写入右侧输出。
   *
   * 行为细节：
   * 1. 输入为空时直接返回，不提示错误；
   * 2. 成功时清空错误、关闭 Unicode 显示；
   * 3. 默认会把当前输入写入历史（可通过参数关闭）；
   * 4. 失败时不清空用户输入，而是把原输入透传到右侧，便于继续编辑修复。
   *
   * @param saveToHistory 是否把当前输入加入历史，默认 `true`。
   */
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

  /**
   * 用右侧结果覆盖左侧输入。
   *
   * 典型场景：
   * - 右侧做完“压缩/转义/Unicode 转换”后，想继续在左侧链式处理；
   * - 复制前希望把最终结果固定为新的输入基线。
   */
  fun overwriteLeft() {
    if (outputJson.isBlank()) return
    inputJson = outputJson
    ToastManager.success("已覆盖到左侧")
  }

  /**
   * 智能压缩 JSON。
   *
   * 两阶段策略：
   * 1. 先按“标准 JSON 文本”直接 `minify`；
   * 2. 若失败，再尝试把输入当作“可能带转义的 JSON 字符串”：
   *    - 包裹成字符串后先反转义；
   *    - 对反转义结果再 `minify`；
   *    - 最后重新转义成单行文本返回。
   *
   * 这样可兼容以下输入：
   * - 标准对象/数组 JSON；
   * - 一整段被转义的 JSON 字符串（常见于日志、接口回包内嵌 JSON）。
   */
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

  /**
   * 加载演示 JSON 并立即触发格式化。
   *
   * 用途：
   * - 首次打开工具时快速感知功能；
   * - 回归测试按钮链路（格式化、树形、复制等）。
   */
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

  /**
   * 对左侧输入执行字符串级转换。
   *
   * 通用行为：
   * 1. 输入为空则提示并返回；
   * 2. 转换后若内容无变化，提示“内容无变化”；
   * 3. 成功时写入历史，并可选地同步刷新右侧（尝试格式化）；
   * 4. 失败时记录简化错误。
   *
   * @param needSync 转换后是否尝试同步刷新右侧。
   * @param action 具体转换函数，输入为当前 `inputJson`，输出为新文本。
   */
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

  /**
   * 对右侧输出执行字符串级转换。
   *
   * 与 [transformInput] 的区别：
   * - 不写入历史；
   * - 不反向影响左侧输入；
   * - 仅关注右侧“结果修饰”类操作。
   */
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

  /**
   * 左侧“加转义”。
   *
   * 示例：
   * - 输入：`{"a":"中"}`
   * - 输出：`{\"a\":\"中\"}`
   */
  fun escapeJsonInput() {
    isUnicodeDisplay = false
    transformInput(needSync = false) { str ->
      var res = JsonUtil.toJsonNoFeature(str)
      if (res.length >= 2) res = res.substring(1, res.length - 1)
      res
    }
  }

  /**
   * 左侧“去转义”。
   *
   * 行为：
   * - 若文本本身不是合法转义字符串，保持原文不变；
   * - 成功后尝试同步右侧格式化展示。
   */
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

  /**
   * 左侧 `\uXXXX` 转中文字符。
   *
   * 使用不区分大小写的正则，兼容 `\u4f60` / `\u4F60`。
   */
  fun unicodeToChineseInput() {
    isUnicodeDisplay = false
    transformInput(needSync = true) { str ->
      "(?i)\\\\u([0-9a-f]{4})".toRegex().replace(str) {
        it.groupValues[1].toInt(16).toChar().toString()
      }
    }
  }

  /**
   * 左侧中文字符转 `\uXXXX`。
   *
   * 规则：仅转换非 ASCII（`code in 128..65535`）字符，英文与常见符号保持原样。
   */
  fun chineseToUnicodeInput() {
    isUnicodeDisplay = true
    transformInput(needSync = true) { str ->
      str.map { char ->
        if (char.code in 128..65535) "\\u${char.code.toString(16).padStart(4, '0')}" else char
      }.joinToString("")
    }
  }

  // ==================== 右侧功能 ====================

  /** 右侧“加转义”，仅修改 `outputJson`。 */
  fun escapeJsonOutput() {
    isUnicodeDisplay = false
    transformOutput { str ->
      var res = JsonUtil.toJsonNoFeature(str)
      if (res.length >= 2) res = res.substring(1, res.length - 1)
      res
    }
  }

  /** 右侧“去转义”，解析失败时保留原文并提示。 */
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

  /** 右侧 `\uXXXX` 转中文字符。 */
  fun unicodeToChineseOutput() {
    isUnicodeDisplay = false
    transformOutput { str ->
      "(?i)\\\\u([0-9a-f]{4})".toRegex().replace(str) {
        it.groupValues[1].toInt(16).toChar().toString()
      }
    }
  }

  /** 右侧中文字符转 `\uXXXX`。 */
  fun chineseToUnicodeOutput() {
    isUnicodeDisplay = true
    transformOutput { str ->
      str.map { char ->
        if (char.code in 128..65535) "\\u${char.code.toString(16).padStart(4, '0')}" else char
      }.joinToString("")
    }
  }

  // ==================== 辅助方法 ====================

  /**
   * 写入历史（MRU 策略）。
   *
   * 规则：
   * - 空文本不记录；
   * - 去重后插头；
   * - 超过 20 条时移除最旧项。
   */
  private fun addToHistory(json: String) {
    if (json.isBlank()) return
    if (historyList.contains(json)) historyList.remove(json)
    historyList.add(0, json)
    if (historyList.size > 20) historyList.removeLast()
  }

  /**
   * 从历史回填到左侧并触发格式化。
   *
   * 注意：此入口不会重复写历史，避免点击历史导致“历史抖动”。
   */
  fun loadFromHistory(json: String) {
    inputJson = json
    formatJson(saveToHistory = false)
  }

  /**
   * 清空输入、输出与错误状态。
   * 历史记录不在此函数中清空，由页面上的“清空历史”按钮单独负责。
   */
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
