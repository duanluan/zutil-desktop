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
import java.util.*

class TimestampViewModel : ViewModel() {

  // ==================== 状态区域 (State) ====================

  // 1. 【卡片一】当前时间戳
  var isMilliByCurrent by mutableStateOf(false)
  var currentTimestamp by mutableStateOf(DateUtil.nowEpochSecond())
    private set

  // 2. 【卡片二】时间戳 -> 日期时间
  var toDatetimeTimestamp by mutableStateOf(currentTimestamp) // 输入：时间戳
  var isMilliToDatetime by mutableStateOf(false)              // 状态：是否毫秒
  var toDatetimeResult by mutableStateOf("")                  // 输出：日期时间字符串
  var selectedTimezoneToDatetime by mutableStateOf(TimeZone.getDefault().id)

  // 3. 【卡片三】日期时间 -> 时间戳
  var toTimestampDatetimeInput by mutableStateOf("")          // 输入：日期时间字符串
  var isMilliToTimestamp by mutableStateOf(false)             // 状态：是否毫秒
  var toTimestampResult by mutableStateOf("")                 // 输出：时间戳
  var selectedTimezoneToTimestamp by mutableStateOf(TimeZone.getDefault().id)


  // ==================== 逻辑区域 (Logic) ====================

  init {
    startClock()
  }

  // --- 内部逻辑 ---
  private fun startClock() {
    viewModelScope.launch {
      while (true) {
        currentTimestamp = if (isMilliByCurrent) {
          DateUtil.nowEpochMilli()
        } else {
          DateUtil.nowEpochSecond()
        }
        // 根据单位动态调整刷新频率
        delay(if (isMilliByCurrent) 100 else 1000)
      }
    }
  }

  // --- 卡片一：当前时间戳逻辑 ---

  fun toggleCurrentMilli() {
    isMilliByCurrent = !isMilliByCurrent
    // 切换后立即更新一次数值，避免等待 delay 造成的视觉延迟
    currentTimestamp = if (isMilliByCurrent) DateUtil.nowEpochMilli() else DateUtil.nowEpochSecond()
  }

  fun copyCurrentTimestamp() {
    if (ClipboardUtil.set(currentTimestamp)) {
      ToastManager.success("复制成功")
    } else {
      ToastManager.error("复制失败")
    }
  }

  // --- 卡片二：时间戳转日期逻辑 ---

  // 转换执行
  fun convertTimestampToDatetime() {
    val timestamp = toDatetimeTimestamp ?: return
    // 如果当前是秒级，转为毫秒处理
    val finalTimestamp = if (!isMilliToDatetime) timestamp * 1000 else timestamp

    try {
      // 使用您指正后的正确写法
      toDatetimeResult = DateUtil.format(
        finalTimestamp,
        TimeZone.getTimeZone(selectedTimezoneToDatetime).toZoneId()
      )
    } catch (e: Exception) {
      ToastManager.error("时间戳格式错误")
    }
  }

  // 切换秒/毫秒（带数值自动换算）
  fun toggleTimestampToDatetimeMilli() {
    isMilliToDatetime = !isMilliToDatetime
    // 切换单位时，自动乘除 1000，保持时间点一致
    if (isMilliToDatetime) {
      toDatetimeTimestamp = (toDatetimeTimestamp ?: 0) * 1000
    } else {
      toDatetimeTimestamp = (toDatetimeTimestamp ?: 0) / 1000
    }
  }

  // --- 卡片三：日期转时间戳逻辑 ---

  // 转换执行（返回 Boolean 供 UI 判断是否需要执行后续逻辑）
  fun convertDatetimeToTimestamp(): Boolean {
    if (StrUtil.isBlank(toTimestampDatetimeInput)) {
      return false
    }
    return try {
      val zoneId = TimeZone.getTimeZone(selectedTimezoneToTimestamp).toZoneId()
      toTimestampResult = if (isMilliToTimestamp) {
        DateUtil.toEpochMilli(toTimestampDatetimeInput, zoneId).toString()
      } else {
        DateUtil.toEpochSecond(toTimestampDatetimeInput, zoneId).toString()
      }
      true
    } catch (e: Exception) {
      ToastManager.error("日期时间转换错误")
      toTimestampResult = ""
      false
    }
  }

  // 切换秒/毫秒
  fun toggleDatetimeToTimestampMilli() {
    // 逻辑复刻原代码：先尝试转换，如果成功则切换单位
    if (convertDatetimeToTimestamp()) {
      isMilliToTimestamp = !isMilliToTimestamp
      // 可选：切换后是否立即按新单位重新转换？
      // convertDatetimeToTimestamp()
    }
  }
}
