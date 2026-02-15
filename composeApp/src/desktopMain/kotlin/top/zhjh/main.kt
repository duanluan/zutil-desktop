package top.zhjh

import androidx.compose.ui.window.application
import top.zhjh.util.SingleInstanceGuard
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.awt.GraphicsEnvironment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JOptionPane

/**
 * 把异常写入日志文件
 */
private fun logCrash(cause: Throwable) {
  // 获取当前工作目录（通常是安装目录）
  val appDir = System.getProperty("user.dir") ?: "."

  // 安装目录下创建 logs 文件夹
  val logDir = File(appDir, "logs").apply { mkdirs() }
  val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
  val logFile = File(logDir, "crash-$timestamp.log")

  val sw = StringWriter()
  cause.printStackTrace(PrintWriter(sw))
  val text = sw.toString()

  try {
    logFile.writeText(
      buildString {
        appendLine("ZUtil crash log")
        appendLine("Time: $timestamp")
        appendLine("OS: ${System.getProperty("os.name")} ${System.getProperty("os.arch")}")
        appendLine("Java: ${System.getProperty("java.version")} ${System.getProperty("java.vendor")}")
        appendLine("App Dir: $appDir") // 记录一下安装目录位置，方便排查
        appendLine()
        appendLine("Exception stack trace:")
        appendLine(text)
      }
    )
    System.err.println("[ZUtil] Uncaught exception, log written to: $logFile")
  } catch (e: Exception) {
    // 如果连日志都写不进去（比如权限问题），只能尝试吐到标准错误
    System.err.println("[ZUtil] Failed to write crash log to disk!")
    System.err.println("Original exception:")
    System.err.println(text)
    System.err.println("Log writing exception:")
    e.printStackTrace()
  }
}

fun main() {
  val instance = SingleInstanceGuard.tryAcquire("zutil-desktop")
  if (instance == null) {
    notifyAlreadyRunning()
    return
  }

  try {
    application {
      AppRoot(
        onExitRequest = {
          instance.close()
          this@application.exitApplication()
        }
      )
      // TestApp()
    }
  } catch (e: Throwable) {
    logCrash(e)
  } finally {
    instance.close()
  }
}

private fun notifyAlreadyRunning() {
  val message = "ZUtil Desktop is already running."
  System.err.println("[ZUtil] $message")

  runCatching {
    if (!GraphicsEnvironment.isHeadless()) {
      JOptionPane.showMessageDialog(
        null,
        message,
        "ZUtil",
        JOptionPane.WARNING_MESSAGE
      )
    }
  }
}
