package top.zhjh.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowRightCircle
import compose.icons.feathericons.Clipboard
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Shuffle
import top.zhjh.common.composable.ToastContainer
import top.zhjh.viewmodel.TimestampViewModel
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType

@Composable
fun TimestampTool() {
  // 1. 获取 ViewModel 实例
  val viewModel: TimestampViewModel = viewModel { TimestampViewModel() }

  // 使用 Box 作为根布局，允许组件堆叠 (ToastContainer 在最上层)
  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .padding(10.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

      // ==================== 卡片一：当前时间戳 ====================
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // 显示当前时间戳
          Text(
            text = "当前时间戳：${viewModel.currentTimestamp} ${if (viewModel.isMilliByCurrent) "毫秒" else "秒"}",
            style = MaterialTheme.typography.h6
          )

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            // 复制按钮
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.copyCurrentTimestamp() }) {
              val text = "复制时间戳"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Clipboard, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }

            // 切换单位按钮
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.toggleCurrentMilli() }) {
              val text = if (viewModel.isMilliByCurrent) "切换为秒级" else "切换为毫秒级"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }
          }
        }
      }

      // ==================== 卡片二：时间戳 转 日期时间 ====================
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "时间戳转日期时间", style = MaterialTheme.typography.h6)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            // 输入：时间戳
            ZTextField(
              modifier = Modifier.width(160.dp),
              value = viewModel.toDatetimeTimestamp.toString(),
              onValueChange = {
                // 处理空字符串或非法输入，避免 Crash
                viewModel.toDatetimeTimestamp = it.toLongOrNull() ?: 0L
              }
            )

            Icon(imageVector = FeatherIcons.ArrowRightCircle, contentDescription = "转换")

            // 输出：日期时间
            ZTextField(
              modifier = Modifier.width(200.dp),
              value = viewModel.toDatetimeResult,
              onValueChange = { viewModel.toDatetimeResult = it }
            )

            // 时区选择
            TimezoneDropdown(
              selectedTimezone = viewModel.selectedTimezoneToDatetime,
              onTimezoneSelected = { newTimezone ->
                viewModel.selectedTimezoneToDatetime = newTimezone
                viewModel.convertTimestampToDatetime()
              }
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            // 切换单位按钮
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.toggleTimestampToDatetimeMilli() }) {
              val text = if (viewModel.isMilliToDatetime) "切换为秒级" else "切换为毫秒级"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }

            // 转换按钮
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.convertTimestampToDatetime() }) {
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.RefreshCw, contentDescription = "转换")
              Spacer(modifier = Modifier.width(5.dp))
              Text("转换")
            }
          }
        }
      }

      // ==================== 卡片三：日期时间 转 时间戳 ====================
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "日期时间转时间戳", style = MaterialTheme.typography.h6)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            // 时区选择
            TimezoneDropdown(
              selectedTimezone = viewModel.selectedTimezoneToTimestamp,
              onTimezoneSelected = { newTimezone ->
                viewModel.selectedTimezoneToTimestamp = newTimezone
                viewModel.convertDatetimeToTimestamp()
              }
            )

            // 输入：日期时间
            ZTextField(
              modifier = Modifier.width(200.dp),
              value = viewModel.toTimestampDatetimeInput,
              onValueChange = { viewModel.toTimestampDatetimeInput = it }
            )

            Icon(imageVector = FeatherIcons.ArrowRightCircle, contentDescription = "转换")

            // 输出：时间戳
            ZTextField(
              modifier = Modifier.width(160.dp),
              value = viewModel.toTimestampResult,
              onValueChange = { viewModel.toTimestampResult = it }
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            // 切换单位按钮
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.toggleDatetimeToTimestampMilli() }) {
              val text = if (viewModel.isMilliToTimestamp) "切换为秒级" else "切换为毫秒级"
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.Shuffle, contentDescription = text)
              Spacer(modifier = Modifier.width(5.dp))
              Text(text)
            }

            // 转换按钮
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.convertDatetimeToTimestamp() }) {
              Icon(modifier = Modifier.size(14.dp), imageVector = FeatherIcons.RefreshCw, contentDescription = "转换")
              Spacer(modifier = Modifier.width(5.dp))
              Text("转换")
            }
          }
        }
      }

      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        ZText("简介", style = MaterialTheme.typography.h3)
        ZParagraph("时间戳，从协调世界时（UTC）1970 年 1 月 1 日 0 时 0 分 0 秒起至现在的总秒数，不考虑闰秒。")
        ZParagraph("协调世界时是世界上调节时钟和时间的主要时间标准，它与 0 度经线的平太阳时相差不超过 1 秒，并不遵守夏令时。")
        ZParagraph("闰秒是偶尔运用于协调世界时（UTC）的调整，经由增加或减少一秒，以消弥精确的时间（使用原子钟测量）和不精确的观测太阳时（称为 UT1），之间的差异。")
      }
    }

    // 将 ToastContainer 放在 Box 的最后，确保它浮在最上层
    ToastContainer()
  }
}
