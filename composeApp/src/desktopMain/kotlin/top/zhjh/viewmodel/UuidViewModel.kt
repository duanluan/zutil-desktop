package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import top.csaf.awt.ClipboardUtil
import top.csaf.id.UuidUtil
import top.zhjh.common.composable.ToastManager
import java.io.File
import java.nio.ByteBuffer
import java.util.*

private const val DEFAULT_GENERATE_COUNT = 100
private const val MAX_GENERATE_COUNT = 1000
private const val DEFAULT_V35_NAME_PREFIX = "zutil-desktop"

private const val FORMAT_STRING = "String"
private const val FORMAT_BINARY = "Binary"
private const val FORMAT_HEX = "Hex"
private const val FORMAT_BASE64 = "Base64"

private val UUID_VERSION_TYPES = UuidUtil.UuidType.entries

class UuidViewModel : ViewModel() {
  val versionOptions = UUID_VERSION_TYPES.map { it.name }
  val formatOptions = listOf(FORMAT_STRING, FORMAT_BINARY, FORMAT_HEX, FORMAT_BASE64)

  var selectedVersion by mutableStateOf(UuidUtil.UuidType.V4.name)
  var generateCountInput by mutableStateOf(DEFAULT_GENERATE_COUNT.toString())
  var selectedFormat by mutableStateOf(FORMAT_STRING)
  var isUppercase by mutableStateOf(false)
  var keepHyphen by mutableStateOf(true)
  var isListMode by mutableStateOf(false)
  var resultText by mutableStateOf("")
  var resultItems by mutableStateOf<List<String>>(emptyList())
    private set
  var copiedResultItemIndexes by mutableStateOf<Set<Int>>(emptySet())
    private set

  fun generate() {
    val count = resolveGenerateCount()
    val result = buildString {
      repeat(count) { index ->
        append(generateOne(index))
        if (index < count - 1) {
          append('\n')
        }
      }
    }
    resultText = result
    resultItems = result.lines()
    copiedResultItemIndexes = emptySet()
    ToastManager.success("生成完成：$count 条")
  }

  fun copyResult() {
    if (resultText.isBlank()) {
      ToastManager.show("暂无可复制内容")
      return
    }
    if (ClipboardUtil.set(resultText)) {
      ToastManager.success("复制成功")
    } else {
      ToastManager.error("复制失败")
    }
  }

  fun copyResultItem(index: Int) {
    val value = resultItems.getOrNull(index) ?: return
    if (ClipboardUtil.set(value)) {
      copiedResultItemIndexes = copiedResultItemIndexes + index
      ToastManager.success("已复制 UUID")
    } else {
      ToastManager.error("复制失败")
    }
  }

  fun clearResult() {
    resultText = ""
    resultItems = emptyList()
    copiedResultItemIndexes = emptySet()
  }

  fun saveResultTo(path: String): Boolean {
    if (resultText.isBlank()) {
      ToastManager.show("暂无可下载内容")
      return false
    }
    return runCatching {
      File(path).writeText(resultText, Charsets.UTF_8)
      true
    }.onFailure {
      ToastManager.error("下载失败：${it.message ?: "未知错误"}")
    }.getOrDefault(false)
  }

  private fun resolveGenerateCount(): Int {
    val parsed = generateCountInput.toIntOrNull()
    val resolved = (parsed ?: DEFAULT_GENERATE_COUNT).coerceIn(1, MAX_GENERATE_COUNT)
    generateCountInput = resolved.toString()
    return resolved
  }

  private fun generateOne(index: Int): String {
    val uuidType = resolveSelectedUuidType()
    val uuid = when (uuidType) {
      UuidUtil.UuidType.V3 -> UuidUtil.v3(buildNameForNameBasedUuid(index))
      UuidUtil.UuidType.V5 -> UuidUtil.v5(buildNameForNameBasedUuid(index))
      else -> UuidUtil.get(uuidType)
    }
    val bytes = uuidToBytes(uuid)

    return when (selectedFormat) {
      FORMAT_STRING -> {
        var text = uuid.toString()
        if (!keepHyphen) {
          text = text.replace("-", "")
        }
        if (isUppercase) text.uppercase() else text.lowercase()
      }

      FORMAT_HEX -> {
        var text = UuidUtil.toSimple(uuid) ?: ""
        if (keepHyphen && text.length == 32) {
          text =
            "${text.substring(0, 8)}-" +
              "${text.substring(8, 12)}-" +
              "${text.substring(12, 16)}-" +
              "${text.substring(16, 20)}-" +
              text.substring(20, 32)
        }
        if (isUppercase) text.uppercase() else text.lowercase()
      }

      FORMAT_BINARY -> {
        val raw = bytes.joinToString(separator = "") { byte ->
          (byte.toInt() and 0xFF).toString(2).padStart(8, '0')
        }
        if (keepHyphen && raw.length == 128) {
          "${raw.substring(0, 32)}-" +
            "${raw.substring(32, 48)}-" +
            "${raw.substring(48, 64)}-" +
            "${raw.substring(64, 80)}-" +
            raw.substring(80, 128)
        } else {
          raw
        }
      }

      FORMAT_BASE64 -> UuidUtil.toBase64(uuid) ?: Base64.getEncoder().encodeToString(bytes)
      else -> uuid.toString()
    }
  }

  private fun resolveSelectedUuidType(): UuidUtil.UuidType {
    return runCatching { UuidUtil.UuidType.valueOf(selectedVersion) }
      .getOrDefault(UuidUtil.UuidType.V4)
  }

  private fun buildNameForNameBasedUuid(index: Int): String {
    return "$DEFAULT_V35_NAME_PREFIX-$index-${System.nanoTime()}"
  }

  private fun uuidToBytes(uuid: UUID): ByteArray {
    val buffer = ByteBuffer.allocate(16)
    buffer.putLong(uuid.mostSignificantBits)
    buffer.putLong(uuid.leastSignificantBits)
    return buffer.array()
  }
}
