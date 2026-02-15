package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.csaf.awt.ClipboardUtil
import top.csaf.date.DateUtil
import top.csaf.lang.StrUtil
import top.zhjh.common.composable.ToastManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 时间戳工具页的状态与业务逻辑中心。
 *
 * 设计目标：
 * 1. 将“时间转换规则、单位切换规则、错误处理”集中在 ViewModel，避免 UI 层散落业务逻辑。
 * 2. 对外暴露稳定的状态字段与函数，便于 Composable 简洁调用。
 * 3. 所有字段使用 Compose State（mutableStateOf），确保状态变化能驱动界面自动重组。
 */
class TimestampViewModel : ViewModel() {
  companion object {
    const val CUSTOM_DATETIME_FORMAT_OPTION = "自定义"
    private const val DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    val DATETIME_FORMAT_OPTIONS = listOf(
      DEFAULT_DATETIME_FORMAT,
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy/MM/dd HH:mm:ss",
      "yyyy-MM-dd'T'HH:mm:ss",
      CUSTOM_DATETIME_FORMAT_OPTION
    )
  }

  // ==================== 卡片一：当前时间戳 ====================

  /**
   * 当前时间戳展示单位：
   * - false：秒
   * - true ：毫秒
   */
  var isMilliByCurrent by mutableStateOf(false)

  /**
   * 当前时间戳值（随时钟定时刷新）。
   * 对外只读，防止 UI 层误写导致数据不一致。
   */
  var currentTimestamp by mutableStateOf(DateUtil.nowEpochSecond())
    private set

  // ==================== 卡片二：时间戳 -> 日期时间 ====================

  /**
   * 输入的时间戳（数字）。
   * 默认使用 currentTimestamp 作为初始值，降低用户首次使用成本。
   */
  var toDatetimeTimestamp by mutableStateOf(currentTimestamp)

  /**
   * 输入时间戳单位：
   * - false：秒级输入
   * - true ：毫秒级输入
   */
  var isMilliToDatetime by mutableStateOf(false)

  /**
   * 转换后的日期时间字符串输出。
   */
  var toDatetimeResult by mutableStateOf("")

  /**
   * “时间戳 -> 日期时间”链路使用的时区 ID（如 Asia/Shanghai）。
   */
  var selectedTimezoneToDatetime by mutableStateOf(TimeZone.getDefault().id)

  /**
   * “时间戳 -> 日期时间”输出格式选项。
   */
  var selectedDatetimeFormatToDatetime by mutableStateOf(DEFAULT_DATETIME_FORMAT)

  /**
   * “时间戳 -> 日期时间”自定义格式内容（当下拉选中“自定义”时生效）。
   */
  var customDatetimeFormatToDatetime by mutableStateOf(DEFAULT_DATETIME_FORMAT)

  // ==================== 卡片三：日期时间 -> 时间戳 ====================

  /**
   * 待解析的日期时间字符串输入。
   */
  var toTimestampDatetimeInput by mutableStateOf("")

  /**
   * 输出时间戳单位：
   * - false：输出秒级
   * - true ：输出毫秒级
   */
  var isMilliToTimestamp by mutableStateOf(false)

  /**
   * 解析结果（时间戳字符串）。
   * 采用 String 是为了与输入框绑定更直接，避免 UI 层频繁做 Long/String 转换。
   */
  var toTimestampResult by mutableStateOf("")

  /**
   * “日期时间 -> 时间戳”链路使用的时区 ID。
   */
  var selectedTimezoneToTimestamp by mutableStateOf(TimeZone.getDefault().id)

  /**
   * “日期时间 -> 时间戳”输入格式选项。
   */
  var selectedDatetimeFormatToTimestamp by mutableStateOf(DEFAULT_DATETIME_FORMAT)

  /**
   * “日期时间 -> 时间戳”自定义格式内容（当下拉选中“自定义”时生效）。
   */
  var customDatetimeFormatToTimestamp by mutableStateOf(DEFAULT_DATETIME_FORMAT)

