package top.zhjh.util

import java.awt.FileDialog
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.UIManager

object FilePickerUtil {

  // 初始化时设置 Swing 外观为系统原生风格，否则 Windows 下的 JFileChooser 会很难看
  init {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * 选择文件 (通用)
   */
  fun pickFile(title: String, allowedExtensions: List<String>? = null): String? {
    val dialog = FileDialog(null as JFrame?, title, FileDialog.LOAD)
    if (allowedExtensions != null) {
      // Windows 过滤器格式
      dialog.file = allowedExtensions.joinToString(";") { "*.$it" }
      // macOS 实际上忽略这个 file 属性作为过滤器，但为了兼容性保留
      dialog.setFilenameFilter { _, name ->
        allowedExtensions.any { name.endsWith(".$it", ignoreCase = true) }
      }
    }
    dialog.isVisible = true
    return if (dialog.directory != null && dialog.file != null) {
      File(dialog.directory, dialog.file).absolutePath
    } else {
      null
    }
  }

  /**
   * 选择目录 (分平台处理)
   */
  fun pickDirectory(title: String): String? {
    val osName = System.getProperty("os.name", "").lowercase(Locale.getDefault())

    return if (osName.contains("mac")) {
      // ================= macOS 方案 =================
      // macOS 支持通过设置属性让 FileDialog 变成文件夹选择器
      System.setProperty("apple.awt.fileDialogForDirectories", "true")
      val dialog = FileDialog(null as JFrame?, title, FileDialog.LOAD)
      dialog.isVisible = true
      System.setProperty("apple.awt.fileDialogForDirectories", "false") // 还原设置

      if (dialog.directory != null && dialog.file != null) {
        File(dialog.directory, dialog.file).absolutePath
      } else {
        null
      }
    } else {
      // ================= Windows / Linux 方案 =================
      // Windows 的 FileDialog 不支持只选目录，必须用 JFileChooser
      val chooser = JFileChooser()
      chooser.dialogTitle = title
      chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY // 关键：只允许选目录

      // 尝试打开对话框
      val result = chooser.showOpenDialog(null)

      if (result == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
      } else {
        null
      }
    }
  }
}
