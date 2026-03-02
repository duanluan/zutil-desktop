package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig
import com.k2fsa.sherpa.onnx.OfflineStream
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import java.lang.UnsatisfiedLinkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.csaf.awt.ClipboardUtil
import top.zhjh.common.composable.ToastManager
import top.zhjh.util.ModelDownloadSettings
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

private const val TARGET_SAMPLE_RATE = 16_000
private const val TARGET_SAMPLE_SIZE = 16
private const val TARGET_CHANNELS = 1
private const val FFMPEG_TIMEOUT_SECONDS = 180L
private const val RECOGNIZER_INIT_TIMEOUT_SECONDS = 90L
private const val SHERPA_NATIVE_PATH_PROP = "sherpa_onnx.native.path"
private const val SHERPA_NATIVE_PRELOADED_PROP = "zutil.sherpa.native.preloaded"
private val DEBUG_LOG_FILE = File(System.getProperty("java.io.tmpdir"), "zutil-stt.log")
private val NATIVE_CACHE_DIR = File(System.getProperty("java.io.tmpdir"), "zutil-sherpa-native")

@Volatile
private var sherpaNativePreloaded = false

private val COMMON_AUDIO_EXTENSIONS = setOf(
  "wav", "mp3", "m4a", "aac", "flac", "ogg", "opus", "wma", "amr", "caf", "aif", "aiff", "au"
)

private enum class AsrModelType(val label: String) {
  SENSE_VOICE("SenseVoice"),
  PARAFORMER("Paraformer"),
  WHISPER("Whisper"),
  ZIPFORMER("Zipformer")
}

private data class AudioMeta(
  val sampleRate: Int,
  val sampleSizeInBits: Int,
  val channels: Int,
  val durationSeconds: Double
)

class SpeechToTextViewModel : ViewModel() {
  var audioPath by mutableStateOf("")
    private set
  var modelDir by mutableStateOf(ModelDownloadSettings.loadOrDefault())
    private set
  var resultText by mutableStateOf("")
  var isConverting by mutableStateOf(false)
  var progressInfo by mutableStateOf("准备就绪")

  var isModelReady by mutableStateOf(false)
    private set
  var modelStatusText by mutableStateOf("请选择模型目录")
    private set
  var modelTypeText by mutableStateOf("未检测到可用模型")
    private set

  var isAudioReady by mutableStateOf(false)
    private set
  var audioStatusText by mutableStateOf("请选择音频文件")
    private set
  var audioMetaText by mutableStateOf("支持 WAV/MP3/M4A/AAC/FLAC/OGG 等")
    private set

  val canConvert: Boolean
    get() = isModelReady && isAudioReady && !isConverting

  private var ffmpegAvailableCache: Boolean? = null

  init {
    refreshModelState()
    refreshAudioState()
  }

  fun updateModelDir(path: String) {
    val normalized = normalizePath(path)
    updateState { modelDir = normalized }
    refreshModelState()
    if (isModelReady && normalized.isNotBlank()) {
      ModelDownloadSettings.save(normalized)
    }
  }

  fun updateAudioPath(path: String) {
    updateState { audioPath = normalizePath(path) }
    refreshAudioState()
  }

  fun clearResult() {
    updateState { resultText = "" }
    ToastManager.success("已清空结果")
  }

  fun copyResult() {
    if (resultText.isBlank()) {
      ToastManager.show("暂无可复制内容")
      return
    }

    if (ClipboardUtil.set(resultText)) {
      ToastManager.success("结果已复制到剪贴板")
    } else {
      ToastManager.error("复制失败")
    }
  }

