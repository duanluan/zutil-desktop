package top.zhjh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
  var downloadDir by mutableStateOf("models")

  init {
    File(downloadDir).mkdirs()
  }

  // 开始或继续下载
  fun startOrResumeDownload(model: ModelInfo) {
    val currentStatus = taskStatuses[model.id]?.state
    if (currentStatus == DownloadState.DOWNLOADING || currentStatus == DownloadState.EXTRACTING) {
      return
    }

    val job = viewModelScope.launch(Dispatchers.IO) {
      val targetFile = File(downloadDir, model.fileName)

      try {
        // 1. 准备断点续传
        var downloadedLength = 0L
        if (targetFile.exists()) {
          downloadedLength = targetFile.length()
        }

        // 更新状态为下载中
        updateStatus(model.id, DownloadState.DOWNLOADING, 0f)

        val url = URL(model.downloadUrl)
        val connection = url.openConnection() as HttpURLConnection

        // 设置 Range 头实现断点续传
        if (downloadedLength > 0) {
          connection.setRequestProperty("Range", "bytes=$downloadedLength-")
        }
        connection.connect()

        // 获取总大小 (Content-Length 只是剩余部分的大小，需要加上已下载的)
        val contentLength = connection.contentLengthLong
        val totalLength = if (contentLength == -1L) -1L else contentLength + downloadedLength

        // 检查服务器响应
        val responseCode = connection.responseCode

        // 处理 206 Partial Content (支持断点续传) 和 200 OK (不支持或文件未开始)
        val isResume = responseCode == HttpURLConnection.HTTP_PARTIAL
        if (!isResume && responseCode != HttpURLConnection.HTTP_OK) {
          throw RuntimeException("Server returned code $responseCode")
        }

        val input = BufferedInputStream(connection.inputStream)
        // 使用 RandomAccessFile 支持断点追加写入
        val output = RandomAccessFile(targetFile, "rw")

        if (isResume) {
          output.seek(downloadedLength)
        } else {
          // 如果服务器不支持续传（返回200），则重置文件
          downloadedLength = 0L
          output.setLength(0)
        }

        val data = ByteArray(1024 * 8)
        var count: Int = 0 // 【修复点】这里显式初始化为 0
        var currentTotal = downloadedLength

        // 循环读取
        while (isActive && input.read(data).also { count = it } != -1) {
          output.write(data, 0, count)
          currentTotal += count

          if (totalLength > 0) {
            val progress = currentTotal.toFloat() / totalLength
            updateStatus(model.id, DownloadState.DOWNLOADING, progress)
          }
        }

        output.close()
        input.close()

        // 检查是完成还是被暂停/取消
        if (isActive) {
          // 2. 下载完成，开始解压
          updateStatus(model.id, DownloadState.EXTRACTING, 1f)
          unzipTarBz2(targetFile, File(downloadDir))

          // 3. 清理压缩包
          targetFile.delete()

          updateStatus(model.id, DownloadState.COMPLETED, 1f)
          withContext(Dispatchers.Main) {
            ToastManager.success("${model.name} 准备就绪！")
          }
        }

      } catch (e: Exception) {
        // 如果是手动取消/暂停导致的 CancellationException，不视为错误
        if (e !is kotlinx.coroutines.CancellationException) {
          e.printStackTrace()
          updateStatus(model.id, DownloadState.ERROR, 0f)
          withContext(Dispatchers.Main) {
            ToastManager.error("下载失败: ${e.message}")
          }
        }
      }
    }

    downloadJobs[model.id] = job
  }

  // 暂停下载
  fun pauseDownload(modelId: String) {
    downloadJobs[modelId]?.cancel() // 取消协程
    // 保留当前进度状态
    val currentProgress = taskStatuses[modelId]?.progress ?: 0f
    updateStatus(modelId, DownloadState.PAUSED, currentProgress)
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

      updateStatus(model.id, DownloadState.IDLE, 0f)
    }
  }

  private fun updateStatus(id: String, state: DownloadState, progress: Float) {
    taskStatuses[id] = TaskStatus(state, progress)
  }

  private fun unzipTarBz2(tarBz2File: File, destDir: File) {
    val fin = BufferedInputStream(tarBz2File.inputStream())
    val bzIn = BZip2CompressorInputStream(fin)
    val tarIn = TarArchiveInputStream(bzIn)

    var entry = tarIn.nextTarEntry
    while (entry != null) {
      if (entry.isDirectory) {
        File(destDir, entry.name).mkdirs()
      } else {
        val outputFile = File(destDir, entry.name)
        outputFile.parentFile.mkdirs()
        val output = FileOutputStream(outputFile)
        tarIn.copyTo(output)
        output.close()
      }
      entry = tarIn.nextTarEntry
    }
    tarIn.close()
  }
}
