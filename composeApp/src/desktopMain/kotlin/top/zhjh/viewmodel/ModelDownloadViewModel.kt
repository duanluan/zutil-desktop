package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import top.zhjh.common.composable.ToastManager
import top.zhjh.data.ModelInfo
import top.zhjh.util.ModelDownloadSettings
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private const val HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416
private const val UNKNOWN_PROGRESS = -1f
private const val MAX_DOWNLOAD_RETRIES = 2
private const val RETRY_BACKOFF_BASE_MS = 800L

// 下载状态枚举
enum class DownloadState {
  IDLE,       // 未开始
  DOWNLOADING,// 下载中
  PAUSED,     // 已暂停
  EXTRACTING, // 解压中
  COMPLETED,  // 完成
  ERROR       // 错误
}

// 封装状态和进度
data class TaskStatus(
  val state: DownloadState = DownloadState.IDLE,
  val progress: Float = 0f,
  val downloadedBytes: Long = 0L,
  val totalBytes: Long = -1L,
  val retryCount: Int = 0,
  val speedBytesPerSec: Double = 0.0
)

class ModelDownloadViewModel : ViewModel() {
  // 状态映射：模型ID -> 任务状态
  val taskStatuses = mutableStateMapOf<String, TaskStatus>()

  // 保存下载协程的 Job，用于取消/暂停
  private val downloadJobs = mutableMapOf<String, Job>()

  // 保存正在使用的连接，便于快速中断阻塞的读取
  private val activeConnections = ConcurrentHashMap<String, HttpURLConnection>()

  // 暂停标记：避免取消后被下载线程再次写回 DOWNLOADING 状态
  private val pausedIds = ConcurrentHashMap.newKeySet<String>()

  // 下载保存的根目录
  var downloadDir by mutableStateOf(ModelDownloadSettings.loadOrDefault())

  init {
    ModelDownloadSettings.ensureDir(downloadDir)
  }

  // 开始或继续下载
  fun startOrResumeDownload(model: ModelInfo) {
    val currentStatus = taskStatuses[model.id]?.state
    if (currentStatus == DownloadState.DOWNLOADING || currentStatus == DownloadState.EXTRACTING) {
      return
    }

    ModelDownloadSettings.ensureDir(downloadDir)

    // 清除暂停标记，立刻切换到下载中，给用户即时反馈
    pausedIds.remove(model.id)
    val targetFile = File(downloadDir, model.fileName)
    val existingBytes = if (targetFile.exists()) targetFile.length() else 0L
    val knownTotal = taskStatuses[model.id]?.totalBytes ?: -1L
    updateStatusSafely(
      model.id,
      DownloadState.DOWNLOADING,
      progress = progressOrUnknown(existingBytes, knownTotal),
      downloadedBytes = existingBytes,
      totalBytes = knownTotal,
      speedBytesPerSec = 0.0
    )

    val job = viewModelScope.launch(Dispatchers.IO) {
      try {
        downloadWithRetries(model, targetFile)
      } catch (e: Exception) {
        // 如果是手动取消/暂停导致的 CancellationException，不视为错误
        if (e !is CancellationException) {
          e.printStackTrace()
          updateStatusSafely(model.id, DownloadState.ERROR, speedBytesPerSec = 0.0)
          notifyToast { ToastManager.error("下载失败: ${e.message}") }
        }
      } finally {
        downloadJobs.remove(model.id)
      }
    }

    downloadJobs[model.id] = job
  }

  // 暂停下载
  fun pauseDownload(modelId: String) {
    // 先标记暂停并断开连接，确保 UI 立即响应
    pausedIds.add(modelId)
    activeConnections.remove(modelId)?.disconnect()
    downloadJobs[modelId]?.cancel() // 取消协程
    // 保留当前进度状态
    val current = taskStatuses[modelId]
    updateStatusSafely(
      modelId,
      DownloadState.PAUSED,
      progress = current?.progress,
      downloadedBytes = current?.downloadedBytes,
      totalBytes = current?.totalBytes,
      retryCount = current?.retryCount,
      speedBytesPerSec = 0.0
    )
  }

  // 取消下载（删除文件）
  fun cancelDownload(model: ModelInfo) {
    pausedIds.remove(model.id)
    activeConnections.remove(model.id)?.disconnect()
    downloadJobs[model.id]?.cancel() // 取消协程

    viewModelScope.launch(Dispatchers.IO) {
      // 删除未完成的文件
      val targetFile = File(downloadDir, model.fileName)
      if (targetFile.exists()) {
        targetFile.delete()
      }
      // 还需要删除可能解压了一半的文件夹
      val folderName = model.fileName.replace(".tar.bz2", "")
      File(downloadDir, folderName).deleteRecursively()

      updateStatusSafely(
        model.id,
        DownloadState.IDLE,
        progress = 0f,
        downloadedBytes = 0L,
        totalBytes = -1L,
        retryCount = 0,
        speedBytesPerSec = 0.0
      )
    }
  }