  fun convert() {
    refreshModelState()
    refreshAudioState()

    if (!isModelReady) {
      ToastManager.error(modelStatusText)
      return
    }
    if (!isAudioReady) {
      ToastManager.error(audioStatusText)
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      updateState {
        isConverting = true
        progressInfo = "正在初始化识别器..."
        resultText = ""
      }
      logDebug("convert-start modelDir=$modelDir audioPath=$audioPath")

      var recognizer: OfflineRecognizer? = null
      var stream: OfflineStream? = null

      try {
        configureSherpaNativePathIfNeeded()
        logDebug("build-model-config-start")
        val modelConfig = createModelConfig(modelDir)
          ?: throw IllegalStateException("模型目录结构不完整，请检查模型文件")
        logDebug("build-model-config-ok")

        val config = OfflineRecognizerConfig.builder()
          .setOfflineModelConfig(modelConfig)
          .setDecodingMethod("greedy_search")
          .build()
        logDebug("build-recognizer-config-ok")
        logDebug(
          "runtime-info os=${System.getProperty("os.name")} arch=${System.getProperty("os.arch")} " +
            "java=${System.getProperty("java.version")} javaHome=${System.getProperty("java.home")} " +
            "nativePath=${System.getProperty(SHERPA_NATIVE_PATH_PROP)}"
        )

        updateState { progressInfo = "正在加载模型文件..." }
        logDebug("recognizer-init-start")
        recognizer = createRecognizerWithTimeout(config)
        logDebug("recognizer-init-ok")

        stream = recognizer.createStream()
        logDebug("stream-create-ok")

        updateState { progressInfo = "正在读取音频..." }
        logDebug("audio-stream-start")
        streamAudioFile(File(audioPath), stream)
        logDebug("audio-stream-ok")

        updateState { progressInfo = "正在识别语音..." }
        logDebug("decode-start")
        recognizer.decode(stream)
        val result = recognizer.getResult(stream).text.orEmpty().trim()
        logDebug("decode-ok resultLength=${result.length}")

        updateState {
          resultText = result
          progressInfo = if (result.isBlank()) "识别完成（未识别到文本）" else "识别完成"
        }

        if (result.isBlank()) {
          ToastManager.show("识别完成，但没有提取到可用文本")
        } else {
          ToastManager.success("识别完成")
        }
      } catch (e: Exception) {
        var message = e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
        if (looksLikeOnnxRuntimeLoadFailure(e) && isWindows()) {
          message = "onnxruntime initialization failed. Please install Microsoft Visual C++ 2015-2022 Redistributable (x64) and retry."
        }
        logDebug("convert-error message=$message")
        logDebug("convert-error-stack ${stackTraceToSingleLine(e)}")
        updateState { progressInfo = "识别失败: $message" }
        ToastManager.error("识别失败: $message")
      } finally {
        runCatching { stream?.release() }
        runCatching { recognizer?.release() }
        updateState { isConverting = false }
        logDebug("convert-end")
      }
    }
  }

  private fun refreshModelState() {
    val path = modelDir.trim()
    if (path.isEmpty()) {
      updateState {
        isModelReady = false
        modelStatusText = "请选择模型目录"
        modelTypeText = "未检测到可用模型"
      }
      return
    }

    val dir = File(path)
    if (!dir.exists() || !dir.isDirectory) {
      updateState {
        isModelReady = false
        modelStatusText = "模型目录不存在"
        modelTypeText = "请重新选择模型目录"
      }
      return
    }

    if (looksLikeModelRootDirectory(dir)) {
      updateState {
        isModelReady = false
        modelStatusText = "请选择具体模型子目录"
        modelTypeText = "当前目录更像模型根目录"
      }
      return
    }

    val modelType = detectModelType(path)
    if (modelType == null) {
      updateState {
        isModelReady = false
        modelStatusText = "无法识别模型类型"
        modelTypeText = "支持 SenseVoice / Paraformer / Whisper / Zipformer"
      }
      return
    }

    val config = createModelConfig(path)
    if (config == null) {
      updateState {
        isModelReady = false
        modelStatusText = "模型文件不完整"
        modelTypeText = "${modelType.label} 文件缺失，请检查目录"
      }
      return
    }

    updateState {
      isModelReady = true
      modelStatusText = "模型已就绪"
      modelTypeText = modelType.label
    }
  }

