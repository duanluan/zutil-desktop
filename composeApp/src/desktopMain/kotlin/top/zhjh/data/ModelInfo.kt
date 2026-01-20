package top.zhjh.data

data class ModelInfo(
  val id: String,
  val name: String,
  val description: String,
  val type: String, // "sense_voice", "paraformer", "whisper", "zipformer"
  val language: String,
  val fileName: String,
  val downloadUrl: String,
  val sizeMb: String
)

object ModelConstants {
  // 基础下载路径 (Sherpa-ONNX 官方模型仓库)
  private const val BASE_URL = "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models"

  val availableModels = listOf(
    ModelInfo(
      id = "sense_voice_small",
      name = "SenseVoiceSmall (多语言/推荐)",
      description = "阿里通义实验室出品。支持中、英、日、韩、粤语，带有情感识别，速度极快，精度高。",
      type = "sense_voice",
      language = "多语言",
      // ✅ 使用 int8 量化版，体积小速度快
      fileName = "sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2024-07-17.tar.bz2",
      downloadUrl = "$BASE_URL/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2024-07-17.tar.bz2",
      sizeMb = "155 MB"
    ),
    ModelInfo(
      id = "paraformer_zh",
      name = "Paraformer (中文/极速)",
      description = "阿里工业级模型。非自回归架构，中文识别非常稳定，推理延迟极低，适合纯中文场景。",
      type = "paraformer",
      language = "中文",
      fileName = "sherpa-onnx-paraformer-zh-2023-09-14.tar.bz2",
      downloadUrl = "$BASE_URL/sherpa-onnx-paraformer-zh-2023-09-14.tar.bz2",
      sizeMb = "223 MB"
    ),
    ModelInfo(
      id = "whisper_tiny",
      name = "Whisper Tiny (通用)",
      description = "OpenAI 出品。全球语言通用性好，Tiny 版本体积小，适合低配设备。",
      type = "whisper",
      language = "多语言",
      fileName = "sherpa-onnx-whisper-tiny.tar.bz2",
      downloadUrl = "$BASE_URL/sherpa-onnx-whisper-tiny.tar.bz2",
      sizeMb = "111 MB"
    ),
    ModelInfo(
      id = "zipformer_en",
      name = "Zipformer (英文)",
      description = "Sherpa 团队自研模型。针对英文优化，识别率极高，适合英语会议记录。",
      type = "zipformer",
      language = "英语",
      fileName = "sherpa-onnx-zipformer-en-2023-06-26.tar.bz2",
      downloadUrl = "$BASE_URL/sherpa-onnx-zipformer-en-2023-06-26.tar.bz2",
      sizeMb = "293 MB"
    )
  )
}
