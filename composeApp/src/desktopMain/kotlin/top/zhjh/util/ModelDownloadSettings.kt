package top.zhjh.util

import java.io.File
import java.util.Locale
import java.util.prefs.Preferences

/**
 * 语音模型下载目录设置。
 *
 * 设计目标：
 * 1. 始终返回“存在且可写”的目录，避免后续下载阶段才失败。
 * 2. 兼容“安装目录不可写”的场景（例如 Windows Program Files）。
 * 3. 统一由此处负责目录校验，业务层只消费最终可用路径。
 */
object ModelDownloadSettings {
  private const val PREF_DOWNLOAD_DIR = "modelDownload.dir"
  private val prefs: Preferences = Preferences.userNodeForPackage(ModelDownloadSettings::class.java)

  fun loadOrDefault(): String {
    val saved = prefs.get(PREF_DOWNLOAD_DIR, "").trim()
    if (saved.isNotEmpty() && ensureDir(saved)) {
      return saved
    }

    val resolved = resolveDefaultDir()
    prefs.put(PREF_DOWNLOAD_DIR, resolved)
    return resolved
  }

  fun save(path: String): Boolean {
    val normalized = path.trim()
    if (normalized.isEmpty()) return false
    if (!ensureDir(normalized)) return false
    prefs.put(PREF_DOWNLOAD_DIR, normalized)
    return true
  }

  fun ensureDir(path: String): Boolean {
    val dir = File(path)
    if (!dir.exists() && !dir.mkdirs()) return false
    if (!dir.isDirectory) return false
    if (!dir.canWrite()) return false
    return runCatching {
      val probe = File.createTempFile(".zutil-write-test-", ".tmp", dir)
      probe.delete()
      true
    }.getOrDefault(false)
  }

  private fun resolveDefaultDir(): String {
    val appDir = System.getProperty("user.dir")?.takeIf { it.isNotBlank() } ?: "."
    val userHome = System.getProperty("user.home")?.takeIf { it.isNotBlank() } ?: appDir
    val osName = System.getProperty("os.name", "").lowercase(Locale.getDefault())

    // 优先延续当前版本行为：安装目录下的 models（便于便携版）
    val candidates = buildList {
      add(File(appDir, "models"))
      // 如果安装目录不可写，回退到用户目录，避免权限问题
      if (osName.contains("win")) {
        add(File(userHome, "AppData/Local/ZUtil/models"))
      }
      add(File(userHome, ".zutil/models"))
      add(File(userHome, "zutil/models"))
    }
    return candidates.firstOrNull { ensureDir(it.path) }?.path ?: File(appDir, "models").path
  }
}