  private fun refreshAudioState() {
    val path = audioPath.trim()
    if (path.isEmpty()) {
      updateState {
        isAudioReady = false
        audioStatusText = "请选择音频文件"
        audioMetaText = "支持 WAV/MP3/M4A/AAC/FLAC/OGG 等"
      }
      return
    }

    val file = File(path)
    if (!file.exists() || !file.isFile) {
      updateState {
        isAudioReady = false
        audioStatusText = "音频文件不存在"
        audioMetaText = "请重新选择可用音频文件"
      }
      return
    }

    val extension = file.extension.lowercase(Locale.getDefault())
    val meta = readAudioMeta(file)
    val directSupported = canReadByJavaSound(file)

    if (directSupported) {
      val currentMeta = meta?.let { buildAudioMetaText(it) } ?: "将自动解码为 16kHz / 16bit / 单声道"
      val needResample = meta?.let { !isTargetAudioMeta(it) } ?: true
      updateState {
        isAudioReady = true
        audioStatusText = if (needResample) "音频已就绪（将自动重采样）" else "音频已就绪"
        audioMetaText = currentMeta
      }
      return
    }

    if (isFfmpegAvailable()) {
      updateState {
        isAudioReady = true
        audioStatusText = "音频已就绪（将使用 FFmpeg 转码）"
        audioMetaText = if (extension.isBlank()) {
          "转码目标: WAV / 16kHz / 16bit / 单声道"
        } else {
          "当前: .$extension，转码目标: WAV / 16kHz / 16bit / 单声道"
        }
      }
      return
    }

    val isCommonFormat = extension in COMMON_AUDIO_EXTENSIONS
    updateState {
      isAudioReady = false
      audioStatusText = if (isCommonFormat) {
        "当前格式需要 FFmpeg 转码（未检测到 ffmpeg）"
      } else {
        "暂不支持该音频格式"
      }
      audioMetaText = "请安装 ffmpeg 并加入 PATH 后重试"
    }
  }

  private fun createModelConfig(path: String): OfflineModelConfig? {
    val modelType = detectModelType(path) ?: return null
    val tokensPath = findFile(path, "tokens.txt") ?: return null
    val builder = OfflineModelConfig.builder()
      .setNumThreads(1)
      .setDebug(false)
      .setProvider("cpu")

    return when (modelType) {
      AsrModelType.SENSE_VOICE -> {
        val modelFile = findFile(path, "model.int8.onnx", "model.onnx") ?: return null
        val senseVoiceConfig = OfflineSenseVoiceModelConfig.builder()
          .setModel(modelFile)
          .setLanguage("")
          .setInverseTextNormalization(true)
          .build()
        builder.setSenseVoice(senseVoiceConfig)
          .setTokens(tokensPath)
          .setModelType("sense_voice")
          .build()
      }

      AsrModelType.PARAFORMER -> {
        val modelFile = findFile(path, "model.int8.onnx", "model.onnx") ?: return null
        val paraformerConfig = OfflineParaformerModelConfig.builder()
          .setModel(modelFile)
          .build()
        builder.setParaformer(paraformerConfig)
          .setTokens(tokensPath)
          .setModelType("paraformer")
          .build()
      }

      AsrModelType.WHISPER -> {
        val encoder = findFile(path, "tiny-encoder.int8.onnx", "tiny-encoder.onnx", "encoder.int8.onnx", "encoder.onnx")
          ?: return null
        val decoder = findFile(path, "tiny-decoder.int8.onnx", "tiny-decoder.onnx", "decoder.int8.onnx", "decoder.onnx")
          ?: return null
        val whisperConfig = OfflineWhisperModelConfig.builder()
          .setEncoder(encoder)
          .setDecoder(decoder)
          .build()
        builder.setWhisper(whisperConfig)
          .setTokens(tokensPath)
          .setModelType("whisper")
          .build()
      }

      AsrModelType.ZIPFORMER -> {
        val encoder = findFile(path, "encoder-epoch-99-avg-1.int8.onnx", "encoder-epoch-99-avg-1.onnx", "encoder.int8.onnx", "encoder.onnx")
          ?: return null
        val decoder = findFile(path, "decoder-epoch-99-avg-1.int8.onnx", "decoder-epoch-99-avg-1.onnx", "decoder.int8.onnx", "decoder.onnx")
          ?: return null
        val joiner = findFile(path, "joiner-epoch-99-avg-1.int8.onnx", "joiner-epoch-99-avg-1.onnx", "joiner.int8.onnx", "joiner.onnx")
          ?: return null

        val transducerConfig = OfflineTransducerModelConfig.builder()
          .setEncoder(encoder)
          .setDecoder(decoder)
          .setJoiner(joiner)
          .build()

        builder.setTransducer(transducerConfig)
          .setTokens(tokensPath)
          .setModelType("zipformer")
          .build()
      }
    }
  }

