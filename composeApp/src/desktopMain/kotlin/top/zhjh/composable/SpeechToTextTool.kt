package top.zhjh.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import top.zhjh.common.composable.ToastContainer
import top.zhjh.util.FilePickerUtil
import top.zhjh.viewmodel.SpeechToTextViewModel
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.isAppInDarkTheme

private val SupportedAudioExtensions = listOf(
  "wav", "mp3", "m4a", "aac", "flac", "ogg", "opus", "wma", "amr", "caf", "aif", "aiff", "au"
)

@Composable
fun SpeechToTextTool() {
  val viewModel: SpeechToTextViewModel = viewModel { SpeechToTextViewModel() }
  var showDownloadDialog by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()
  val isDarkTheme = isAppInDarkTheme()

  if (showDownloadDialog) {
    ModelManagerDialog(
      onCloseRequest = { showDownloadDialog = false },
      onModelReady = { path ->
        viewModel.updateModelDir(path)
        showDownloadDialog = false
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
        .padding(end = 12.dp)
        .verticalScroll(scrollState),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              ZText("语音转文本", fontWeight = FontWeight.Bold)
              ZText(
                "离线识别 · Sherpa-ONNX",
                style = MaterialTheme.typography.caption,
                color = if (isDarkTheme) Color(0xFFB0BAC5) else Color(0xFF909399)
              )
            }

            ZButton(
              type = ZColorType.SUCCESS,
              onClick = { showDownloadDialog = true },
              icon = { Icon(FeatherIcons.DownloadCloud, null, modifier = Modifier.size(14.dp)) }
            ) {
              ZText("在线下载模型")
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatusCard(
              title = "模型状态",
              status = viewModel.modelStatusText,
              detail = viewModel.modelTypeText,
              ready = viewModel.isModelReady,
              modifier = Modifier.weight(1f)
            )

            StatusCard(
              title = "音频状态",
              status = viewModel.audioStatusText,
              detail = viewModel.audioMetaText,
              ready = viewModel.isAudioReady,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          ZText("配置", fontWeight = FontWeight.Bold)

          ZForm(
            itemSpacing = 18.dp,
            labelPosition = ZFormLabelPosition.RIGHT,
            labelWidth = 92.dp
          ) {
            ZFormItem(label = "模型目录") {
              Row(verticalAlignment = Alignment.CenterVertically) {
                ZTextField(
                  value = viewModel.modelDir,
                  onValueChange = { viewModel.updateModelDir(it) },
                  placeholder = "选择模型目录（包含 onnx 与 tokens.txt）",
                  modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ZButton(
                  type = ZColorType.PRIMARY,
                  onClick = {
                    val path = FilePickerUtil.pickDirectory("选择模型目录")
                    if (path != null) {
                      viewModel.updateModelDir(path)
                    }
                  },
                  icon = { Icon(FeatherIcons.Folder, null, modifier = Modifier.size(16.dp)) }
                ) {
                  ZText("浏览")
                }
              }
            }

            ZFormItem(label = "音频文件") {
              Row(verticalAlignment = Alignment.CenterVertically) {
                ZTextField(
                  value = viewModel.audioPath,
                  onValueChange = { viewModel.updateAudioPath(it) },
                  placeholder = "支持 WAV/MP3/M4A/AAC/FLAC/OGG，自动重采样或转码",
                  modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ZButton(
                  type = ZColorType.PRIMARY,
                  onClick = {
                    val path = FilePickerUtil.pickFile("选择音频", SupportedAudioExtensions)
                    if (path != null) {
                      viewModel.updateAudioPath(path)
                    }
                  },
                  icon = { Icon(FeatherIcons.File, null, modifier = Modifier.size(16.dp)) }
                ) {
                  ZText("选择音频")
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ZButton(
              type = ZColorType.PRIMARY,
              enabled = viewModel.canConvert,
              loading = viewModel.isConverting,
              onClick = { viewModel.convert() },
              icon = { Icon(FeatherIcons.Play, null, modifier = Modifier.size(16.dp)) }
            ) {
              ZText(if (viewModel.isConverting) "识别中..." else "开始转换")
            }

            Spacer(modifier = Modifier.width(8.dp))

            ZButton(
              type = ZColorType.DEFAULT,
              enabled = viewModel.resultText.isNotBlank(),
              onClick = { viewModel.copyResult() },
              icon = { Icon(FeatherIcons.Copy, null, modifier = Modifier.size(14.dp)) }
            ) {
              ZText("复制结果")
            }

            Spacer(modifier = Modifier.width(8.dp))

            ZButton(
              type = ZColorType.DEFAULT,
              enabled = viewModel.resultText.isNotBlank(),
              onClick = { viewModel.clearResult() },
              icon = { Icon(FeatherIcons.Trash2, null, modifier = Modifier.size(14.dp)) }
            ) {
              ZText("清空结果")
            }

            Spacer(modifier = Modifier.weight(1f))

            ZText(
              viewModel.progressInfo,
              style = MaterialTheme.typography.caption,
              color = if (isDarkTheme) Color(0xFFB0BAC5) else Color(0xFF909399)
            )
          }
        }
      }

      ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ZText("转写结果", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            ZText(
              buildResultSummary(viewModel.resultText),
              style = MaterialTheme.typography.caption,
              color = if (isDarkTheme) Color(0xFFB0BAC5) else Color(0xFF909399)
            )
          }

          ZTextField(
            value = viewModel.resultText,
            onValueChange = { viewModel.resultText = it },
            type = ZTextFieldType.TEXTAREA,
            resize = false,
            modifier = Modifier
              .fillMaxWidth()
              .height(220.dp),
            placeholder = "识别结果将显示在这里"
          )
        }
      }
    }

    VerticalScrollbar(
      modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
      adapter = rememberScrollbarAdapter(scrollState)
    )

    ToastContainer()
  }
}

@Composable
private fun StatusCard(
  title: String,
  status: String,
  detail: String,
  ready: Boolean,
  modifier: Modifier = Modifier
) {
  val isDarkTheme = isAppInDarkTheme()
  val shape = RoundedCornerShape(4.dp)
  val borderColor = if (ready) {
    if (isDarkTheme) Color(0xFF4E8E2F) else Color(0xFF95D475)
  } else {
    if (isDarkTheme) Color(0xFF854040) else Color(0xFFF89898)
  }
  val backgroundColor = if (ready) {
    if (isDarkTheme) Color(0x332E7D32) else Color(0x1A67C23A)
  } else {
    if (isDarkTheme) Color(0x33B25252) else Color(0x1AF56C6C)
  }
  val titleColor = if (isDarkTheme) Color(0xFFB0BAC5) else Color(0xFF606266)
  val detailColor = if (isDarkTheme) Color(0xFF8D9095) else Color(0xFF909399)

  Row(
    modifier = modifier
      .background(backgroundColor, shape)
      .border(1.dp, borderColor, shape)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (ready) FeatherIcons.CheckCircle else FeatherIcons.AlertCircle,
      contentDescription = null,
      tint = borderColor,
      modifier = Modifier.size(14.dp)
    )

    Spacer(modifier = Modifier.width(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
      ZText(title, style = MaterialTheme.typography.caption, color = titleColor)
      ZText(status, color = borderColor)
      ZText(detail, style = MaterialTheme.typography.caption, color = detailColor)
    }
  }
}

private fun buildResultSummary(text: String): String {
  if (text.isBlank()) {
    return "0 字符 · 0 行"
  }
  return "${text.length} 字符 · ${text.lines().size} 行"
}
