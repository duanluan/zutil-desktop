package top.zhjh.zutil.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.csaf.awt.ClipboardUtil
import top.csaf.date.DateUtil

@Composable
fun TimestampTool() {
  // 添加通知宿主
  ToastContainer()

  // 是否为毫秒级时间戳
  var isMilliseconds by remember { mutableStateOf(false) }
  var currentTimestamp by remember { mutableStateOf(DateUtil.nowEpochSecond()) }

  // 启动协程定期更新时间戳
  LaunchedEffect(key1 = true, key2 = isMilliseconds) {
    while (true) {
      currentTimestamp = if (isMilliseconds) {
        DateUtil.nowEpochMilli()
      } else {
        DateUtil.nowEpochSecond()
      }
      // 秒级时间戳每秒更新一次，毫秒级时间戳每100毫秒更新一次
      delay(if (isMilliseconds) 100 else 1000)
    }
  }

  Column(modifier = Modifier.padding(16.dp)) {
    Text(
      text = "当前时间戳：${currentTimestamp}",
      style = MaterialTheme.typography.h5
    )

    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
      // 复制时间戳
      Button(
        onClick = {
          val msg = (if (ClipboardUtil.set(currentTimestamp)) {
            ToastManager.success("复制成功")
          } else {
            ToastManager.error("复制失败")
          })
        }
      ) {
        Text(text = "复制时间戳")
      }

      // 切换秒和毫秒
      Button(
        onClick = { isMilliseconds = !isMilliseconds }
      ) {
        Text(text = if (isMilliseconds) "切换为秒级" else "切换为毫秒级")
      }
    }
  }
}
