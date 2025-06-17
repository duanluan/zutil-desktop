package top.zhjh.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import top.zhjh.zui.composable.ZTextField
import java.util.*

@Composable
fun TimestampTool() {
  // 添加通知宿主
  ToastContainer()

  Column {
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

    Box(modifier = Modifier.padding(10.dp).fillMaxWidth().border(1.dp, Color(0xffe5e7eb), RoundedCornerShape(10.dp))) {
      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "当前时间戳：${currentTimestamp}", style = MaterialTheme.typography.h6)

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          // 复制时间戳
          Button(modifier = Modifier.size(85.dp, 32.dp), onClick = {
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
          Button(modifier = Modifier.size(145.dp, 32.dp), onClick = { isMilliByCurrent = !isMilliByCurrent }) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
              val text = if (isMilliByCurrent) "切换为秒级" else "切换为毫秒级"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }
          }
        }
      }
    }

    Box(modifier = Modifier.padding(10.dp).fillMaxWidth().border(1.dp, Color(0xffe5e7eb), RoundedCornerShape(10.dp))) {
      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
          Button(modifier = Modifier.size(145.dp, 32.dp), onClick = {
            isMilliToDatetime = !isMilliToDatetime
            if (isMilliToDatetime) {
              toDatetimeTimestamp.value *= 1000
            } else {
              toDatetimeTimestamp.value /= 1000
            }
          }) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
              val text = if (isMilliToDatetime) "切换为秒级" else "切换为毫秒级"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }
          }

          Button(modifier = Modifier.size(85.dp, 32.dp), onClick = {
            convertTimestamp()
          }) {
            Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.RefreshCw, contentDescription = "转换")
            Spacer(modifier = Modifier.width(5.dp))
            Text("转换")
          }
        }
      }
    }

    Box(modifier = Modifier.padding(10.dp).fillMaxWidth().border(1.dp, Color(0xffe5e7eb), RoundedCornerShape(10.dp))) {
      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
          Button(modifier = Modifier.size(145.dp, 32.dp), onClick = {
            if (convertDatetime()) {
              isMilliToDatetime = !isMilliToDatetime
            }
          }) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
              val text = if (isMilliToDatetime) "切换为秒级" else "切换为毫秒级"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }
          }

          Button(modifier = Modifier.size(85.dp, 32.dp), onClick = {
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
