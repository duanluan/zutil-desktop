package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k2fsa.sherpa.onnx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.zhjh.common.composable.ToastManager
import java.io.File
import javax.sound.sampled.AudioSystem

class SpeechToTextViewModel : ViewModel() {
  var audioPath by mutableStateOf("")
  var modelDir by mutableStateOf("")
  var resultText by mutableStateOf("")
  var isConverting by mutableStateOf(false)
  var progressInfo by mutableStateOf("准备就绪")

  fun convert() {
    if (audioPath.isBlank() || modelDir.isBlank()) {
      ToastManager.error("请先选择音频文件和模型目录")
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      isConverting = true
      progressInfo = "正在初始化模型..."
      resultText = ""

      try {
        // 1. 自动识别模型类型并创建配置
        val modelConfig = createModelConfig(modelDir)
        if (modelConfig == null) {
          withContext(Dispatchers.Main) {
            ToastManager.error("无法识别模型类型，请确保文件夹名称包含 sense-voice, paraformer, whisper 或 zipformer")
          }
          return@launch
        }

        // 2. 创建识别器配置
        val config = OfflineRecognizerConfig.builder()
          .setOfflineModelConfig(modelConfig)
          .setDecodingMethod("greedy_search")
          .build()

        // 3. 创建识别器
        val recognizer = OfflineRecognizer(config)

        // 4. 读取音频
        progressInfo = "正在转换..."
        val stream = recognizer.createStream()

        try {
          val (samples, sampleRate) = readWavFile(File(audioPath))
          stream.acceptWaveform(samples, sampleRate)
        } catch (e: Exception) {
          e.printStackTrace()
          withContext(Dispatchers.Main) {
            ToastManager.error("音频读取失败: ${e.message}")
          }
          stream.release()
          recognizer.release()
          return@launch
        }

        // 5. 解码
        recognizer.decode(stream)
        val result = recognizer.getResult(stream)

        withContext(Dispatchers.Main) {
          resultText = result.text
          progressInfo = "转换完成"
          ToastManager.success("转换成功")
        }

        stream.release()
        recognizer.release()

      } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
          progressInfo = "转换失败: ${e.message}"
          ToastManager.error("错误: ${e.message}")
        }
      } finally {
        isConverting = false
      }
    }
  }

  // 根据文件夹名称猜测模型类型，并构建对应的 Config
  private fun createModelConfig(path: String): OfflineModelConfig? {
    val dirName = File(path).name.lowercase()
    val builder = OfflineModelConfig.builder()
      .setNumThreads(1)
      .setDebug(true)
      .setProvider("cpu")

    // 查找 tokens.txt (大部分模型都需要)
    val tokensFile = File(path, "tokens.txt").absolutePath

    return when {
      // A. SenseVoice
      dirName.contains("sense-voice") || dirName.contains("sense_voice") -> {
        val modelFile = findFile(path, "model.int8.onnx", "model.onnx") ?: return null
        val senseVoiceConfig = OfflineSenseVoiceModelConfig.builder()
          .setModel(modelFile)
          .setLanguage("")
          .setInverseTextNormalization(true)
          .build()
        builder.setSenseVoice(senseVoiceConfig)
          .setTokens(tokensFile)
          .setModelType("sense_voice")
          .build()
      }

      // B. Paraformer
      dirName.contains("paraformer") -> {
        val modelFile = findFile(path, "model.int8.onnx", "model.onnx") ?: return null
        val paraformerConfig = OfflineParaformerModelConfig.builder()
          .setModel(modelFile)
          .build()
        builder.setParaformer(paraformerConfig)
          .setTokens(tokensFile)
          .setModelType("paraformer")
          .build()
      }

      // C. Whisper
      dirName.contains("whisper") -> {
        // Whisper 需要 encoder 和 decoder
        val encoder = findFile(path, "tiny-encoder.int8.onnx", "tiny-encoder.onnx", "encoder.int8.onnx", "encoder.onnx") ?: return null
        val decoder = findFile(path, "tiny-decoder.int8.onnx", "tiny-decoder.onnx", "decoder.int8.onnx", "decoder.onnx") ?: return null

        val whisperConfig = OfflineWhisperModelConfig.builder()
          .setEncoder(encoder)
          .setDecoder(decoder)
          .build()

        builder.setWhisper(whisperConfig)
          .setTokens(tokensFile)
          .setModelType("whisper")
          .build()
      }

      // D. Zipformer / Transducer
      dirName.contains("zipformer") -> {
        val modelFile = findFile(path, "encoder-epoch-99-avg-1.int8.onnx", "encoder-epoch-99-avg-1.onnx", "model.int8.onnx", "model.onnx") ?: return null
        // Zipformer 通常是 Transducer 架构
        val transducerConfig = OfflineTransducerModelConfig.builder()
          .setEncoder(modelFile)
          .setDecoder(modelFile) // Zipformer 有时把两者合一，或者需要具体看文件结构，此处简化处理
          .setJoiner(findFile(path, "joiner-epoch-99-avg-1.int8.onnx", "joiner-epoch-99-avg-1.onnx", "joiner.int8.onnx", "joiner.onnx") ?: "")
          .build()

        builder.setTransducer(transducerConfig)
          .setTokens(tokensFile)
          .setModelType("zipformer") // 或者 transducer
          .build()
      }

      else -> null
    }
  }

  // 辅助方法：在目录下查找多个可能的文件名，返回第一个存在的完整路径
  private fun findFile(dir: String, vararg fileNames: String): String? {
    val folder = File(dir)
    for (name in fileNames) {
      val f = File(folder, name)
      if (f.exists()) return f.absolutePath
    }
    // 如果找不到精确匹配，尝试模糊搜索 .onnx
    if (fileNames.any { it.contains("model") }) {
      val onnxFiles = folder.listFiles { _, name -> name.endsWith(".onnx") }
      if (onnxFiles != null && onnxFiles.isNotEmpty()) return onnxFiles[0].absolutePath
    }
    return null
  }

  private fun readWavFile(file: File): Pair<FloatArray, Int> {
    AudioSystem.getAudioInputStream(file).use { ais ->
      val format = ais.format
      val sampleRate = format.sampleRate.toInt()
      if (format.sampleSizeInBits != 16) throw UnsupportedOperationException("只支持 16-bit WAV")
      val bytes = ais.readAllBytes()
      val floatArray = FloatArray(bytes.size / 2)
      val isBigEndian = format.isBigEndian
      for (i in floatArray.indices) {
        val b1 = bytes[i * 2].toInt()
        val b2 = bytes[i * 2 + 1].toInt()
        val sampleShort = if (isBigEndian) ((b1 shl 8) or (b2 and 0xFF)) else ((b2 shl 8) or (b1 and 0xFF))
        floatArray[i] = sampleShort.toShort() / 32768.0f
      }
      return Pair(floatArray, sampleRate)
    }
  }
}