  private fun detectModelType(path: String): AsrModelType? {
    val dir = File(path)
    val dirName = dir.name.lowercase(Locale.getDefault())
    val names = dir.listFiles()?.map { it.name.lowercase(Locale.getDefault()) }.orEmpty()

    fun containsKeyword(keyword: String): Boolean {
      return dirName.contains(keyword) || names.any { it.contains(keyword) }
    }

    fun hasAny(vararg fileNames: String): Boolean {
      return fileNames.any { expected -> names.any { it == expected.lowercase(Locale.getDefault()) } }
    }

    val whisperDetected =
      hasAny("tiny-encoder.int8.onnx", "tiny-encoder.onnx", "encoder.int8.onnx", "encoder.onnx") &&
        hasAny("tiny-decoder.int8.onnx", "tiny-decoder.onnx", "decoder.int8.onnx", "decoder.onnx")
    if (whisperDetected || containsKeyword("whisper")) {
      return AsrModelType.WHISPER
    }

    val zipformerDetected =
      hasAny("joiner.int8.onnx", "joiner.onnx", "joiner-epoch-99-avg-1.int8.onnx", "joiner-epoch-99-avg-1.onnx") &&
        hasAny("encoder.int8.onnx", "encoder.onnx", "encoder-epoch-99-avg-1.int8.onnx", "encoder-epoch-99-avg-1.onnx")
    if (zipformerDetected || containsKeyword("zipformer")) {
      return AsrModelType.ZIPFORMER
    }

    if (containsKeyword("sense-voice") || containsKeyword("sense_voice") || containsKeyword("sensevoice")) {
      return AsrModelType.SENSE_VOICE
    }

    if (containsKeyword("paraformer")) {
      return AsrModelType.PARAFORMER
    }

    if (hasAny("model.int8.onnx", "model.onnx") && hasAny("tokens.txt")) {
      return AsrModelType.SENSE_VOICE
    }

    return null
  }

  private fun findFile(dir: String, vararg fileNames: String): String? {
    val folder = File(dir)
    val children = folder.listFiles().orEmpty()
    fileNames.firstNotNullOfOrNull { expected ->
      children.firstOrNull { it.isFile && it.name.equals(expected, ignoreCase = true) }?.absolutePath
    }?.let { return it }

    if (fileNames.any { it.contains("model", ignoreCase = true) }) {
      val modelNamedOnnx = children.filter {
        it.isFile &&
          it.name.endsWith(".onnx", ignoreCase = true) &&
          it.name.contains("model", ignoreCase = true)
      }
      if (modelNamedOnnx.size == 1) {
        return modelNamedOnnx[0].absolutePath
      }

      val allOnnx = children.filter { it.isFile && it.name.endsWith(".onnx", ignoreCase = true) }
      if (allOnnx.size == 1) {
        return allOnnx[0].absolutePath
      }
    }

    return null
  }

