package top.zhjh.util

import java.io.File
import java.util.prefs.Preferences

object ModelDownloadSettings {
  private const val PREF_DOWNLOAD_DIR = "modelDownload.dir"
  private val prefs: Preferences = Preferences.userNodeForPackage(ModelDownloadSettings::class.java)

  fun loadOrDefault(): String {
    val saved = prefs.get(PREF_DOWNLOAD_DIR, "").trim()
    if (saved.isNotEmpty() && ensureDir(saved)) {
      return saved
    }

    val resolved = resolveDefaultDir()
    ensureDir(resolved)
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
    return if (dir.exists()) {
      dir.isDirectory
    } else {
      dir.mkdirs()
    }
  }

  private fun resolveDefaultDir(): String {
    val appDir = System.getProperty("user.dir") ?: "."
    return File(appDir, "models").path
  }
}
