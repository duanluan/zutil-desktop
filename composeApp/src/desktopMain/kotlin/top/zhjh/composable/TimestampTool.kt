package top.zhjh.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowRightCircle
import compose.icons.feathericons.Clipboard
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Shuffle
import kotlinx.coroutines.delay
import top.csaf.awt.ClipboardUtil
import top.csaf.date.DateUtil
import top.csaf.lang.StrUtil
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZCard
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import java.util.*

@Composable
fun TimestampTool() {
  // 添加通知宿主
  ToastContainer()

  Column(modifier = Modifier.padding(10.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // 当前时间戳是否为毫秒级
    var isMilliByCurrent by remember { mutableStateOf(false) }
    // 当前时间戳
    var currentTimestamp by remember { mutableStateOf(DateUtil.nowEpochSecond()) }

    // 启动协程定期更新时间戳
    LaunchedEffect(key1 = true, key2 = isMilliByCurrent) {
      while (true) {
        currentTimestamp = if (isMilliByCurrent) {
          DateUtil.nowEpochMilli()
        } else {
          DateUtil.nowEpochSecond()
        }
        // 秒级每秒更新一次，毫秒级每100毫秒更新一次
        delay(if (isMilliByCurrent) 100 else 1000)
      }
    }

    ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "当前时间戳：${currentTimestamp} ${if (isMilliByCurrent) "毫秒" else "秒"}", style = MaterialTheme.typography.h6)

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          // 复制时间戳
          ZButton(type = ZColorType.PRIMARY, onClick = {
            (if (ClipboardUtil.set(currentTimestamp)) {
              ToastManager.success("复制成功")
            } else {
              ToastManager.error("复制失败")
            })
          }
          ) {
            val text = "复制时间戳"
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Clipboard, contentDescription = text)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text)
          }

          // 切换秒和毫秒
          ZButton(type = ZColorType.PRIMARY, onClick = { isMilliByCurrent = !isMilliByCurrent }) {
            val text = if (isMilliByCurrent) "切换为秒级" else "切换为毫秒级"
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text)
          }
        }
      }
    }

    ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 需要转换的时间戳
        val toDatetimeTimestamp = remember { mutableStateOf(currentTimestamp) }
        // 当前时间戳是否为毫秒级
        var isMilliToDatetime by remember { mutableStateOf(false) }
        // 转换后的日期时间
        val toDatetime = remember { mutableStateOf("") }
        // 选择的时区
        var selectedTimezone by remember { mutableStateOf(TimeZone.getDefault().id) }

        fun convertTimestamp() {
          // 转换时间戳
          var timestamp = toDatetimeTimestamp.value
          if (timestamp != null) {
            if (!isMilliToDatetime) {
              timestamp *= 1000
            }
            toDatetime.value = DateUtil.format(timestamp, TimeZone.getTimeZone(selectedTimezone).toZoneId())
          } else {
            ToastManager.error("时间戳格式错误")
          }
        }

        Text(text = "时间戳转日期时间", style = MaterialTheme.typography.h6)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          ZTextField(modifier = Modifier.width(180.dp), value = toDatetimeTimestamp.value.toString(), onValueChange = { toDatetimeTimestamp.value = it.toLongOrNull() ?: 0L })
          Icon(imageVector = FeatherIcons.ArrowRightCircle, contentDescription = "转换")
          ZTextField(modifier = Modifier.width(180.dp), value = toDatetime.value, onValueChange = { toDatetime.value = it })
          TimezoneDropdown(
            selectedTimezone = selectedTimezone,
            onTimezoneSelected = { newTimezone ->
              selectedTimezone = newTimezone
              convertTimestamp()
            }
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          // 切换秒和毫秒
          ZButton(type = ZColorType.PRIMARY, onClick = {
            isMilliToDatetime = !isMilliToDatetime
            if (isMilliToDatetime) {
              toDatetimeTimestamp.value *= 1000
            } else {
              toDatetimeTimestamp.value /= 1000
            }
          }) {
            val text = if (isMilliToDatetime) "切换为秒级" else "切换为毫秒级"
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text)
          }

          ZButton(type = ZColorType.PRIMARY, onClick = {
            convertTimestamp()
          }) {
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.RefreshCw, contentDescription = "转换")
            Spacer(modifier = Modifier.width(5.dp))
            Text("转换")
          }
        }
      }
    }

    ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 转换后的时间戳
        val toTimestamp = remember { mutableStateOf("") }
        // 当前时间戳是否为毫秒级
        var isMilliToDatetime by remember { mutableStateOf(false) }
        // 需要转换的日期时间
        val toTimestampDatetime = remember { mutableStateOf("") }
        // 选择的时区
        var selectedTimezone by remember { mutableStateOf(TimeZone.getDefault().id) }

        fun convertDatetime(): Boolean {
          if (StrUtil.isBlank(toTimestampDatetime.value)) {
            return false
          }
          try {
            if (isMilliToDatetime) {
              toTimestamp.value = DateUtil.toEpochMilli(toTimestampDatetime.value, TimeZone.getTimeZone(selectedTimezone).toZoneId()).toString()
            } else {
              toTimestamp.value = DateUtil.toEpochSecond(toTimestampDatetime.value, TimeZone.getTimeZone(selectedTimezone).toZoneId()).toString()
            }
            return true
          } catch (e: Exception) {
            ToastManager.error("日期时间转换错误")
            toTimestamp.value = ""
            return false
          }
        }

        Text(text = "日期时间转时间戳", style = MaterialTheme.typography.h6)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          TimezoneDropdown(
            selectedTimezone = selectedTimezone,
            onTimezoneSelected = { newTimezone ->
              selectedTimezone = newTimezone
              convertDatetime()
            }
          )
          ZTextField(modifier = Modifier.width(180.dp), value = toTimestampDatetime.value, onValueChange = { toTimestampDatetime.value = it })
          Icon(imageVector = FeatherIcons.ArrowRightCircle, contentDescription = "转换")
          ZTextField(modifier = Modifier.width(180.dp), value = toTimestamp.value, onValueChange = { toTimestamp.value = it })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          // 切换秒和毫秒
          ZButton(type = ZColorType.PRIMARY, onClick = {
            if (convertDatetime()) {
              isMilliToDatetime = !isMilliToDatetime
            }
          }) {
            val text = if (isMilliToDatetime) "切换为秒级" else "切换为毫秒级"
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text)
          }

          ZButton(type = ZColorType.PRIMARY, onClick = {
            convertDatetime()
          }) {
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.RefreshCw, contentDescription = "转换")
            Spacer(modifier = Modifier.width(5.dp))
            Text("转换")
          }
        }
      }
    }
  }
}
