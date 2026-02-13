package top.zhjh.composable

import ZLink
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Download
import compose.icons.feathericons.Pause
import compose.icons.feathericons.Play
import compose.icons.feathericons.X
import top.zhjh.data.ModelConstants
import top.zhjh.data.ModelInfo
import top.zhjh.viewmodel.DownloadState
import top.zhjh.viewmodel.ModelDownloadViewModel
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZCard
import top.zhjh.zui.enums.ZColorType
import java.io.File
import java.util.Locale

@Composable
fun ModelManagerDialog(
  onCloseRequest: () -> Unit,
  onModelReady: (String) -> Unit
) {
  val viewModel: ModelDownloadViewModel = viewModel { ModelDownloadViewModel() }

  // 定义列表滚动状态
  val listState = rememberLazyListState()

  DialogWindow(
    onCloseRequest = onCloseRequest,
    title = "模型下载中心",
    state = rememberDialogState(width = 800.dp, height = 600.dp)
  ) {
    MaterialTheme {
      Column(Modifier.padding(16.dp)) {
        // 顶部标题栏
        Row(
          Modifier.fillMaxWidth().padding(bottom = 5.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            Modifier.fillMaxWidth().padding(bottom = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Text("Sherpa-ONNX 官方模型库", style = MaterialTheme.typography.h6)
            Text("保存位置: ${File(viewModel.downloadDir).absolutePath}", style = MaterialTheme.typography.caption)
          }
        }

        Row(modifier = Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
          Text("更多模型请访问：", style = MaterialTheme.typography.body2)
          ZLink(
            text = "github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models",
            url = "https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models"
          )
        }

        // 添加滚动条容器
        // 使用 Box + weight(1f) 占据剩余高度
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          LazyColumn(
            state = listState, // 绑定状态
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(end = 12.dp) // 右侧留出滚动条空间
          ) {
            items(ModelConstants.availableModels) { model ->
              ModelItemCard(model, viewModel, onModelReady)
            }
          }

          // 垂直滚动条
          VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = listState)
          )
        }
      }
    }
  }
}

@Composable
fun ModelItemCard(
  model: ModelInfo,
  viewModel: ModelDownloadViewModel,
  onUseModel: (String) -> Unit
) {
  // 获取当前任务状态
  val taskStatus = viewModel.taskStatuses[model.id]
  val state = taskStatus?.state ?: DownloadState.IDLE
  val progress = taskStatus?.progress ?: 0f
  val downloadedBytes = taskStatus?.downloadedBytes ?: 0L
  val totalBytes = taskStatus?.totalBytes ?: -1L
  val speedBytesPerSec = taskStatus?.speedBytesPerSec ?: 0.0

  // 检查本地是否已存在
  val folderName = model.fileName.replace(".tar.bz2", "")
  val isLocalExist = File(viewModel.downloadDir, folderName).exists()

  ZCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(10.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 左侧信息区域
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(model.name, style = MaterialTheme.typography.subtitle1, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
          Spacer(Modifier.width(8.dp))
          Text(model.sizeMb, style = MaterialTheme.typography.caption, color = Color.Gray)
        }
        Spacer(Modifier.height(4.dp))
        Text(model.description, style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(4.dp))
        Text("类型: ${model.type} | 语言: ${model.language}", style = MaterialTheme.typography.caption, color = Color.Gray)
      }

      Spacer(Modifier.width(16.dp))

      // 右侧操作区域
      if (isLocalExist && state != DownloadState.EXTRACTING) {
        // 已下载完成，显示“使用”
        ZButton(type = ZColorType.SUCCESS, onClick = {
          val path = File(viewModel.downloadDir, folderName).absolutePath
          onUseModel(path)
        }) {
          Icon(FeatherIcons.CheckCircle, null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("使用")
        }
      } else {
        // 根据下载状态显示不同 UI
        when (state) {
          DownloadState.IDLE, DownloadState.ERROR -> {
            ZButton(type = ZColorType.PRIMARY, onClick = { viewModel.startOrResumeDownload(model) }) {
              Icon(FeatherIcons.Download, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(if (state == DownloadState.ERROR) "重试" else "下载")
            }
          }

          DownloadState.DOWNLOADING -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 8.dp)) {
                if (progress < 0f) {
                  LinearProgressIndicator(modifier = Modifier.width(60.dp))
                } else {
                  LinearProgressIndicator(progress = progress, modifier = Modifier.width(60.dp))
                }
                Text(
                  buildProgressLabel(downloadedBytes, totalBytes, speedBytesPerSec),
                  style = MaterialTheme.typography.caption
                )
              }

              // 暂停按钮
              ZButton(type = ZColorType.WARNING, modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp), onClick = { viewModel.pauseDownload(model.id) }) {
                Icon(FeatherIcons.Pause, null, modifier = Modifier.size(14.dp))
              }
              Spacer(Modifier.width(5.dp))
              // 取消按钮
              ZButton(type = ZColorType.DANGER, modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp), onClick = { viewModel.cancelDownload(model) }) {
                Icon(FeatherIcons.X, null, modifier = Modifier.size(14.dp))
              }
            }
          }

          DownloadState.PAUSED -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                "暂停: ${buildProgressLabel(downloadedBytes, totalBytes, speedBytesPerSec)}",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(end = 8.dp)
              )

              // 继续按钮
              ZButton(type = ZColorType.PRIMARY, modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp), onClick = { viewModel.startOrResumeDownload(model) }) {
                Icon(FeatherIcons.Play, null, modifier = Modifier.size(14.dp))
              }
              Spacer(Modifier.width(5.dp))
              // 取消按钮
              ZButton(type = ZColorType.DANGER, modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp), onClick = { viewModel.cancelDownload(model) }) {
                Icon(FeatherIcons.X, null, modifier = Modifier.size(14.dp))
              }
            }
          }

          DownloadState.EXTRACTING -> {
            Text("解压中...", style = MaterialTheme.typography.caption, color = Color.Blue)
          }

          DownloadState.COMPLETED -> {
            // 理论上这里会被上面的 if (isLocalExist) 拦截，作为兜底
            Text("完成", style = MaterialTheme.typography.caption, color = Color.Green)
          }
        }
      }
    }
  }
}

private fun buildProgressLabel(downloadedBytes: Long, totalBytes: Long, speedBytesPerSec: Double): String {
  val downloadedLabel = formatBytes(downloadedBytes)
  val totalLabel = if (totalBytes > 0L) formatBytes(totalBytes) else "未知"
  val speedLabel = formatSpeed(speedBytesPerSec)
  return "已下载 $downloadedLabel / $totalLabel · $speedLabel"
}

private fun formatBytes(bytes: Long): String {
  if (bytes <= 0L) return "0.0 MB"
  val mb = bytes / (1024.0 * 1024.0)
  return String.format(Locale.US, "%.1f MB", mb)
}

private fun formatSpeed(bytesPerSec: Double): String {
  if (bytesPerSec <= 0.0) return "0.0 MB/s"
  val mbPerSec = bytesPerSec / (1024.0 * 1024.0)
  return String.format(Locale.US, "%.1f MB/s", mbPerSec)
}
