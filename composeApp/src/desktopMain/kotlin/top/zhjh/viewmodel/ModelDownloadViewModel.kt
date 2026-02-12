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
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

private const val HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416

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
  val progress: Float = 0f
)

class ModelDownloadViewModel : ViewModel() {
  // 状态映射：模型ID -> 任务状态
  val taskStatuses = mutableStateMapOf<String, TaskStatus>()

  // 保存下载协程的 Job，用于取消/暂停
  private val downloadJobs = mutableMapOf<String, Job>()

  // 下载保存的根目录
  var downloadDir by mutableStateOf(resolveDownloadDir())

  init {
    ensureDownloadDir(downloadDir)
  }

  // 开始或继续下载
  fun startOrResumeDownload(model: ModelInfo) {
    val currentStatus = taskStatuses[model.id]?.state
    if (currentStatus == DownloadState.DOWNLOADING || currentStatus == DownloadState.EXTRACTING) {
      return
    }

    ensureDownloadDir(downloadDir)

    val job = viewModelScope.launch(Dispatchers.IO) {
      val targetFile = File(downloadDir, model.fileName)
      var connection: HttpURLConnection? = null

      try {
        var downloadedLength = if (targetFile.exists()) targetFile.length() else 0L

        connection = openConnection(model.downloadUrl, downloadedLength)
        var responseCode = connection.responseCode

        if (responseCode == HTTP_REQUESTED_RANGE_NOT_SATISFIABLE) {
          targetFile.delete()
          downloadedLength = 0L
          connection.disconnect()
          connection = openConnection(model.downloadUrl, 0L)
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

        val initialProgress = progressOrNull(downloadedLength, totalLength)
        updateStatusSafely(model.id, DownloadState.DOWNLOADING, initialProgress ?: 0f)

        RandomAccessFile(targetFile, "rw").use { output ->
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
            var lastProgressValue = -1f

            while (isActive && count != -1) {
              output.write(data, 0, count)
              currentTotal += count

              val progress = progressOrNull(currentTotal, totalLength)
              if (progress != null) {
                val now = System.currentTimeMillis()
                if (progress - lastProgressValue >= 0.005f || now - lastProgressUpdate >= 200) {
                  updateStatusSafely(model.id, DownloadState.DOWNLOADING, progress)
                  lastProgressUpdate = now
                  lastProgressValue = progress
                }
              }

              count = input.read(data)
            }
          }
        }

        if (!isActive) return@launch

        updateStatusSafely(model.id, DownloadState.EXTRACTING, 1f)
        unzipTarBz2(targetFile, File(downloadDir))

        targetFile.delete()
        updateStatusSafely(model.id, DownloadState.COMPLETED, 1f)
        notifyToast { ToastManager.success("${model.name} 准备就绪！") }
      } catch (e: Exception) {
        // 如果是手动取消/暂停导致的 CancellationException，不视为错误
        if (e !is kotlinx.coroutines.CancellationException) {
          e.printStackTrace()
          updateStatusSafely(model.id, DownloadState.ERROR, 0f)
          notifyToast { ToastManager.error("下载失败: ${e.message}") }
        }
      } finally {
        connection?.disconnect()
        downloadJobs.remove(model.id)
      }
    }

    downloadJobs[model.id] = job
  }

  // 暂停下载
  fun pauseDownload(modelId: String) {
    downloadJobs[modelId]?.cancel() // 取消协程
    // 保留当前进度状态
    val currentProgress = taskStatuses[modelId]?.progress ?: 0f
    updateStatusSafely(modelId, DownloadState.PAUSED, currentProgress)
  }

  // 取消下载（删除文件）
  fun cancelDownload(model: ModelInfo) {
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

      updateStatusSafely(model.id, DownloadState.IDLE, 0f)
    }
  }

  private fun updateStatusSafely(id: String, state: DownloadState, progress: Float) {
    Snapshot.withMutableSnapshot {
      taskStatuses[id] = TaskStatus(state, progress.coerceIn(0f, 1f))
    }
  }

  private fun notifyToast(block: () -> Unit) {
    Snapshot.withMutableSnapshot {
      block()
    }
  }

  private fun progressOrNull(current: Long, total: Long): Float? {
    if (total <= 0L) return null
    return (current.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
  }

  private fun openConnection(url: String, downloadedLength: Long): HttpURLConnection {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = 15_000
    connection.readTimeout = 30_000
    if (downloadedLength > 0) {
      connection.setRequestProperty("Range", "bytes=$downloadedLength-")
    }
    connection.setRequestProperty("User-Agent", "ZUtil-Desktop")
    connection.connect()
    return connection
  }

  private fun unzipTarBz2(tarBz2File: File, destDir: File) {
    BufferedInputStream(tarBz2File.inputStream()).use { fin ->
      BZip2CompressorInputStream(fin).use { bzIn ->
        TarArchiveInputStream(bzIn).use { tarIn ->
          val baseDir = destDir.canonicalFile
          var entry = tarIn.nextTarEntry
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
            entry = tarIn.nextTarEntry
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

  private fun resolveDownloadDir(): String {
    val userHome = System.getProperty("user.home") ?: "."
    val osName = System.getProperty("os.name")?.lowercase().orEmpty()
    val preferred = when {
      osName.contains("win") -> {
        val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
          ?: File(userHome, "AppData\\Roaming").path
        File(appData, "ZUtil\\models").path
      }
      osName.contains("mac") -> File(userHome, "Library/Application Support/ZUtil/models").path
      else -> {
        val xdg = System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() }
        val base = xdg ?: File(userHome, ".local/share").path
        File(base, "zutil/models").path
      }
    }

    return if (ensureDownloadDir(preferred)) {
      preferred
    } else {
      val fallback = File(userHome, "zutil/models").path
      ensureDownloadDir(fallback)
      fallback
    }
  }

  private fun ensureDownloadDir(path: String): Boolean {
    val dir = File(path)
    return if (dir.exists()) {
      dir.isDirectory
    } else {
      dir.mkdirs()
    }
  }
}
