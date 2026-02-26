package top.zhjh.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import top.zhjh.common.composable.ToastContainer
import top.zhjh.viewmodel.TimestampViewModel
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType

/**
 * 时间戳工具页面。
 *
 * 说明：
 * 1. 当前时间戳卡片：实时展示当前时间戳，并支持复制、秒/毫秒切换。
 * 2. 时间戳 >> 日期时间：将输入时间戳按所选时区转换为日期时间字符串。
 * 3. 日期时间 >> 时间戳：将输入日期时间按所选时区转换为秒级或毫秒级时间戳。
 */
@Composable
fun TimestampTool() {
  val viewModel: TimestampViewModel = viewModel { TimestampViewModel() }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .padding(10.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ZText(
            text = "当前时间戳：${viewModel.currentTimestamp} ${if (viewModel.isMilliByCurrent) "毫秒" else "秒"}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            val copyText = "复制时间戳"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.copyCurrentTimestamp() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.Clipboard,
                  contentDescription = copyText
                )
              }
            ) {
              ZText(copyText)
            }

            val currentToggleText = if (viewModel.isMilliByCurrent) "切换为秒级" else "切换为毫秒级"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.toggleCurrentMilli() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.Shuffle,
                  contentDescription = currentToggleText
                )
              }
            ) {
              ZText(currentToggleText)
            }
          }
        }
      }

      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ZText(text = "时间戳 >> 日期时间", fontWeight = FontWeight.Bold, fontSize = 18.sp)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            ZTextField(
              modifier = Modifier.width(160.dp),
              value = viewModel.toDatetimeTimestamp.toString(),
              onValueChange = { viewModel.toDatetimeTimestamp = it.toLongOrNull() ?: 0L }
            )

            Icon(imageVector = FeatherIcons.ArrowRightCircle, contentDescription = "转换方向")

            ZTextField(
              modifier = Modifier.width(200.dp),
              value = viewModel.toDatetimeResult,
              onValueChange = { viewModel.toDatetimeResult = it }
            )

            TimezoneDropdown(
              selectedTimezone = viewModel.selectedTimezoneToDatetime,
              onTimezoneSelected = { newTimezone ->
                viewModel.selectedTimezoneToDatetime = newTimezone
                viewModel.convertTimestampToDatetime()
              }
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            ZDropdownMenu(
              options = TimestampViewModel.DATETIME_FORMAT_OPTIONS,
              defaultSelectedOption = viewModel.selectedDatetimeFormatToDatetime,
              onOptionSelected = { selectedFormat ->
                viewModel.selectedDatetimeFormatToDatetime = selectedFormat.orEmpty()
                viewModel.convertTimestampToDatetime()
              },
              placeholder = "请选择输出格式",
              modifier = Modifier.width(220.dp)
            )

            if (viewModel.selectedDatetimeFormatToDatetime == TimestampViewModel.CUSTOM_DATETIME_FORMAT_OPTION) {
              ZTextField(
                modifier = Modifier.width(300.dp),
                value = viewModel.customDatetimeFormatToDatetime,
                onValueChange = { viewModel.customDatetimeFormatToDatetime = it },
                placeholder = "自定义格式，例如 yyyy-MM-dd HH:mm:ss"
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            val toDatetimeToggleText = if (viewModel.isMilliToDatetime) "切换为秒级" else "切换为毫秒级"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.toggleTimestampToDatetimeMilli() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.Shuffle,
                  contentDescription = toDatetimeToggleText
                )
              }
            ) {
              ZText(toDatetimeToggleText)
            }

            val convertToDatetimeText = "转换"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.convertTimestampToDatetime() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.RefreshCw,
                  contentDescription = convertToDatetimeText
                )
              }
            ) {
              ZText(convertToDatetimeText)
            }

            val nowToDatetimeText = "当前时间"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.useNowForTimestampToDatetime() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.Clock,
                  contentDescription = nowToDatetimeText
                )
              }
            ) {
              ZText(nowToDatetimeText)
            }
          }
        }
      }

      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ZText(text = "日期时间 >> 时间戳", fontWeight = FontWeight.Bold, fontSize = 18.sp)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            TimezoneDropdown(
              selectedTimezone = viewModel.selectedTimezoneToTimestamp,
              onTimezoneSelected = { newTimezone ->
                viewModel.selectedTimezoneToTimestamp = newTimezone
                viewModel.convertDatetimeToTimestamp()
              }
            )

            ZTextField(
              modifier = Modifier.width(200.dp),
              value = viewModel.toTimestampDatetimeInput,
              onValueChange = { viewModel.toTimestampDatetimeInput = it }
            )

            Icon(imageVector = FeatherIcons.ArrowRightCircle, contentDescription = "转换方向")

            ZTextField(
              modifier = Modifier.width(160.dp),
              value = viewModel.toTimestampResult,
              onValueChange = { viewModel.toTimestampResult = it }
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            ZDropdownMenu(
              options = TimestampViewModel.DATETIME_FORMAT_OPTIONS,
              defaultSelectedOption = viewModel.selectedDatetimeFormatToTimestamp,
              onOptionSelected = { selectedFormat ->
                viewModel.selectedDatetimeFormatToTimestamp = selectedFormat.orEmpty()
                viewModel.convertDatetimeToTimestamp()
              },
              placeholder = "请选择输入格式",
              modifier = Modifier.width(220.dp)
            )

            if (viewModel.selectedDatetimeFormatToTimestamp == TimestampViewModel.CUSTOM_DATETIME_FORMAT_OPTION) {
              ZTextField(
                modifier = Modifier.width(300.dp),
                value = viewModel.customDatetimeFormatToTimestamp,
                onValueChange = { viewModel.customDatetimeFormatToTimestamp = it },
                placeholder = "自定义格式，例如 yyyy-MM-dd HH:mm:ss"
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            val toTimestampToggleText = if (viewModel.isMilliToTimestamp) "切换为秒级" else "切换为毫秒级"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.toggleDatetimeToTimestampMilli() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.Shuffle,
                  contentDescription = toTimestampToggleText
                )
              }
            ) {
              ZText(toTimestampToggleText)
            }

            val convertToTimestampText = "转换"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.convertDatetimeToTimestamp() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.RefreshCw,
                  contentDescription = convertToTimestampText
                )
              }
            ) {
              ZText(convertToTimestampText)
            }

            val nowToTimestampText = "当前时间"
            ZButton(
              type = ZColorType.PRIMARY,
              onClick = { viewModel.useNowForDatetimeToTimestamp() },
              icon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  imageVector = FeatherIcons.Clock,
                  contentDescription = nowToTimestampText
                )
              }
            ) {
              ZText(nowToTimestampText)
            }
          }
        }
      }

      ZCard(
        shadow = ZCardShadow.NEVER,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
          start = ZCardDefaults.ContentPadding.calculateStartPadding(LayoutDirection.Ltr),
          end = ZCardDefaults.ContentPadding.calculateEndPadding(LayoutDirection.Ltr),
          top = 0.dp,
          bottom = ZCardDefaults.ContentPadding.calculateBottomPadding()
        )
      ) {
        ZText("简介", style = MaterialTheme.typography.h3)
        ZParagraph("时间戳，从协调世界时（UTC）1970 年 1 月 1 日 0 时 0 分 0 秒起至现在的总秒数，不考虑闰秒。")
        ZParagraph("协调世界时是世界上调节时钟和时间的主要时间标准，它与 0 度经线的平太阳时相差不超过 1 秒，并不遵守夏令时。")
        ZParagraph("闰秒是偶尔运用于协调世界时（UTC）的调整，经由增加或减少一秒，以消弥精确的时间（使用原子钟测量）和不精确的观测太阳时（称为 UT1），之间的差异。")
      }
    }

    ToastContainer()
  }
}