  private fun updateStatusSafely(
    id: String,
    state: DownloadState,
    progress: Float? = null,
    downloadedBytes: Long? = null,
    totalBytes: Long? = null,
    retryCount: Int? = null,
    speedBytesPerSec: Double? = null
  ) {
    if (state == DownloadState.DOWNLOADING && pausedIds.contains(id)) {
      return
    }
    Snapshot.withMutableSnapshot {
      val current = taskStatuses[id] ?: TaskStatus()
      val resolvedProgress = progress ?: current.progress
      taskStatuses[id] = current.copy(
        state = state,
        progress = if (resolvedProgress == UNKNOWN_PROGRESS) UNKNOWN_PROGRESS else resolvedProgress.coerceIn(0f, 1f),
        downloadedBytes = downloadedBytes ?: current.downloadedBytes,
        totalBytes = totalBytes ?: current.totalBytes,
        retryCount = retryCount ?: current.retryCount,
        speedBytesPerSec = speedBytesPerSec ?: current.speedBytesPerSec
      )
    }
  }

  private fun notifyToast(block: () -> Unit) {
    Snapshot.withMutableSnapshot {
      block()
    }
  }

  private fun progressOrUnknown(current: Long, total: Long): Float {
    if (total <= 0L) return UNKNOWN_PROGRESS
    return (current.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
  }

  private fun openConnection(modelId: String, url: String, downloadedLength: Long): HttpURLConnection {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = 15_000
    connection.readTimeout = 30_000
    if (downloadedLength > 0) {
      connection.setRequestProperty("Range", "bytes=$downloadedLength-")
    }
    connection.setRequestProperty("User-Agent", "ZUtil-Desktop")
    // 先保存连接，便于暂停时中断连接
    activeConnections[modelId] = connection
    connection.connect()
    return connection
  }

  private suspend fun downloadWithRetries(model: ModelInfo, targetFile: File) {
    var lastError: Exception? = null

    for (attempt in 0..MAX_DOWNLOAD_RETRIES) {
      if (!currentCoroutineContext().isActive) break
      try {
        val metrics = downloadOnce(model, targetFile, attempt)
        if (!currentCoroutineContext().isActive) return

        updateStatusSafely(
          model.id,
          DownloadState.EXTRACTING,
          progress = 1f,
          downloadedBytes = metrics.downloadedBytes,
          totalBytes = metrics.totalBytes,
          retryCount = attempt,
          speedBytesPerSec = 0.0
        )

        val outputDir = File(downloadDir)
        val folderName = model.fileName.replace(".tar.bz2", "")
        val extractedDir = File(outputDir, folderName)
        try {
          unzipTarBz2(targetFile, outputDir)
          if (!extractedDir.exists()) {
            throw IllegalStateException("解压完成但未找到目标目录")
          }
        } catch (e: Exception) {
          extractedDir.deleteRecursively()
          targetFile.delete()
          throw e
        }

        targetFile.delete()
        updateStatusSafely(
          model.id,
          DownloadState.COMPLETED,
          progress = 1f,
          downloadedBytes = if (metrics.totalBytes > 0) metrics.totalBytes else metrics.downloadedBytes,
          totalBytes = metrics.totalBytes,
          retryCount = 0,
          speedBytesPerSec = 0.0
        )
        notifyToast { ToastManager.success("${model.name} 准备就绪！") }
        return
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        lastError = e
        val nextAttempt = attempt + 1
        if (nextAttempt > MAX_DOWNLOAD_RETRIES) break
        updateStatusSafely(model.id, DownloadState.DOWNLOADING, retryCount = nextAttempt, speedBytesPerSec = 0.0)
        delay(RETRY_BACKOFF_BASE_MS * nextAttempt)
      }
    }

    lastError?.let { throw it }
  }

  private data class DownloadMetrics(
    val downloadedBytes: Long,
    val totalBytes: Long
  )

  private suspend fun downloadOnce(model: ModelInfo, targetFile: File, attempt: Int): DownloadMetrics {
    return withContext(Dispatchers.IO) {
      var downloadedLength = if (targetFile.exists()) targetFile.length() else 0L
      var connection: HttpURLConnection? = null

      try {
        connection = openConnection(model.id, model.downloadUrl, downloadedLength)
        var responseCode = connection.responseCode

        if (responseCode == HTTP_REQUESTED_RANGE_NOT_SATISFIABLE) {
          targetFile.delete()
          downloadedLength = 0L
          connection.disconnect()
          connection = openConnection(model.id, model.downloadUrl, 0L)
          responseCode = connection.responseCode
        }

        if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
          throw RuntimeException("Server returned code $responseCode")
        }

        val isResume = responseCode == HttpURLConnection.HTTP_PARTIAL
        val contentLength = connection.contentLengthLong
        val totalLength = when (responseCode) {
          HttpURLConnection.HTTP_PARTIAL -> if (contentLength > 0) contentLength + downloadedLength else -1L
          else -> contentLength
        }
        if (!isResume) {
          downloadedLength = 0L
        }

        updateStatusSafely(
          model.id,
          DownloadState.DOWNLOADING,
          progress = progressOrUnknown(downloadedLength, totalLength),
          downloadedBytes = downloadedLength,
          totalBytes = totalLength,
          retryCount = attempt,
          speedBytesPerSec = 0.0
        )

        val metrics = RandomAccessFile(targetFile, "rw").use { output ->
          if (isResume && downloadedLength > 0) {
            output.seek(downloadedLength)
          } else {
            output.setLength(0)
          }

          BufferedInputStream(connection.inputStream).use { input ->
            val data = ByteArray(1024 * 8)
            var count = input.read(data)
            var currentTotal = downloadedLength
            var lastProgressUpdate = 0L
            var lastProgressValue = UNKNOWN_PROGRESS
            var lastSpeedUpdate = System.currentTimeMillis()
            var lastSpeedBytes = downloadedLength
            var lastSpeed = 0.0

            while (currentCoroutineContext().isActive && count != -1) {
              output.write(data, 0, count)
              currentTotal += count

              val progress = progressOrUnknown(currentTotal, totalLength)
              val now = System.currentTimeMillis()
              val speedDeltaTime = now - lastSpeedUpdate
              if (speedDeltaTime >= 300) {
                val deltaBytes = currentTotal - lastSpeedBytes
                if (deltaBytes >= 0) {
                  val instant = deltaBytes * 1000.0 / speedDeltaTime
                  lastSpeed = if (lastSpeed <= 0.0) instant else (lastSpeed * 0.7 + instant * 0.3)
                  lastSpeedUpdate = now
                  lastSpeedBytes = currentTotal
                }
              }
              val shouldUpdate = if (progress == UNKNOWN_PROGRESS) {
                now - lastProgressUpdate >= 200
              } else {
                progress - lastProgressValue >= 0.005f || now - lastProgressUpdate >= 200
              }

              if (shouldUpdate) {
                updateStatusSafely(
                  model.id,
                  DownloadState.DOWNLOADING,
                  progress = progress,
                  downloadedBytes = currentTotal,
                  totalBytes = totalLength,
                  retryCount = attempt,
                  speedBytesPerSec = lastSpeed
                )
                lastProgressUpdate = now
                lastProgressValue = progress
              }

              count = input.read(data)
            }

            currentCoroutineContext().ensureActive()
            DownloadMetrics(currentTotal, totalLength)
          }
        }

        metrics
      } finally {
        activeConnections.remove(model.id)
        connection?.disconnect()
      }
    }
  }

  private suspend fun unzipTarBz2(tarBz2File: File, destDir: File) = withContext(Dispatchers.IO) {
    BufferedInputStream(tarBz2File.inputStream()).use { fin ->
      BZip2CompressorInputStream(fin).use { bzIn ->
        TarArchiveInputStream(bzIn).use { tarIn ->
          val baseDir = destDir.canonicalFile
          var entry = tarIn.nextEntry
          while (entry != null) {
            val safeFile = resolveTarEntry(baseDir, entry.name)
            if (safeFile != null) {
              if (entry.isDirectory) {
                safeFile.mkdirs()
              } else if (!entry.isSymbolicLink && !entry.isLink) {
                safeFile.parentFile?.mkdirs()
                FileOutputStream(safeFile).use { output ->
                  tarIn.copyTo(output)
                }
              }
            }
            entry = tarIn.nextEntry
          }
        }
      }
    }
  }

  private fun resolveTarEntry(destDir: File, entryName: String): File? {
    val destFile = File(destDir, entryName)
    val destPath = destFile.canonicalPath
    val basePath = destDir.canonicalPath + File.separator
    return if (destPath.startsWith(basePath)) destFile else null
  }
}