  init {
    startClock()
  }

  // ==================== 内部时钟刷新逻辑 ====================

  /**
   * 启动一个循环任务，按当前单位刷新 [currentTimestamp]。
   *
   * 刷新频率策略：
   * - 毫秒模式：100ms 刷新一次，平衡“实时感”与“重组开销”。
   * - 秒模式：1000ms 刷新一次，避免无意义高频更新。
   *
   * 说明：
   * 该协程运行在 viewModelScope 内，ViewModel 销毁时会自动取消，无需手动清理。
   */
  private fun startClock() {
    viewModelScope.launch {
      while (true) {
        currentTimestamp = if (isMilliByCurrent) {
          DateUtil.nowEpochMilli()
        } else {
          DateUtil.nowEpochSecond()
        }
        delay(if (isMilliByCurrent) 100 else 1000)
      }
    }
  }

  // ==================== 卡片一行为 ====================

  /**
   * 切换当前时间戳显示单位（秒 <-> 毫秒）。
   *
   * 切换后立即刷新一次 currentTimestamp，
   * 避免必须等待下一轮 delay 才看到变化。
   */
  fun toggleCurrentMilli() {
    isMilliByCurrent = !isMilliByCurrent
    currentTimestamp = if (isMilliByCurrent) DateUtil.nowEpochMilli() else DateUtil.nowEpochSecond()
  }

  /**
   * 复制当前时间戳到系统剪贴板，并给出操作反馈。
   */
  fun copyCurrentTimestamp() {
    if (ClipboardUtil.set(currentTimestamp)) {
      ToastManager.success("复制成功")
    } else {
      ToastManager.error("复制失败")
    }
  }

  // ==================== 卡片二行为：时间戳 -> 日期时间 ====================

  /**
   * 获取“时间戳 -> 日期时间”当前生效的日期格式模板。
   */
  fun getEffectiveDatetimeFormatToDatetime(): String {
    return if (selectedDatetimeFormatToDatetime == CUSTOM_DATETIME_FORMAT_OPTION) {
      customDatetimeFormatToDatetime.trim()
    } else {
      selectedDatetimeFormatToDatetime
    }
  }

  /**
   * 执行“时间戳 -> 日期时间”转换。
   *
   * 处理步骤：
   * 1. 根据 [isMilliToDatetime] 判断输入是秒还是毫秒。
   * 2. 如果输入是秒，则统一放大到毫秒再格式化。
   * 3. 使用 [selectedTimezoneToDatetime] 对应时区输出目标文本。
   */
  fun convertTimestampToDatetime() {
    val finalTimestamp = if (!isMilliToDatetime) {
      toDatetimeTimestamp * 1000
    } else {
      toDatetimeTimestamp
    }

    try {
      val formatter = DateTimeFormatter.ofPattern(getEffectiveDatetimeFormatToDatetime())
      val zoneId = TimeZone.getTimeZone(selectedTimezoneToDatetime).toZoneId()
      toDatetimeResult = Instant.ofEpochMilli(finalTimestamp)
        .atZone(zoneId)
        .toLocalDateTime()
        .format(formatter)
    } catch (_: Exception) {
      ToastManager.error("时间戳或日期格式错误")
    }
  }

  /**
   * 切换“时间戳输入单位”（秒 <-> 毫秒）。
   *
   * 关键点：
   * - 该切换作用于“输入值解释方式”，不是只改显示文案。
   * - 为了保持“表示同一时刻”，切换时会自动乘/除 1000。
   */
  fun toggleTimestampToDatetimeMilli() {
    isMilliToDatetime = !isMilliToDatetime
    toDatetimeTimestamp = if (isMilliToDatetime) {
      toDatetimeTimestamp * 1000
    } else {
      toDatetimeTimestamp / 1000
    }
  }

