package top.zhjh.composable

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.DownloadCloud
import compose.icons.feathericons.File
import compose.icons.feathericons.Folder
import compose.icons.feathericons.Play
import top.zhjh.common.composable.ToastContainer
import top.zhjh.util.FilePickerUtil
import top.zhjh.viewmodel.SpeechToTextViewModel
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType

@Composable
fun SpeechToTextTool() {
  val viewModel: SpeechToTextViewModel = viewModel { SpeechToTextViewModel() }
  var showDownloadDialog by remember { mutableStateOf(false) }

  // 定义滚动状态
  val scrollState = rememberScrollState()

  if (showDownloadDialog) {
    ModelManagerDialog(
      onCloseRequest = { showDownloadDialog = false },
      onModelReady = { path ->
        viewModel.modelDir = path
        showDownloadDialog = false
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .padding(10.dp)
        .padding(end = 12.dp) // 右侧留出一点空间给滚动条
        .verticalScroll(scrollState), // 绑定滚动状态
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

      // ==================== 配置区域 ====================
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            // 【修改点1】加大加粗标题
            ZText("配置", style = MaterialTheme.typography.h3, fontWeight = FontWeight.Bold)

            ZButton(type = ZColorType.SUCCESS, modifier = Modifier.height(30.dp), onClick = { showDownloadDialog = true }) {
              Icon(FeatherIcons.DownloadCloud, null, modifier = Modifier.size(14.dp))
              Spacer(Modifier.width(4.dp))
              Text("在线下载模型")
            }
          }

          // ... (选择模型目录和音频文件的代码保持不变) ...
          // 1. 选择模型目录
          Row(verticalAlignment = Alignment.CenterVertically) {
            ZTextField(
              value = viewModel.modelDir,
              onValueChange = { viewModel.modelDir = it },
              placeholder = "选择 SenseVoice 模型目录 (包含 model.int8.onnx)",
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ZButton(type = ZColorType.PRIMARY, onClick = {
              val path = FilePickerUtil.pickDirectory("选择模型文件夹")
              if (path != null) viewModel.modelDir = path
            }) {
              Icon(FeatherIcons.Folder, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text("浏览")
            }
          }

          // 2. 选择音频文件
          Row(verticalAlignment = Alignment.CenterVertically) {
            ZTextField(
              value = viewModel.audioPath,
              onValueChange = { viewModel.audioPath = it },
              placeholder = "选择 wav 音频文件 (16k, 16bit)",
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ZButton(type = ZColorType.PRIMARY, onClick = {
              val path = FilePickerUtil.pickFile("选择音频", listOf("wav"))
              if (path != null) viewModel.audioPath = path
            }) {
              Icon(FeatherIcons.File, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text("选择音频")
            }
          }

          // 3. 开始转换按钮
          Row(verticalAlignment = Alignment.CenterVertically) {
            ZButton(
              type = ZColorType.PRIMARY,
              enabled = !viewModel.isConverting,
              onClick = { viewModel.convert() }
            ) {
              Icon(FeatherIcons.Play, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(if (viewModel.isConverting) "转换中..." else "开始转换")
            }

            Spacer(Modifier.width(10.dp))
            Text(viewModel.progressInfo, style = MaterialTheme.typography.caption)
          }
        }
      }

      // ==================== 结果输出区域 ====================
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // 【修改点1】加大加粗标题
          ZText("转换结果", style = MaterialTheme.typography.h3, fontWeight = FontWeight.Bold)

          ZTextField(
            value = viewModel.resultText,
            onValueChange = { viewModel.resultText = it },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            singleLine = false,
            maxLines = 20
          )
        }
      }

      // ==================== 简介 ====================
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        // 【修改点1】加大加粗标题
        ZText("说明", style = MaterialTheme.typography.h3, fontWeight = FontWeight.Bold)
        ZParagraph("基于 Sherpa-ONNX 引擎和 SenseVoiceSmall 模型。", indent = false)
        ZParagraph("请确保音频文件为 16kHz 采样率、16-bit 单声道的 WAV 格式。", indent = false)
        ZParagraph("你需要从 ModelScope 或 GitHub 下载对应的 ONNX 模型文件并解压到本地。", indent = false)
      }
    }

    // 显式添加垂直滚动条
    VerticalScrollbar(
      modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
      adapter = rememberScrollbarAdapter(scrollState)
    )

    ToastContainer()
  }
}