  private fun looksLikeModelRootDirectory(dir: File): Boolean {
    val children = dir.listFiles().orEmpty()
    if (children.none { it.isDirectory }) {
      return false
    }

    val fileNames = children
      .filter { it.isFile }
      .map { it.name.lowercase(Locale.getDefault()) }
    val hasCurrentDirModelFile = fileNames.any { it.endsWith(".onnx") }
    val hasCurrentDirTokens = fileNames.any { it == "tokens.txt" }
    if (hasCurrentDirModelFile && hasCurrentDirTokens) {
      return false
    }

    return children.any { child ->
      if (!child.isDirectory) {
        false
      } else {
        val name = child.name.lowercase(Locale.getDefault())
        name.contains("sense") ||
          name.contains("paraformer") ||
          name.contains("whisper") ||
          name.contains("zipformer")
      }
    }
  }

  private fun createRecognizerWithTimeout(config: OfflineRecognizerConfig): OfflineRecognizer {
    val firstAttempt = runCatching { createRecognizerWithTimeoutOnce(config) }
    if (firstAttempt.isSuccess) {
      return firstAttempt.getOrThrow()
    }

    val firstError = firstAttempt.exceptionOrNull()
    if (firstError != null && looksLikeOnnxRuntimeLoadFailure(firstError)) {
      logDebug("recognizer-init-retry-clean-temp")
      cleanupSherpaTempDirs()
      return createRecognizerWithTimeoutOnce(config)
    }

    throw (firstError ?: IllegalStateException("Recognizer initialization failed"))
  }