  /**
   * 将“时间戳 -> 日期时间”区域的输入与输出都设置为当前时刻。
   *
   * 规则：
   * - 输入框 toDatetimeTimestamp 按当前单位（秒/毫秒）写入。
   * - 输出框 toDatetimeResult 按当前选择时区格式化。
   */
  fun useNowForTimestampToDatetime() {
    val nowMillis = DateUtil.nowEpochMilli()
    toDatetimeTimestamp = if (isMilliToDatetime) {
      nowMillis
    } else {
      nowMillis / 1000
    }

    try {
      val formatter = DateTimeFormatter.ofPattern(getEffectiveDatetimeFormatToDatetime())
      val zoneId = TimeZone.getTimeZone(selectedTimezoneToDatetime).toZoneId()
      toDatetimeResult = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDateTime()
        .format(formatter)
    } catch (_: Exception) {
      ToastManager.error("日期格式错误")
    }
  }

  // ==================== 卡片三行为：日期时间 -> 时间戳 ====================

  /**
   * 获取“日期时间 -> 时间戳”当前生效的日期格式模板。
   */
  fun getEffectiveDatetimeFormatToTimestamp(): String {
    return if (selectedDatetimeFormatToTimestamp == CUSTOM_DATETIME_FORMAT_OPTION) {
      customDatetimeFormatToTimestamp.trim()
    } else {
      selectedDatetimeFormatToTimestamp
    }
  }

  /**
   * 执行“日期时间 -> 时间戳”转换。
   *
   * @return true 表示转换成功，false 表示失败（空输入或解析异常）。
   *
   * 说明：
   * - 输入为空时直接返回 false，不弹错，避免打断用户输入过程。
   * - 解析失败时清空输出并 toast 提示。
   */
  fun convertDatetimeToTimestamp(): Boolean {
    if (StrUtil.isBlank(toTimestampDatetimeInput)) {
      return false
    }

    return try {
      val zoneId = TimeZone.getTimeZone(selectedTimezoneToTimestamp).toZoneId()
      val formatter = DateTimeFormatter.ofPattern(getEffectiveDatetimeFormatToTimestamp())
      val instant = LocalDateTime.parse(toTimestampDatetimeInput, formatter)
        .atZone(zoneId)
        .toInstant()
      toTimestampResult = if (isMilliToTimestamp) {
        instant.toEpochMilli().toString()
      } else {
        instant.epochSecond.toString()
      }
      true
    } catch (_: Exception) {
      ToastManager.error("日期时间转换错误")
      toTimestampResult = ""
      false
    }
  }

  /**
   * 切换“日期时间 -> 时间戳”输出单位（秒 <-> 毫秒）。
   *
   * 行为约定：
   * - 仅切换状态不直接报错。
   * - 若输入框当前有内容，则自动触发一次重算，保证结果立即与新单位一致。
   */
  fun toggleDatetimeToTimestampMilli() {
    isMilliToTimestamp = !isMilliToTimestamp
    if (StrUtil.isNotBlank(toTimestampDatetimeInput)) {
      convertDatetimeToTimestamp()
    }
  }

  /**
   * 将“日期时间 -> 时间戳”区域的输入与输出都设置为当前时刻。
   *
   * 规则：
   * - 输入框 toTimestampDatetimeInput 以当前选择时区格式化当前时间。
   * - 输出框 toTimestampResult 按当前单位（秒/毫秒）写入。
   */
  fun useNowForDatetimeToTimestamp() {
    val nowMillis = DateUtil.nowEpochMilli()
    val zoneId = TimeZone.getTimeZone(selectedTimezoneToTimestamp).toZoneId()

    try {
      val formatter = DateTimeFormatter.ofPattern(getEffectiveDatetimeFormatToTimestamp())
      toTimestampDatetimeInput = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDateTime()
        .format(formatter)
    } catch (_: Exception) {
      ToastManager.error("日期格式错误")
      return
    }
    toTimestampResult = if (isMilliToTimestamp) {
      nowMillis.toString()
    } else {
      (nowMillis / 1000).toString()
    }
  }
}