  private fun createRecognizerWithTimeoutOnce(config: OfflineRecognizerConfig): OfflineRecognizer {
    val executor = Executors.newSingleThreadExecutor()
    return try {
      val future = executor.submit<OfflineRecognizer> { OfflineRecognizer(config) }
      future.get(RECOGNIZER_INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    } catch (_: TimeoutException) {
      throw IllegalStateException("识别器初始化超时，请稍后重试")
    } catch (e: ExecutionException) {
      val causeMessage = e.cause?.message?.takeIf { it.isNotBlank() } ?: e.message
      throw IllegalStateException(causeMessage ?: "识别器初始化失败")
    } finally {
      executor.shutdownNow()
    }
  }

  private fun looksLikeOnnxRuntimeLoadFailure(error: Throwable): Boolean {
    val message = buildString {
      append(error.message.orEmpty())
      append(" ")
      append(error.cause?.message.orEmpty())
    }.lowercase(Locale.getDefault())
    return message.contains("onnxruntime.dll")
  }

  private fun cleanupSherpaTempDirs() {
    val tempDir = File(System.getProperty("java.io.tmpdir"))
    val dirs = tempDir.listFiles()
      ?.filter { it.isDirectory && it.name.startsWith("sherpa-onnx-java", ignoreCase = true) }
      .orEmpty()
    dirs.forEach { dir ->
      runCatching { deleteRecursively(dir) }
    }
  }

  private fun deleteRecursively(file: File) {
    if (file.isDirectory) {
      file.listFiles()?.forEach { child ->
        deleteRecursively(child)
      }
    }
    file.delete()
  }

  private fun readAudioMeta(file: File): AudioMeta? {
    return runCatching {
      val fileFormat = AudioSystem.getAudioFileFormat(file)
      val format = fileFormat.format
      val frameRate = format.frameRate.toDouble()
      val frameLength = fileFormat.frameLength.toDouble()
      val durationSeconds = if (frameRate > 0.0 && frameLength > 0.0) frameLength / frameRate else 0.0
      AudioMeta(
        sampleRate = format.sampleRate.toInt(),
        sampleSizeInBits = format.sampleSizeInBits,
        channels = format.channels,
        durationSeconds = durationSeconds
      )
    }.getOrNull()
  }

  private fun buildAudioMetaText(meta: AudioMeta): String {
    val channelText = if (meta.channels == 1) "单声道" else "${meta.channels}声道"
    return "${meta.sampleRate}Hz · ${meta.sampleSizeInBits}bit · $channelText · ${formatDuration(meta.durationSeconds)}"
  }

  private fun isTargetAudioMeta(meta: AudioMeta): Boolean {
    return meta.sampleRate == TARGET_SAMPLE_RATE &&
      meta.sampleSizeInBits == TARGET_SAMPLE_SIZE &&
      meta.channels == TARGET_CHANNELS
  }

  private fun formatDuration(seconds: Double): String {
    if (seconds.isNaN() || seconds.isInfinite() || seconds <= 0.0) {
      return "--:--"
    }
    val sec = seconds.toInt()
    val minute = sec / 60
    val remainSec = sec % 60
    val decimal = ((seconds - sec) * 10).toInt().coerceIn(0, 9)
    return String.format(Locale.US, "%02d:%02d.%01d", minute, remainSec, decimal)
  }

  private fun streamAudioFile(file: File, stream: OfflineStream) {
    val handledByJavaSound = tryStreamByJavaSound(file, stream)
    if (handledByJavaSound) {
      return
    }

    if (!isFfmpegAvailable()) {
      throw UnsupportedOperationException("该音频格式需要 FFmpeg 转码，请先安装 ffmpeg 并加入 PATH")
    }

    updateState { progressInfo = "正在调用 FFmpeg 转码..." }
    logDebug("ffmpeg-transcode-start file=${file.absolutePath}")
    val tempWav = convertAudioToTargetWavWithFfmpeg(file)
    try {
      streamWavFile(tempWav, stream)
    } finally {
      runCatching { tempWav.delete() }
      logDebug("ffmpeg-transcode-end")
    }
  }

  private fun tryStreamByJavaSound(file: File, stream: OfflineStream): Boolean {
    return try {
      AudioSystem.getAudioInputStream(file).use { sourceStream ->
        val sourceFormat = sourceStream.format
        if (isTargetPcmFormat(sourceFormat)) {
          streamPcmAudio(sourceStream, stream)
        } else {
          val targetFormat = targetPcmFormat()
          AudioSystem.getAudioInputStream(targetFormat, sourceStream).use { converted ->
            streamPcmAudio(converted, stream)
          }
        }
      }
      true
    } catch (_: Exception) {
      false
    }
  }

  private fun canReadByJavaSound(file: File): Boolean {
    return try {
      AudioSystem.getAudioInputStream(file).use { sourceStream ->
        if (isTargetPcmFormat(sourceStream.format)) {
          true
        } else {
          AudioSystem.getAudioInputStream(targetPcmFormat(), sourceStream).use { true }
        }
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun targetPcmFormat(): AudioFormat {
    return AudioFormat(
      AudioFormat.Encoding.PCM_SIGNED,
      TARGET_SAMPLE_RATE.toFloat(),
      TARGET_SAMPLE_SIZE,
      TARGET_CHANNELS,
      TARGET_CHANNELS * (TARGET_SAMPLE_SIZE / 8),
      TARGET_SAMPLE_RATE.toFloat(),
      false
    )
  }

  private fun isTargetPcmFormat(format: AudioFormat): Boolean {
    return format.encoding == AudioFormat.Encoding.PCM_SIGNED &&
      format.sampleRate.toInt() == TARGET_SAMPLE_RATE &&
      format.sampleSizeInBits == TARGET_SAMPLE_SIZE &&
      format.channels == TARGET_CHANNELS
  }

  private fun streamWavFile(file: File, stream: OfflineStream) {
    AudioSystem.getAudioInputStream(file).use { ais ->
      streamPcmAudio(ais, stream)
    }
  }

  private fun streamPcmAudio(ais: AudioInputStream, stream: OfflineStream) {
    val format = ais.format
    val sampleRate = format.sampleRate.toInt()
    val channels = format.channels
    val sampleBits = format.sampleSizeInBits

    if (sampleRate != TARGET_SAMPLE_RATE) {
      throw UnsupportedOperationException("只支持 16kHz 音频，当前: ${sampleRate}Hz")
    }
    if (sampleBits != TARGET_SAMPLE_SIZE) {
      throw UnsupportedOperationException("只支持 16-bit 音频，当前: ${sampleBits}bit")
    }
    if (channels != TARGET_CHANNELS) {
      throw UnsupportedOperationException("只支持单声道音频，当前: ${channels}声道")
    }

    val isBigEndian = format.isBigEndian
    val buffer = ByteArray(8192)
    var carry: Byte? = null
    var bytesRead = ais.read(buffer)

    while (bytesRead > 0) {
      var available = bytesRead
      val data = if (carry != null) {
        val carryByte = carry ?: 0
        val combined = ByteArray(bytesRead + 1)
        combined[0] = carryByte
        System.arraycopy(buffer, 0, combined, 1, bytesRead)
        carry = null
        available = combined.size
        combined
      } else {
        buffer
      }

      if (available % 2 != 0) {
        carry = data[available - 1]
        available -= 1
      }

      val sampleCount = available / 2
      if (sampleCount > 0) {
        val samples = FloatArray(sampleCount)
        var sampleIndex = 0
        var index = 0
        while (index < available) {
          val b1 = data[index].toInt()
          val b2 = data[index + 1].toInt()
          val sampleShort = if (isBigEndian) {
            (b1 shl 8) or (b2 and 0xFF)
          } else {
            (b2 shl 8) or (b1 and 0xFF)
          }
          samples[sampleIndex++] = sampleShort.toShort() / 32768.0f
          index += 2
        }
        stream.acceptWaveform(samples, sampleRate)
      }

      bytesRead = ais.read(buffer)
    }
  }

  private fun convertAudioToTargetWavWithFfmpeg(inputFile: File): File {
    val outputFile = File.createTempFile("zutil-stt-", ".wav")
    val command = listOf(
      "ffmpeg",
      "-y",
      "-i",
      inputFile.absolutePath,
      "-vn",
      "-ac",
      TARGET_CHANNELS.toString(),
      "-ar",
      TARGET_SAMPLE_RATE.toString(),
      "-acodec",
      "pcm_s16le",
      outputFile.absolutePath
    )

    val process = ProcessBuilder(command)
      .redirectErrorStream(true)
      .start()
    val log = process.inputStream.bufferedReader().use { it.readText() }
    val finished = process.waitFor(FFMPEG_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    if (!finished) {
      process.destroyForcibly()
      runCatching { outputFile.delete() }
      throw IllegalStateException("FFmpeg 转码超时，请重试")
    }

    if (process.exitValue() != 0 || !outputFile.exists() || outputFile.length() <= 0L) {
      runCatching { outputFile.delete() }
      val tail = compactProcessLog(log)
      throw IllegalStateException("FFmpeg 转码失败${if (tail.isBlank()) "" else ": $tail"}")
    }

    return outputFile
  }

  private fun compactProcessLog(log: String): String {
    if (log.isBlank()) return ""
    return log.lineSequence()
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .toList()
      .takeLast(3)
      .joinToString(" | ")
      .take(220)
  }

  private fun isFfmpegAvailable(): Boolean {
    ffmpegAvailableCache?.let { return it }
    val available = runCatching {
      val process = ProcessBuilder("ffmpeg", "-version")
        .redirectErrorStream(true)
        .start()
      val finished = process.waitFor(4, TimeUnit.SECONDS)
      if (!finished) {
        process.destroyForcibly()
        false
      } else {
        process.exitValue() == 0
      }
    }.getOrDefault(false)
    ffmpegAvailableCache = available
    return available
  }

  @Synchronized
  private fun configureSherpaNativePathIfNeeded() {
    if (sherpaNativePreloaded || System.getProperty(SHERPA_NATIVE_PRELOADED_PROP) == "true") {
      sherpaNativePreloaded = true
      return
    }

    val nativeBase = resolveNativeResourceBase() ?: return
    val nativeFileNames = resolveNativeFileNamesForCurrentPlatform() ?: return
    val onnxTarget = extractNativeResource(nativeBase, nativeFileNames.first)
    val jniTarget = extractNativeResource(nativeBase, nativeFileNames.second)
    if (onnxTarget == null || jniTarget == null) {
      logDebug("native-config-skip missing-resource base=$nativeBase")
      return
    }

    val nativeDir = jniTarget.parentFile ?: return
    System.setProperty(SHERPA_NATIVE_PATH_PROP, nativeDir.absolutePath)

    loadNativeLibrarySafely(onnxTarget, "onnxruntime")
    loadNativeLibrarySafely(jniTarget, "sherpa-onnx-jni")

    sherpaNativePreloaded = true
    System.setProperty(SHERPA_NATIVE_PRELOADED_PROP, "true")
    logDebug("native-config-ok base=$nativeBase dir=${nativeDir.absolutePath}")
    logDebug("native-config-files onnx=${onnxTarget.absolutePath}(${onnxTarget.length()}) jni=${jniTarget.absolutePath}(${jniTarget.length()})")
  }

  private fun loadNativeLibrarySafely(file: File, label: String) {
    try {
      System.load(file.absolutePath)
      logDebug("native-load-ok $label path=${file.absolutePath}")
    } catch (e: UnsatisfiedLinkError) {
      val message = e.message.orEmpty()
      if (message.contains("already loaded in another classloader", ignoreCase = true)) {
        logDebug("native-load-skip $label already-loaded-other-classloader")
        return
      }
      throw IllegalStateException("Failed to load native library $label: $message")
    }
  }

  private fun resolveNativeFileNamesForCurrentPlatform(): Pair<String, String>? {
    val os = System.getProperty("os.name", "").lowercase(Locale.getDefault())
    return when {
      os.contains("mac") -> "libonnxruntime.1.17.1.dylib" to "libsherpa-onnx-jni.dylib"
      else -> System.mapLibraryName("onnxruntime") to System.mapLibraryName("sherpa-onnx-jni")
    }
  }

  private fun resolveNativeResourceBase(): String? {
    val os = System.getProperty("os.name", "").lowercase(Locale.getDefault())
    val arch = System.getProperty("os.arch", "").lowercase(Locale.getDefault())
    return when {
      os.contains("win") && (arch.contains("64") || arch.contains("amd64")) -> "sherpa-onnx/native/win-x64"
      os.contains("linux") && (arch.contains("aarch64") || arch.contains("arm64")) -> "sherpa-onnx/native/linux-aarch64"
      os.contains("linux") -> "sherpa-onnx/native/linux-x64"
      os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64")) -> "sherpa-onnx/native/osx-aarch64"
      os.contains("mac") -> "sherpa-onnx/native/osx-x64"
      else -> null
    }
  }

  private fun extractNativeResource(base: String, fileName: String): File? {
    val resourcePath = "$base/$fileName"
    val resource = this::class.java.classLoader.getResourceAsStream(resourcePath) ?: return null

    val targetDir = File(NATIVE_CACHE_DIR, base.replace('/', File.separatorChar))
    if (!targetDir.exists()) {
      targetDir.mkdirs()
    }
    val targetFile = File(targetDir, fileName)

    if (targetFile.exists() && targetFile.length() > 0L) {
      return targetFile
    }

    resource.use { input ->
      targetFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    return targetFile
  }

  private fun normalizePath(path: String): String {
    return path.trim().trim('"')
  }

  private fun isWindows(): Boolean {
    return System.getProperty("os.name", "")
      .lowercase(Locale.getDefault())
      .contains("win")
  }

  private fun stackTraceToSingleLine(t: Throwable): String {
    val writer = StringWriter()
    val printWriter = PrintWriter(writer)
    t.printStackTrace(printWriter)
    printWriter.flush()
    return writer.toString().lineSequence().map { it.trim() }.joinToString(" | ").take(800)
  }

  private fun logDebug(message: String) {
    runCatching {
      DEBUG_LOG_FILE.parentFile?.mkdirs()
      DEBUG_LOG_FILE.appendText("${System.currentTimeMillis()} [stt] $message\n")
    }
  }

  private fun updateState(block: () -> Unit) {
    Snapshot.withMutableSnapshot { block() }
  }
}
