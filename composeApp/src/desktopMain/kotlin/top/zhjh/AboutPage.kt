package top.zhjh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.SimpleIcons
import compose.icons.feathericons.*
import compose.icons.simpleicons.Discord
import compose.icons.simpleicons.Tencentqq
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.compose.resources.painterResource
import top.csaf.json.JsonUtil
import top.zhjh.common.composable.ToastManager
import top.zhjh.enums.ToolCategory
import top.zhjh.enums.ToolItem
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import zutil_desktop.composeapp.generated.resources.Res
import zutil_desktop.composeapp.generated.resources.commonly_used
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.util.prefs.Preferences
import java.util.zip.ZipInputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 关于页：展示版本信息、贡献者与社区链接。
 */
@Composable
internal fun AboutPage(
  onExitRequest: () -> Unit,
  onSchedulePostExitLaunch: (PostExitLaunchPlan?) -> Unit
) {
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()
  var includePrerelease by remember { mutableStateOf(loadIncludePrerelease()) }
  var checkingUpdate by remember { mutableStateOf(false) }
  var updateDialogState by remember { mutableStateOf<UpdateDialogState?>(null) }
  var downloadingInstaller by remember { mutableStateOf(false) }
  var downloadStatusText by remember { mutableStateOf("") }
  var downloadedBytes by remember { mutableStateOf(0L) }
  var downloadTotalBytes by remember { mutableStateOf<Long?>(null) }
  var downloadErrorText by remember { mutableStateOf<String?>(null) }
  var downloadJob by remember { mutableStateOf<Job?>(null) }
  var selectedMirrorLabel by remember { mutableStateOf(UPDATE_MIRROR_OPTIONS.first().label) }
  var exitAfterUpdateDialogState by remember { mutableStateOf<ExitAfterUpdateDialogState?>(null) }
  val showExitAfterUpdateDialog: suspend (ExitAfterUpdateDialogState) -> Unit = { dialog ->
    updateDialogState = null
    // 等待一帧，确保“发现新版本”弹窗先卸载，避免遮罩层叠。
    yield()
    exitAfterUpdateDialogState = dialog
  }
  // 开发者信息卡片
  val developerCards = listOf(
    LinkCardData(
      title = "duanluan",
      lines = listOf("Owner / Full Stack"),
      url = GITHUB_OWNER_URL
    ),
    LinkCardData(
      title = "更多贡献者 >",
      lines = listOf("查看贡献者列表"),
      url = GITHUB_CONTRIBUTORS_URL
    )
  )
  // 特别鸣谢卡片
  val thanksCards = listOf(
    LinkCardData(
      title = "zutil",
      lines = listOf("追求更快更全的 Java 工具类"),
      url = ZUTIL_PROJECT_URL
    )
  )
  // 社区入口卡片
  val communityCards = listOf(
    LinkCardData(
      title = "加入交流群",
      lines = emptyList(),
      url = QQ_GROUP_URL,
      icon = SimpleIcons.Tencentqq
    ),
    LinkCardData(
      title = "Discord",
      lines = emptyList(),
      url = DISCORD_URL,
      icon = SimpleIcons.Discord
    ),
    LinkCardData(
      title = "官方博客",
      lines = emptyList(),
      url = BLOG_URL,
      icon = FeatherIcons.Rss
    )
  )

  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("关于软件", style = MaterialTheme.typography.h5)
    ZCard(
      shadow = ZCardShadow.NEVER,
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "ZUtil 工具箱",
              style = MaterialTheme.typography.subtitle1,
              modifier = Modifier.clickable { openUrl(GITHUB_REPO_URL) }
            )
          }
          ZCheckbox(
            checked = includePrerelease,
            onCheckedChange = { checked ->
              includePrerelease = checked
              saveIncludePrerelease(checked)
            },
            label = "包含测试版本"
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text("版本号：$APP_VERSION")
          Spacer(modifier = Modifier.width(8.dp))
          ZButton(
            type = ZColorType.DEFAULT,
            plain = true,
            enabled = !checkingUpdate,
            onClick = {
              checkingUpdate = true
              scope.launch(Dispatchers.Swing) {
                try {
                  val result = withContext(Dispatchers.IO) {
                    checkForUpdates(
                      currentVersion = APP_VERSION,
                      includePrerelease = includePrerelease
                    )
                  }
                  when (result) {
                    is UpdateCheckResult.HasUpdate -> {
                      val installerAsset = selectInstallerAssetForCurrentPlatform(result.release.assets)
                      downloadingInstaller = false
                      downloadedBytes = 0L
                      downloadTotalBytes = null
                      downloadStatusText = ""
                      downloadErrorText = null
                      updateDialogState = UpdateDialogState(
                        currentVersion = APP_VERSION,
                        release = result.release,
                        installerAsset = installerAsset
                      )
                    }
                    is UpdateCheckResult.UpToDate -> {
                      ToastManager.success("当前已是最新版本：${result.latestTag}", duration = 2500)
                    }
                    is UpdateCheckResult.Failed -> {
                      ToastManager.error("检查更新失败：${result.reason}", duration = 3500)
                    }
                  }
                } catch (cancel: CancellationException) {
                  throw cancel
                } catch (error: Throwable) {
                  val reason = error.message ?: error::class.simpleName ?: "未知错误"
                  ToastManager.error("检查更新失败：$reason", duration = 3500)
                } finally {
                  checkingUpdate = false
                }
              }
            }
          ) {
            Text(if (checkingUpdate) "检查中..." else "检查更新")
          }
        }
      }
    }

    Text("开发人员", style = MaterialTheme.typography.h5)
    LinkCardGrid(items = developerCards, columns = 2)

    Text("特别鸣谢", style = MaterialTheme.typography.h5)
    LinkCardGrid(items = thanksCards, columns = 2)

    Text("社区", style = MaterialTheme.typography.h5)
    LinkCardGrid(items = communityCards, columns = 3)
  }

  val dialogState = updateDialogState
  if (dialogState != null) {
    val installerAsset = dialogState.installerAsset
    val mirrorBaseUrl = resolveMirrorBaseUrlByLabel(selectedMirrorLabel)
    val progressRatio = calculateProgressRatio(downloadedBytes, downloadTotalBytes)
    val cancelDownloadOnly: () -> Unit = {
      downloadJob?.cancel()
      downloadJob = null
      downloadingInstaller = false
      downloadStatusText = "下载已取消"
    }
    AlertDialog(
      onDismissRequest = {
        if (!downloadingInstaller) {
          updateDialogState = null
        }
      },
      title = { Text("发现新版本") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("当前版本：${dialogState.currentVersion}")
          Text("最新版本：${dialogState.release.tagName}")
          if (dialogState.release.name.isNotBlank()) {
            Text("发布名称：${dialogState.release.name}")
          }
          Text(if (dialogState.release.prerelease) "发布渠道：测试版" else "发布渠道：正式版")
          if (installerAsset != null) {
            Text("安装包：${installerAsset.name}", style = MaterialTheme.typography.caption)
          } else {
            Text(
              "未找到适配当前系统的安装包，将打开发布页手动下载",
              style = MaterialTheme.typography.caption,
              color = MaterialTheme.colors.error
            )
          }
          if (downloadingInstaller) {
            Text(
              text = downloadStatusText.ifBlank { "正在下载更新包..." },
              style = MaterialTheme.typography.caption
            )
            if (progressRatio != null) {
              LinearProgressIndicator(
                progress = progressRatio,
                modifier = Modifier.fillMaxWidth()
              )
            } else {
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            val progressText = formatDownloadProgress(downloadedBytes, downloadTotalBytes)
            Text(progressText, style = MaterialTheme.typography.caption)
          }
          val errorText = downloadErrorText
          if (!errorText.isNullOrBlank()) {
            Text(
              text = "错误：$errorText",
              style = MaterialTheme.typography.caption,
              color = MaterialTheme.colors.error
            )
          }
        }
      },
      confirmButton = {
        Box(modifier = Modifier.padding(end = 10.dp, bottom = 10.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ZButton(
              type = ZColorType.PRIMARY,
              enabled = !downloadingInstaller,
              onClick = {
                if (installerAsset == null) {
                  openUrl(applyUpdateMirror(dialogState.release.htmlUrl, mirrorBaseUrl))
                  updateDialogState = null
                  return@ZButton
                }

                downloadingInstaller = true
                downloadedBytes = 0L
                downloadTotalBytes = installerAsset.size?.takeIf { it > 0L }
                downloadStatusText = "准备下载 ${installerAsset.name}..."
                downloadErrorText = null
                downloadJob = scope.launch(Dispatchers.Swing) {
                  try {
                    val downloadedFile = downloadReleaseAsset(
                      asset = installerAsset,
                      releaseTag = dialogState.release.tagName,
                      mirrorBaseUrl = mirrorBaseUrl
                    ) { downloaded, total ->
                      downloadedBytes = downloaded
                      downloadTotalBytes = total
                      downloadStatusText = buildDownloadStatusText()
                    }
                    val postExitLaunchPlan = withContext(Dispatchers.IO) {
                      preparePostExitLaunchPlan(downloadedFile)
                    }
                    if (postExitLaunchPlan != null) {
                      onSchedulePostExitLaunch(postExitLaunchPlan)
                      val readyMessage = if (postExitLaunchPlan.replaceCurrentInstall) {
                        "新版已准备完成，退出应用后会覆盖当前安装目录并自动启动新版。现在退出可立即完成更新。"
                      } else {
                        "新版已准备完成，退出应用后会自动启动新版。现在退出可立即完成更新。"
                      }
                      showExitAfterUpdateDialog(
                        ExitAfterUpdateDialogState(
                          title = "更新已就绪",
                          message = readyMessage,
                          autoStartOnNextExit = true
                        )
                      )
                    } else {
                      downloadStatusText = "下载完成，正在启动安装器..."
                      val launched = withContext(Dispatchers.IO) { launchInstallerPackage(downloadedFile) }
                      if (launched) {
                        onSchedulePostExitLaunch(null)
                        showExitAfterUpdateDialog(
                          ExitAfterUpdateDialogState(
                            title = "安装器已启动",
                            message = "安装程序已启动，建议现在退出应用以完成更新。",
                            autoStartOnNextExit = false
                          )
                        )
                      } else {
                        downloadErrorText = "安装器启动失败，请手动打开：${downloadedFile.absolutePath}"
                      }
                    }
                  } catch (cancel: CancellationException) {
                    downloadStatusText = "下载已取消"
                  } catch (error: Throwable) {
                    downloadStatusText = ""
                    downloadErrorText = formatDownloadErrorMessage(error)
                  } finally {
                    downloadingInstaller = false
                    downloadJob = null
                  }
                }
              }
            ) {
              Text(
                when {
                  downloadingInstaller -> "下载中..."
                  installerAsset == null -> "打开发布页"
                  else -> "下载并安装"
                }
              )
            }
            ZButton(
              type = ZColorType.DEFAULT,
              onClick = {
                if (downloadingInstaller) {
                  cancelDownloadOnly()
                } else {
                  updateDialogState = null
                }
              }
            ) {
              Text(if (downloadingInstaller) "取消下载" else "稍后再说")
            }
          }
        }
      },
      dismissButton = {
        Box(modifier = Modifier.padding(bottom = 10.dp)) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ZDropdownMenu(
              options = UPDATE_MIRROR_OPTIONS.map { it.label },
              value = selectedMirrorLabel,
              onOptionSelected = { selectedMirrorLabel = it ?: UPDATE_MIRROR_OPTIONS.first().label },
              modifier = Modifier.width(190.dp),
              enabled = !downloadingInstaller
            )
            ZButton(
              type = ZColorType.DEFAULT,
              plain = true,
              enabled = !downloadingInstaller,
              onClick = { openUrl(applyUpdateMirror(dialogState.release.htmlUrl, mirrorBaseUrl)) }
            ) {
              Text("手动下载")
            }
          }
        }
      }
    )
  }

  val exitDialogState = exitAfterUpdateDialogState
  if (exitDialogState != null) {
    AlertDialog(
      onDismissRequest = { exitAfterUpdateDialogState = null },
      title = { Text(exitDialogState.title) },
      text = { Text(exitDialogState.message) },
      confirmButton = {
        Box(modifier = Modifier.padding(end = 10.dp, bottom = 10.dp)) {
          ZButton(
            type = ZColorType.PRIMARY,
            onClick = {
              exitAfterUpdateDialogState = null
              onExitRequest()
            }
          ) {
            Text("立即退出")
          }
        }
      },
      dismissButton = {
        Box(modifier = Modifier.padding(bottom = 10.dp)) {
          ZButton(
            type = ZColorType.DEFAULT,
            onClick = { exitAfterUpdateDialogState = null }
          ) {
            Text("稍后退出")
          }
        }
      }
    )
  }

}

/**
 * 工具网格：根据分类与搜索词渲染工具按钮列表。
 */
@Composable
fun ToolListGrid(
  category: ToolCategory,
  filterText: String,
  onToolClick: (ToolItem) -> Unit
) {
  // 使用枚举 name 进行比较，规避 Live Edit/热重载时枚举类加载器不一致导致的过滤失效
  val categoryName = category.name
  val normalizedFilterText = filterText.trim()
  val tools = ToolItem.entries.filter { tool ->
    tool.category.name == categoryName &&
      (normalizedFilterText.isEmpty() || tool.toolName.contains(normalizedFilterText, ignoreCase = true))
  }

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 200.dp),
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(items = tools, key = { it.name }) { tool ->
      ZButton(
        type = ZColorType.PRIMARY,
        contentPadding = PaddingValues(15.dp),
        contentAlignment = Alignment.CenterStart,
        onClick = { onToolClick(tool) }
      ) {
        // 根据工具类型动态显示图标
        val iconModifier = Modifier.size(24.dp).padding(end = 8.dp)
        if (tool.category.name == ToolCategory.AI.name) {
          Icon(FeatherIcons.Cpu, null, modifier = iconModifier)
        } else {
          Icon(painterResource(Res.drawable.commonly_used), null, modifier = iconModifier)
        }

        Text(tool.toolName)
      }
    }
  }
}

/**
 * 链接卡片数据模型。
 */
private data class LinkCardData(
  val title: String,
  val lines: List<String>,
  val url: String,
  val icon: ImageVector? = null
)

/**
 * 更新弹窗展示态。
 */
private data class UpdateDialogState(
  val currentVersion: String,
  val release: GithubRelease,
  val installerAsset: GithubAsset?
)

internal data class PostExitLaunchPlan(
  val command: List<String>,
  val workingDirectory: String?,
  val replaceCurrentInstall: Boolean = false
)

private data class ExitAfterUpdateDialogState(
  val title: String,
  val message: String,
  val autoStartOnNextExit: Boolean
)

private data class LinuxPortableLayout(
  val appRoot: File,
  val executable: File
)

private data class PortableInstallTarget(
  val appRoot: File,
  val launcherName: String
)

private data class UpdateMirrorOption(
  val label: String,
  val baseUrl: String
)

/**
 * 链接卡片网格：按指定列数排布。
 */
@Composable
private fun LinkCardGrid(
  items: List<LinkCardData>,
  columns: Int
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    items.chunked(columns).forEach { rowItems ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        rowItems.forEach { item ->
          LinkCard(item, Modifier.weight(1f))
        }
        // 补齐空列，保证每行等宽
        repeat(columns - rowItems.size) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

/**
 * 单个链接卡片：标题 + 可选图标 + 说明行。
 */
@Composable
private fun LinkCard(
  data: LinkCardData,
  modifier: Modifier = Modifier
) {
  ZCard(
    shadow = ZCardShadow.NEVER,
    modifier = modifier.clickable { openUrl(data.url) },
    contentPadding = PaddingValues(16.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (data.icon != null) {
        Icon(data.icon, contentDescription = data.title, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(data.title, style = MaterialTheme.typography.subtitle1)
        data.lines.forEach { line ->
          Text(line, style = MaterialTheme.typography.caption)
        }
      }
    }
  }
}

/**
 * 在系统浏览器中打开链接。
 * 优先使用 Desktop API，失败时使用命令行兜底。
 */
private fun openUrl(url: String) {
  val uri = runCatching { URI(url) }.getOrNull() ?: return
  val opened = runCatching {
    if (Desktop.isDesktopSupported()) {
      val desktop = Desktop.getDesktop()
      if (desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(uri)
        return@runCatching true
      }
    }
    false
  }.getOrDefault(false)

  if (!opened) {
    openUrlFallback(uri)
  }
}

/**
 * 兜底方案：根据操作系统选择外部命令打开链接。
 */
private fun openUrlFallback(uri: URI) {
  val os = System.getProperty("os.name")?.lowercase().orEmpty()
  val command = when {
    os.contains("mac") -> listOf("open", uri.toString())
    os.contains("win") -> listOf("rundll32", "url.dll,FileProtocolHandler", uri.toString())
    else -> listOf("xdg-open", uri.toString())
  }
  runCatching {
    ProcessBuilder(command)
      .redirectErrorStream(true)
      .start()
  }
}

/**
 * 在系统文件管理器中打开目录。
 */
internal fun openDirectory(path: String) {
  val target = File(path)
  if (!target.exists()) return
  val opened = runCatching {
    if (Desktop.isDesktopSupported()) {
      val desktop = Desktop.getDesktop()
      if (desktop.isSupported(Desktop.Action.OPEN)) {
        desktop.open(target)
        return@runCatching true
      }
    }
    false
  }.getOrDefault(false)

  if (!opened) {
    openDirectoryFallback(target)
  }
}

/**
 * 兜底方案：根据操作系统选择外部命令打开目录。
 */
private fun openDirectoryFallback(target: File) {
  val os = System.getProperty("os.name")?.lowercase().orEmpty()
  val command = when {
    os.contains("mac") -> listOf("open", target.absolutePath)
    os.contains("win") -> listOf("explorer", target.absolutePath)
    else -> listOf("xdg-open", target.absolutePath)
  }
  runCatching {
    ProcessBuilder(command)
      .redirectErrorStream(true)
      .start()
  }
}

/**
 * 更新检查结果。
 */
private sealed interface UpdateCheckResult {
  data class HasUpdate(val release: GithubRelease) : UpdateCheckResult
  data class UpToDate(val latestTag: String) : UpdateCheckResult
  data class Failed(val reason: String) : UpdateCheckResult
}

/**
 * GitHub Release 必要字段。
 */
private data class GithubRelease(
  val tagName: String,
  val name: String,
  val htmlUrl: String,
  val prerelease: Boolean,
  val draft: Boolean,
  val assets: List<GithubAsset>
)

/**
 * GitHub Release 资产信息。
 */
private data class GithubAsset(
  val name: String,
  val downloadUrl: String,
  val size: Long?,
  val sha256: String?
)

private enum class OsFamily {
  WINDOWS, MAC, LINUX, OTHER
}

private enum class CpuFamily {
  X64, ARM64, X86, ARM, OTHER
}

/**
 * 语义化版本，用于版本比较。
 */
private data class ParsedVersion(
  val major: Int,
  val minor: Int,
  val patch: Int,
  val preRelease: List<String>
) : Comparable<ParsedVersion> {
  override fun compareTo(other: ParsedVersion): Int {
    val majorCmp = major.compareTo(other.major)
    if (majorCmp != 0) return majorCmp
    val minorCmp = minor.compareTo(other.minor)
    if (minorCmp != 0) return minorCmp
    val patchCmp = patch.compareTo(other.patch)
    if (patchCmp != 0) return patchCmp

    if (preRelease.isEmpty() && other.preRelease.isNotEmpty()) return 1
    if (preRelease.isNotEmpty() && other.preRelease.isEmpty()) return -1
    if (preRelease.isEmpty() && other.preRelease.isEmpty()) return 0

    val size = max(preRelease.size, other.preRelease.size)
    for (index in 0 until size) {
      val left = preRelease.getOrNull(index) ?: return -1
      val right = other.preRelease.getOrNull(index) ?: return 1
      val identifierCmp = comparePreReleaseIdentifier(left, right)
      if (identifierCmp != 0) return identifierCmp
    }
    return 0
  }
}

/**
 * 检查更新：从 GitHub Releases 获取最新版本并做比较。
 */
private fun checkForUpdates(
  currentVersion: String,
  includePrerelease: Boolean
): UpdateCheckResult {
  val releases = fetchGithubReleases()
  if (releases.isEmpty()) {
    return UpdateCheckResult.Failed("未获取到发布信息")
  }

  val latestRelease = releases.firstOrNull { release ->
    !release.draft && (includePrerelease || !release.prerelease)
  } ?: run {
    if (!includePrerelease && releases.any { !it.draft && it.prerelease }) {
      return UpdateCheckResult.Failed("当前暂无正式版，请勾选“包含测试版本”后重试")
    }
    return UpdateCheckResult.Failed("没有找到符合条件的版本")
  }

  if (isForceUpdateEnabledForTest()) {
    return UpdateCheckResult.HasUpdate(latestRelease)
  }

  val versionCmp = compareReleaseVersion(latestRelease.tagName, currentVersion)
  return if (versionCmp > 0) {
    UpdateCheckResult.HasUpdate(latestRelease)
  } else {
    UpdateCheckResult.UpToDate(latestRelease.tagName)
  }
}

/**
 * 从 GitHub API 拉取 Release 列表。
 */
private fun fetchGithubReleases(): List<GithubRelease> {
  val request = HttpRequest.newBuilder(URI(GITHUB_RELEASES_API_URL))
    .timeout(Duration.ofSeconds(10))
    .header("Accept", "application/vnd.github+json")
    .header("X-GitHub-Api-Version", "2022-11-28")
    .header("User-Agent", UPDATE_CHECK_USER_AGENT)
    .GET()
    .build()

  val response = githubHttpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
  val code = response.statusCode()
  if (code !in 200..299) {
    val apiMessage = parseGithubErrorMessage(response.body())
    val reason = if (apiMessage.isNullOrBlank()) "HTTP $code" else "HTTP $code: $apiMessage"
    throw IllegalStateException(reason)
  }

  return parseGithubReleaseList(response.body())
}

/**
 * 解析 GitHub releases 数组响应，仅提取本功能所需字段。
 */
private fun parseGithubReleaseList(json: String): List<GithubRelease> {
  val parsed = runCatching { JsonUtil.parse(json) }
    .getOrElse { error ->
      throw IllegalStateException("解析发布数据失败：${error.message ?: "未知错误"}", error)
    }
  val items = parsed as? List<*> ?: throw IllegalStateException("发布数据格式异常")
  return items.mapNotNull { item ->
    val obj = item as? Map<*, *> ?: return@mapNotNull null
    val tagName = obj["tag_name"]?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
    val htmlUrl = obj["html_url"]?.toString()?.takeIf { it.isNotBlank() } ?: GITHUB_RELEASES_URL
    val name = obj["name"]?.toString().orEmpty()
    val prerelease = parseAnyToBoolean(obj["prerelease"])
    val draft = parseAnyToBoolean(obj["draft"])
    val assets = parseGithubAssets(obj["assets"])
    GithubRelease(
      tagName = tagName,
      name = name,
      htmlUrl = htmlUrl,
      prerelease = prerelease,
      draft = draft,
      assets = assets
    )
  }
}

/**
 * 解析 GitHub 错误响应中的 message。
 */
private fun parseGithubErrorMessage(json: String): String? {
  val parsed = runCatching { JsonUtil.parse(json) }.getOrNull() as? Map<*, *> ?: return null
  return parsed["message"]?.toString()?.takeIf { it.isNotBlank() }
}

/**
 * 兼容布尔字段既可能是 Boolean，也可能是字符串。
 */
private fun parseAnyToBoolean(value: Any?): Boolean {
  return when (value) {
    is Boolean -> value
    is String -> value.toBooleanStrictOrNull() ?: false
    else -> false
  }
}

/**
 * 解析 assets 字段。
 */
private fun parseGithubAssets(value: Any?): List<GithubAsset> {
  val assets = value as? List<*> ?: return emptyList()
  return assets.mapNotNull { item ->
    val obj = item as? Map<*, *> ?: return@mapNotNull null
    val name = obj["name"]?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
    val downloadUrl = obj["browser_download_url"]?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
    val size = parseAnyToLong(obj["size"])
    val sha256 = normalizeSha256(obj["digest"]?.toString())
    GithubAsset(
      name = name,
      downloadUrl = downloadUrl,
      size = size,
      sha256 = sha256
    )
  }
}

/**
 * 解析数值字段到 Long。
 */
private fun parseAnyToLong(value: Any?): Long? {
  return when (value) {
    is Long -> value
    is Int -> value.toLong()
    is Number -> value.toLong()
    is String -> value.toLongOrNull()
    else -> null
  }
}

/**
 * 规范化 SHA-256 文本（支持 "sha256:xxx"）。
 */
private fun normalizeSha256(raw: String?): String? {
  val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
  val withoutPrefix = value.removePrefix("sha256:").removePrefix("SHA256:")
  if (!SHA256_REGEX.matches(withoutPrefix)) return null
  return withoutPrefix.lowercase()
}

/**
 * 按当前平台选择可安装资产。
 */
private fun selectInstallerAssetForCurrentPlatform(assets: List<GithubAsset>): GithubAsset? {
  if (assets.isEmpty()) return null

  val os = detectOsFamily()
  val cpu = detectCpuFamily()
  val archLikeLinux = os == OsFamily.LINUX && isArchLikeLinuxDistro()

  val preferredCandidates: List<GithubAsset> = when (os) {
    OsFamily.WINDOWS -> filterAssetsByExtensions(assets, listOf(".exe", ".msi"))
    OsFamily.MAC -> filterAssetsByExtensions(assets, listOf(".dmg", ".pkg"))
    OsFamily.LINUX -> {
      if (archLikeLinux) {
        val appImage = filterAssetsByExtensions(assets, listOf(".appimage"))
        if (appImage.isNotEmpty()) {
          appImage
        } else {
          // Arch 系优先 portable zip，避免自动拉 deb/rpm 导致不可安装。
          assets.filter { asset ->
            val lower = asset.name.lowercase()
            lower.endsWith(".zip") && lower.contains("linux-portable")
          }
        }
      } else {
        filterAssetsByExtensions(assets, listOf(".appimage", ".deb", ".rpm"))
      }
    }
    OsFamily.OTHER -> emptyList()
  }
  if (preferredCandidates.isEmpty()) return null

  val byArch = preferredCandidates.filter { asset ->
    val lower = asset.name.lowercase()
    !containsArchHint(lower) || matchesCurrentArch(lower, cpu)
  }
  return byArch.firstOrNull() ?: preferredCandidates.firstOrNull()
}

/**
 * 下载更新安装包并做可选校验。
 */
private suspend fun downloadReleaseAsset(
  asset: GithubAsset,
  releaseTag: String,
  mirrorBaseUrl: String,
  onProgress: (downloaded: Long, total: Long?) -> Unit
): File = withContext(Dispatchers.IO) {
  var lastError: Throwable? = null
  repeat(UPDATE_DOWNLOAD_MAX_RETRIES) { attempt ->
    try {
      return@withContext downloadReleaseAssetOnce(
        asset = asset,
        releaseTag = releaseTag,
        mirrorBaseUrl = mirrorBaseUrl,
        onProgress = onProgress
      )
    } catch (cancel: CancellationException) {
      throw cancel
    } catch (error: Throwable) {
      lastError = error
      val hasNextAttempt = attempt < UPDATE_DOWNLOAD_MAX_RETRIES - 1
      if (!hasNextAttempt || !shouldRetryDownloadError(error)) {
        throw error
      }
      withContext(Dispatchers.Swing) {
        onProgress(0L, asset.size?.takeIf { it > 0L })
      }
      delay(UPDATE_DOWNLOAD_RETRY_DELAY_MS)
    }
  }
  throw lastError ?: IllegalStateException("下载失败")
}

/**
 * 单次下载流程。
 */
private suspend fun downloadReleaseAssetOnce(
  asset: GithubAsset,
  releaseTag: String,
  mirrorBaseUrl: String,
  onProgress: (downloaded: Long, total: Long?) -> Unit
): File = withContext(Dispatchers.IO) {
  val resolvedDownloadUrl = applyUpdateMirror(asset.downloadUrl, mirrorBaseUrl)
  val request = HttpRequest.newBuilder(URI(resolvedDownloadUrl))
    .timeout(Duration.ofMinutes(UPDATE_DOWNLOAD_TIMEOUT_MINUTES))
    .header("Accept", "application/octet-stream")
    .header("User-Agent", UPDATE_CHECK_USER_AGENT)
    .GET()
    .build()

  val response = githubHttpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
  val code = response.statusCode()
  if (code !in 200..299) {
    runCatching { response.body().close() }
    throw IllegalStateException("下载失败，HTTP $code")
  }

  val totalFromHeader = response.headers().firstValueAsLong("Content-Length").orElse(-1L).takeIf { it > 0L }
  val total = totalFromHeader ?: asset.size?.takeIf { it > 0L }
  val targetDir = buildUpdateDownloadDir(releaseTag).also { dir ->
    if (!dir.exists()) {
      dir.mkdirs()
    }
  }
  val targetFile = File(targetDir, asset.name)
  if (targetFile.exists()) {
    targetFile.delete()
  }

  try {
    response.body().use { input ->
      targetFile.outputStream().use { output ->
        val buffer = ByteArray(64 * 1024)
        var downloaded = 0L
        var lastReportAt = 0L
        withContext(Dispatchers.Swing) { onProgress(0L, total) }
        while (true) {
          val read = input.read(buffer)
          if (read <= 0) break
          output.write(buffer, 0, read)
          downloaded += read
          val now = System.currentTimeMillis()
          if (now - lastReportAt >= UPDATE_DOWNLOAD_PROGRESS_INTERVAL_MS) {
            lastReportAt = now
            withContext(Dispatchers.Swing) { onProgress(downloaded, total) }
          }
        }
        output.flush()
        withContext(Dispatchers.Swing) { onProgress(downloaded, total) }
      }
    }
    verifyAssetSha256(targetFile, asset.sha256)
    targetFile
  } catch (e: Throwable) {
    runCatching { targetFile.delete() }
    throw e
  }
}

/**
 * 下载错误是否可自动重试。
 */
private fun shouldRetryDownloadError(error: Throwable): Boolean {
  if (error is IOException) return true
  val message = error.message?.lowercase().orEmpty()
  if (message.contains("closed")) return true
  if (message.contains("connection reset")) return true
  if (message.contains("broken pipe")) return true
  if (message.contains("timed out")) return true
  if (message.contains("eof")) return true

  if (message.startsWith("下载失败，http")) {
    val statusCode = message.substringAfter("http").trim().toIntOrNull()
    if (statusCode != null && statusCode in 500..599) {
      return true
    }
  }

  return false
}

/**
 * 将技术异常转换成用户可理解提示。
 */
private fun formatDownloadErrorMessage(error: Throwable): String {
  val message = error.message?.trim().orEmpty()
  val lowered = message.lowercase()

  if (lowered.contains("closed") ||
    lowered.contains("connection reset") ||
    lowered.contains("broken pipe") ||
    lowered.contains("eof")
  ) {
    return "下载连接中断，请重试或使用“手动下载”"
  }
  if (lowered.contains("timed out")) {
    return "下载超时，请重试或使用“手动下载”"
  }

  return message.ifBlank { "下载失败，请重试或使用“手动下载”" }
}

/**
 * 判断是否应采用“退出后启动新版”模式，并返回待执行命令。
 */
private fun preparePostExitLaunchPlan(file: File): PostExitLaunchPlan? {
  if (!file.exists()) return null

  val os = detectOsFamily()
  if (os != OsFamily.LINUX) return null

  val lowerName = file.name.lowercase()
  if (lowerName.endsWith(".appimage")) {
    runCatching { file.setExecutable(true) }
    return PostExitLaunchPlan(
      command = listOf(file.absolutePath),
      workingDirectory = file.parentFile?.absolutePath
    )
  }

  if (lowerName.endsWith(".zip") && lowerName.contains("linux-portable")) {
    val portableLayout = prepareLinuxPortableLayout(file) ?: return null
    val currentInstall = detectCurrentPortableInstallTarget()
    if (currentInstall != null) {
      val replaceScript = buildPortableReplaceAndLaunchScript(
        stagedAppRoot = portableLayout.appRoot,
        targetInstall = currentInstall
      )
      if (replaceScript != null) {
        return PostExitLaunchPlan(
          command = listOf("sh", replaceScript.absolutePath),
          workingDirectory = currentInstall.appRoot.parentFile?.absolutePath,
          replaceCurrentInstall = true
        )
      }
    }

    return PostExitLaunchPlan(
      command = listOf(portableLayout.executable.absolutePath),
      workingDirectory = portableLayout.executable.parentFile?.absolutePath
    )
  }

  return null
}

/**
 * 检测当前运行的 portable 安装目录。
 * 仅识别 Compose 打包目录结构：<appRoot>/lib/app 目录下的 jar。
 */
private fun detectCurrentPortableInstallTarget(): PortableInstallTarget? {
  val codeSourceFile = runCatching {
    val location = UpdateCodeSourceMarker::class.java.protectionDomain?.codeSource?.location ?: return@runCatching null
    File(location.toURI())
  }.getOrNull() ?: return null

  val codeSourceDir = if (codeSourceFile.isFile) codeSourceFile.parentFile else codeSourceFile
  val appDir = codeSourceDir?.takeIf { it.name == "app" } ?: return null
  val libDir = appDir.parentFile?.takeIf { it.name == "lib" } ?: return null
  val appRoot = libDir.parentFile ?: return null

  val launcher = File(appRoot, "bin/ZUtil")
  if (!launcher.exists()) return null
  val parent = appRoot.parentFile ?: return null
  if (!parent.exists() || !parent.canWrite()) return null

  return PortableInstallTarget(
    appRoot = appRoot,
    launcherName = launcher.name
  )
}

/**
 * 生成“退出后覆盖安装目录并重启”脚本。
 */
private fun buildPortableReplaceAndLaunchScript(
  stagedAppRoot: File,
  targetInstall: PortableInstallTarget
): File? {
  val stagedCanonical = runCatching { stagedAppRoot.canonicalFile }.getOrElse { stagedAppRoot.absoluteFile }
  val targetCanonical = runCatching { targetInstall.appRoot.canonicalFile }.getOrElse { targetInstall.appRoot.absoluteFile }
  if (!stagedCanonical.exists() || !stagedCanonical.isDirectory) return null
  if (stagedCanonical == targetCanonical) return null

  val targetParent = targetCanonical.parentFile ?: return null
  if (!targetParent.exists() || !targetParent.canWrite()) return null

  val scriptDir = File(System.getProperty("java.io.tmpdir"), "$UPDATE_DOWNLOAD_DIR_NAME/scripts")
  if (!scriptDir.exists()) {
    scriptDir.mkdirs()
  }
  val scriptFile = File(scriptDir, "apply-portable-update-${System.currentTimeMillis()}.sh")
  val script = """
    |#!/bin/sh
    |set -eu
    |STAGED=${quoteForPosixShell(stagedCanonical.absolutePath)}
    |TARGET=${quoteForPosixShell(targetCanonical.absolutePath)}
    |TARGET_PARENT=${quoteForPosixShell(targetParent.absolutePath)}
    |LAUNCHER_NAME=${quoteForPosixShell(targetInstall.launcherName)}
    |BACKUP="${'$'}TARGET.update-backup"
    |
    |if [ ! -d "${'$'}STAGED" ]; then
    |  exit 1
    |fi
    |if [ ! -d "${'$'}TARGET_PARENT" ] || [ ! -w "${'$'}TARGET_PARENT" ]; then
    |  exit 1
    |fi
    |
    |rm -rf "${'$'}BACKUP"
    |if [ -e "${'$'}TARGET" ]; then
    |  mv "${'$'}TARGET" "${'$'}BACKUP"
    |fi
    |
    |if ! mv "${'$'}STAGED" "${'$'}TARGET"; then
    |  if [ -e "${'$'}BACKUP" ] && [ ! -e "${'$'}TARGET" ]; then
    |    mv "${'$'}BACKUP" "${'$'}TARGET"
    |  fi
    |  exit 1
    |fi
    |
    |rm -rf "${'$'}BACKUP"
    |chmod +x "${'$'}TARGET/bin/${'$'}LAUNCHER_NAME" >/dev/null 2>&1 || true
    |exec "${'$'}TARGET/bin/${'$'}LAUNCHER_NAME"
  """.trimMargin()

  return runCatching {
    scriptFile.writeText("$script\n", StandardCharsets.UTF_8)
    scriptFile.setExecutable(true)
    scriptFile
  }.getOrNull()
}

/**
 * 退出后延迟启动外部命令，避免新进程在当前进程退出前抢占单实例锁。
 */
internal fun launchDetachedCommandAfterDelay(
  command: List<String>,
  delaySeconds: Int,
  workingDirectory: String?
): Boolean {
  if (command.isEmpty()) return false
  val safeDelay = delaySeconds.coerceAtLeast(0)
  return when (detectOsFamily()) {
    OsFamily.WINDOWS -> launchDetachedCommandAfterDelayOnWindows(command, safeDelay, workingDirectory)
    else -> launchDetachedCommandAfterDelayOnUnix(command, safeDelay, workingDirectory)
  }
}

private fun launchDetachedCommandAfterDelayOnUnix(
  command: List<String>,
  delaySeconds: Int,
  workingDirectory: String?
): Boolean {
  val commandScript = command.joinToString(" ") { quoteForPosixShell(it) }
  val cdScript = workingDirectory
    ?.takeIf { it.isNotBlank() }
    ?.let { "cd ${quoteForPosixShell(it)} || exit 1; " }
    .orEmpty()
  val delayedScript = "sleep $delaySeconds; ${cdScript}exec $commandScript"
  val launchScript = "nohup sh -c ${quoteForPosixShell(delayedScript)} >/dev/null 2>&1 &"
  return runCatching {
    ProcessBuilder("sh", "-c", launchScript)
      .redirectErrorStream(true)
      .start()
    true
  }.getOrDefault(false)
}

private fun launchDetachedCommandAfterDelayOnWindows(
  command: List<String>,
  delaySeconds: Int,
  workingDirectory: String?
): Boolean {
  val executable = quoteForPowerShell(command.first())
  val args = command.drop(1).joinToString(", ") { "'${quoteForPowerShell(it)}'" }
  val argsScript = if (args.isBlank()) "" else "-ArgumentList @($args) "
  val workDirScript = workingDirectory
    ?.takeIf { it.isNotBlank() }
    ?.let { "-WorkingDirectory '${quoteForPowerShell(it)}' " }
    .orEmpty()
  val script = "Start-Sleep -Seconds $delaySeconds; Start-Process -FilePath '$executable' $argsScript$workDirScript"
  val launchedByPowerShell = runCatching {
    ProcessBuilder("powershell", "-NoProfile", "-WindowStyle", "Hidden", "-Command", script)
      .redirectErrorStream(true)
      .start()
    true
  }.getOrDefault(false)
  if (launchedByPowerShell) return true

  return runCatching {
    val processBuilder = ProcessBuilder(command).redirectErrorStream(true)
    if (!workingDirectory.isNullOrBlank()) {
      processBuilder.directory(File(workingDirectory))
    }
    processBuilder.start()
    true
  }.getOrDefault(false)
}

private fun quoteForPosixShell(value: String): String {
  return "'${value.replace("'", "'\"'\"'")}'"
}

private fun quoteForPowerShell(value: String): String {
  return value.replace("'", "''")
}

/**
 * 启动安装包。
 */
private fun launchInstallerPackage(file: File): Boolean {
  if (!file.exists()) return false

  val os = detectOsFamily()
  val lowerName = file.name.lowercase()

  if (os == OsFamily.LINUX && lowerName.endsWith(".zip") && lowerName.contains("linux-portable")) {
    return runCatching {
      launchLinuxPortableZip(file)
    }.getOrDefault(false)
  }

  if (os == OsFamily.LINUX && lowerName.endsWith(".appimage")) {
    runCatching { file.setExecutable(true) }
    val started = runCatching {
      ProcessBuilder(listOf(file.absolutePath))
        .redirectErrorStream(true)
        .start()
    }.isSuccess
    if (started) return true
  }

  val command = when {
    os == OsFamily.WINDOWS && lowerName.endsWith(".msi") -> listOf("msiexec", "/i", file.absolutePath)
    os == OsFamily.MAC -> listOf("open", file.absolutePath)
    os == OsFamily.LINUX -> listOf("xdg-open", file.absolutePath)
    else -> emptyList()
  }
  if (command.isNotEmpty()) {
    val started = runCatching {
      ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()
    }.isSuccess
    if (started) return true
  }

  return runCatching {
    if (Desktop.isDesktopSupported()) {
      val desktop = Desktop.getDesktop()
      if (desktop.isSupported(Desktop.Action.OPEN)) {
        desktop.open(file)
        return@runCatching true
      }
    }
    false
  }.getOrDefault(false)
}

/**
 * 过滤资产扩展名，并按扩展优先级返回。
 */
private fun filterAssetsByExtensions(assets: List<GithubAsset>, extensionOrder: List<String>): List<GithubAsset> {
  if (extensionOrder.isEmpty()) return emptyList()
  val result = mutableListOf<GithubAsset>()
  for (ext in extensionOrder) {
    for (asset in assets) {
      val lower = asset.name.lowercase()
      if (lower.endsWith(ext)) {
        result.add(asset)
      }
    }
  }
  return result
}

/**
 * 启动 Linux portable zip：解压后直接拉起可执行文件。
 */
private fun launchLinuxPortableZip(zipFile: File): Boolean {
  val layout = prepareLinuxPortableLayout(zipFile) ?: return false
  return runCatching {
    ProcessBuilder(listOf(layout.executable.absolutePath))
      .directory(layout.executable.parentFile)
      .redirectErrorStream(true)
      .start()
    true
  }.getOrDefault(false)
}

/**
 * 解压 portable zip 并解析布局信息。
 */
private fun prepareLinuxPortableLayout(zipFile: File): LinuxPortableLayout? {
  val extractRoot = File(zipFile.parentFile, "${zipFile.nameWithoutExtension}-unpacked")
  if (extractRoot.exists()) {
    extractRoot.deleteRecursively()
  }
  extractRoot.mkdirs()
  extractZipSafely(zipFile, extractRoot)

  val appRoot = findLinuxPortableAppRoot(extractRoot) ?: return null
  val executable = findLinuxPortableExecutable(appRoot) ?: return null
  runCatching { executable.setExecutable(true) }
  return LinuxPortableLayout(
    appRoot = appRoot,
    executable = executable
  )
}

/**
 * 在解压目录中定位 portable 应用根目录。
 * 约定：根目录包含 bin/ZUtil。
 */
private fun findLinuxPortableAppRoot(root: File): File? {
  if (!root.exists() || !root.isDirectory) return null
  val rootLauncher = File(root, "bin/ZUtil")
  if (rootLauncher.exists()) return root

  return root.walkTopDown()
    .maxDepth(4)
    .firstOrNull { candidate ->
      candidate.isDirectory && File(candidate, "bin/ZUtil").exists()
    }
}

/**
 * 安全解压 zip，防止 Zip Slip。
 */
private fun extractZipSafely(zipFile: File, destDir: File) {
  val destCanonical = destDir.canonicalFile
  ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
    while (true) {
      val entry = zis.nextEntry ?: break
      val outFile = File(destDir, entry.name)
      val outCanonical = outFile.canonicalFile
      if (!outCanonical.path.startsWith(destCanonical.path + File.separator) && outCanonical != destCanonical) {
        throw IllegalStateException("更新包解压失败：非法路径")
      }
      if (entry.isDirectory) {
        outCanonical.mkdirs()
      } else {
        outCanonical.parentFile?.mkdirs()
        outCanonical.outputStream().use { output ->
          val buffer = ByteArray(64 * 1024)
          while (true) {
            val read = zis.read(buffer)
            if (read <= 0) break
            output.write(buffer, 0, read)
          }
        }
      }
      zis.closeEntry()
    }
  }
}

/**
 * 在 portable 包中定位可执行入口。
 */
private fun findLinuxPortableExecutable(root: File): File? {
  val allFiles = root.walkTopDown().filter { it.isFile }.toList()
  if (allFiles.isEmpty()) return null

  val preferredByName = allFiles.firstOrNull { file ->
    val lower = file.name.lowercase()
    lower == "zutil" || lower == "zutil.sh" || lower == "zutil.desktop"
  }?.takeIf { candidate ->
    candidate.name.lowercase() != "zutil.desktop"
  }
  if (preferredByName != null) return preferredByName

  val inBin = allFiles.firstOrNull { file ->
    file.parentFile?.name?.equals("bin", ignoreCase = true) == true &&
      !file.name.endsWith(".desktop", ignoreCase = true)
  }
  if (inBin != null) return inBin

  return allFiles.firstOrNull { file ->
    !file.name.endsWith(".desktop", ignoreCase = true) &&
      !file.name.endsWith(".dll", ignoreCase = true) &&
      !file.name.endsWith(".so", ignoreCase = true) &&
      !file.name.endsWith(".jar", ignoreCase = true)
  }
}

/**
 * 计算下载进度（0~1），未知总大小时返回 null。
 */
private fun calculateProgressRatio(downloaded: Long, total: Long?): Float? {
  val safeTotal = total?.takeIf { it > 0L } ?: return null
  return (downloaded.toDouble() / safeTotal.toDouble()).toFloat().coerceIn(0f, 1f)
}

/**
 * 下载状态文本。
 */
private fun buildDownloadStatusText(): String {
  return "正在下载更新包..."
}

/**
 * 下载进度文本。
 */
private fun formatDownloadProgress(downloaded: Long, total: Long?): String {
  val downloadedText = formatBytes(downloaded)
  val totalText = total?.takeIf { it > 0L }?.let { formatBytes(it) } ?: "未知大小"
  return "$downloadedText / $totalText"
}

/**
 * 友好的字节数格式。
 */
private fun formatBytes(bytes: Long): String {
  if (bytes < 1024L) return "${bytes} B"
  val kb = bytes / 1024.0
  if (kb < 1024.0) return "${format1Decimal(kb)} KB"
  val mb = kb / 1024.0
  if (mb < 1024.0) return "${format1Decimal(mb)} MB"
  val gb = mb / 1024.0
  return "${format1Decimal(gb)} GB"
}

/**
 * 保留一位小数。
 */
private fun format1Decimal(value: Double): String {
  val scaled = (value * 10.0).roundToInt() / 10.0
  return scaled.toString()
}

/**
 * 校验下载文件的 SHA-256。
 */
private fun verifyAssetSha256(file: File, expectedSha256: String?) {
  val expected = expectedSha256?.trim()?.takeIf { it.isNotBlank() } ?: return
  val digest = MessageDigest.getInstance("SHA-256")
  file.inputStream().use { input ->
    val buffer = ByteArray(64 * 1024)
    while (true) {
      val read = input.read(buffer)
      if (read <= 0) break
      digest.update(buffer, 0, read)
    }
  }
  val actual = digest.digest().joinToString("") { byte ->
    val intValue = byte.toInt() and 0xff
    intValue.toString(16).padStart(2, '0')
  }
  if (!actual.equals(expected, ignoreCase = true)) {
    throw IllegalStateException("下载文件校验失败，请重试")
  }
}

/**
 * 下载目录：系统临时目录下的 zutil-updater/releaseTag。
 */
private fun buildUpdateDownloadDir(releaseTag: String): File {
  val safeTag = releaseTag.filter { ch ->
    ch.isLetterOrDigit() || ch == '.' || ch == '-' || ch == '_'
  }.ifBlank { "latest" }
  return File(System.getProperty("java.io.tmpdir"), "$UPDATE_DOWNLOAD_DIR_NAME/$safeTag")
}

/**
 * 通过标签解析镜像地址。
 */
private fun resolveMirrorBaseUrlByLabel(label: String): String {
  return UPDATE_MIRROR_OPTIONS.firstOrNull { it.label == label }?.baseUrl.orEmpty()
}

/**
 * 按镜像规则组装 URL。
 * mirrorBaseUrl 为空时直接返回原始地址。
 */
private fun applyUpdateMirror(originalUrl: String, mirrorBaseUrl: String): String {
  val base = mirrorBaseUrl.trim()
  if (base.isBlank()) return originalUrl
  return if (base.endsWith("/")) {
    "$base$originalUrl"
  } else {
    "$base/$originalUrl"
  }
}

/**
 * 当前操作系统识别。
 */
private fun detectOsFamily(): OsFamily {
  val osName = System.getProperty("os.name")?.lowercase().orEmpty()
  return when {
    osName.contains("win") -> OsFamily.WINDOWS
    osName.contains("mac") -> OsFamily.MAC
    osName.contains("linux") -> OsFamily.LINUX
    else -> OsFamily.OTHER
  }
}

/**
 * 当前 CPU 架构识别。
 */
private fun detectCpuFamily(): CpuFamily {
  val arch = System.getProperty("os.arch")?.lowercase().orEmpty()
  return when {
    arch.contains("aarch64") || arch.contains("arm64") -> CpuFamily.ARM64
    arch.contains("amd64") || arch.contains("x86_64") -> CpuFamily.X64
    arch.contains("x86") || arch.contains("i386") || arch.contains("i686") -> CpuFamily.X86
    arch.startsWith("arm") -> CpuFamily.ARM
    else -> CpuFamily.OTHER
  }
}

/**
 * 判断当前 Linux 发行版是否 Arch 系。
 */
private fun isArchLikeLinuxDistro(): Boolean {
  val osRelease = parseOsRelease()
  val id = osRelease["id"]?.lowercase().orEmpty()
  val idLike = osRelease["id_like"]?.lowercase().orEmpty()
  return id == "arch" ||
    id == "manjaro" ||
    id.contains("endeavouros") ||
    idLike.contains("arch")
}

/**
 * 读取 /etc/os-release。
 */
private fun parseOsRelease(): Map<String, String> {
  val file = File("/etc/os-release")
  if (!file.exists()) return emptyMap()
  return runCatching {
    file.readLines()
      .mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isBlank() || trimmed.startsWith("#")) return@mapNotNull null
        val idx = trimmed.indexOf('=')
        if (idx <= 0) return@mapNotNull null
        val key = trimmed.substring(0, idx).trim().lowercase()
        val rawValue = trimmed.substring(idx + 1).trim()
        val value = rawValue.removeSurrounding("\"")
        key to value
      }
      .toMap()
  }.getOrDefault(emptyMap())
}

/**
 * 文件名是否包含架构提示词。
 */
private fun containsArchHint(name: String): Boolean {
  return ARCH_HINTS.any { hint -> name.contains(hint) }
}

/**
 * 文件名架构是否匹配当前系统。
 */
private fun matchesCurrentArch(name: String, cpu: CpuFamily): Boolean {
  val hints = when (cpu) {
    CpuFamily.X64 -> listOf("x64", "x86_64", "amd64")
    CpuFamily.ARM64 -> listOf("arm64", "aarch64")
    CpuFamily.X86 -> listOf("i386", "i686", "x86_32", "x86-32")
    CpuFamily.ARM -> listOf("arm")
    CpuFamily.OTHER -> emptyList()
  }
  if (hints.isEmpty()) return true
  return hints.any { hint -> name.contains(hint) }
}

/**
 * 测试开关：忽略版本比较，始终提示有更新。
 * 启动参数示例：-Dzutil.update.force=true
 */
private fun isForceUpdateEnabledForTest(): Boolean {
  val prop = System.getProperty(UPDATE_FORCE_SYSTEM_PROPERTY)?.trim().orEmpty()
  val env = System.getenv(UPDATE_FORCE_ENV_KEY)?.trim().orEmpty()
  return isTruthyFlag(prop) || isTruthyFlag(env)
}

/**
 * 解析常见开关值：true/1/yes/on
 */
private fun isTruthyFlag(raw: String): Boolean {
  return when (raw.lowercase()) {
    "true", "1", "yes", "on" -> true
    else -> false
  }
}

/**
 * 比较 release tag 与当前版本：返回值 > 0 表示有更新。
 */
private fun compareReleaseVersion(releaseTag: String, currentVersion: String): Int {
  val remote = parseVersion(releaseTag)
  val local = parseVersion(currentVersion)
  if (remote != null && local != null) {
    return remote.compareTo(local)
  }
  val normalizedRemote = releaseTag.trim().removePrefix("v").removePrefix("V")
  val normalizedLocal = currentVersion.trim().removePrefix("v").removePrefix("V")
  return normalizedRemote.compareTo(normalizedLocal, ignoreCase = true)
}

/**
 * 解析版本号，支持 v1.2.3 / 1.2.3-beta.1 等格式。
 */
private fun parseVersion(raw: String): ParsedVersion? {
  val cleaned = raw.trim()
  val match = SEMVER_PATTERN.matchEntire(cleaned) ?: return null
  val major = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
  val minor = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
  val patch = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 0
  val preRelease = match.groupValues.getOrNull(4)
    ?.takeIf { it.isNotBlank() }
    ?.split('.')
    .orEmpty()
  return ParsedVersion(major, minor, patch, preRelease)
}

/**
 * 预发布标识比较规则（遵循 semver 规则）。
 */
private fun comparePreReleaseIdentifier(left: String, right: String): Int {
  val leftNum = left.toLongOrNull()
  val rightNum = right.toLongOrNull()
  return when {
    leftNum != null && rightNum != null -> leftNum.compareTo(rightNum)
    leftNum != null -> -1
    rightNum != null -> 1
    else -> left.compareTo(right)
  }
}

// 应用基础信息
private const val APP_VERSION = "1.0.0"
private const val GITHUB_REPO_URL = "https://github.com/duanluan/zutil-desktop"
private const val GITHUB_RELEASES_URL = "https://github.com/duanluan/zutil-desktop/releases"
private const val GITHUB_RELEASES_API_URL = "https://api.github.com/repos/duanluan/zutil-desktop/releases?per_page=100"
private const val GITHUB_OWNER_URL = "https://github.com/duanluan"
private const val GITHUB_CONTRIBUTORS_URL = "https://github.com/duanluan/zutil-desktop/graphs/contributors"
private const val ZUTIL_PROJECT_URL = "https://github.com/duanluan/zutil"
private const val QQ_GROUP_URL =
  "http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=vlo9IzNeTEqtUk2cO1Ubiasyl3N5RdMA&authKey=XNuH4tmWfVx3%2Fs%2FcXXfC6QpJdyJ3P0itndvDoud0iTwzBffIAo3o1KChqDcR422B&noverify=0&group_code=273743748"
private const val DISCORD_URL = "https://discord.gg/N39y9EvYC9"
private const val BLOG_URL = "https://blog.zhjh.top"
private const val UPDATE_CHECK_USER_AGENT = "zutil-desktop-update-checker"
private const val UPDATE_FORCE_SYSTEM_PROPERTY = "zutil.update.force"
private const val UPDATE_FORCE_ENV_KEY = "ZUTIL_UPDATE_FORCE"
private const val UPDATE_DOWNLOAD_DIR_NAME = "zutil-updater"
private const val UPDATE_DOWNLOAD_PROGRESS_INTERVAL_MS = 120L
private const val UPDATE_DOWNLOAD_MAX_RETRIES = 3
private const val UPDATE_DOWNLOAD_RETRY_DELAY_MS = 1200L
private const val UPDATE_DOWNLOAD_TIMEOUT_MINUTES = 20L
internal const val UPDATE_POST_EXIT_LAUNCH_DELAY_SECONDS = 2
private object UpdateCodeSourceMarker
private val SHA256_REGEX = Regex("^[0-9a-fA-F]{64}$")
private val ARCH_HINTS = listOf("x64", "x86_64", "amd64", "arm64", "aarch64", "i386", "i686", "x86_32", "arm")
private val SEMVER_PATTERN = Regex("^v?(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-([0-9A-Za-z.-]+))?(?:\\+.*)?$")
private val githubHttpClient: HttpClient = HttpClient.newBuilder()
  .connectTimeout(Duration.ofSeconds(8))
  .followRedirects(HttpClient.Redirect.NORMAL)
  .build()
private val UPDATE_MIRROR_OPTIONS = listOf(
  UpdateMirrorOption(label = "不使用镜像", baseUrl = ""),
  UpdateMirrorOption(label = "gh-proxy.com", baseUrl = "https://gh-proxy.com/"),
  UpdateMirrorOption(label = "ghproxy.net", baseUrl = "https://ghproxy.net/"),
  UpdateMirrorOption(label = "ghfast.top", baseUrl = "https://ghfast.top/"),
  UpdateMirrorOption(label = "fastgit.cc", baseUrl = "https://fastgit.cc/")
)


private const val PREF_INCLUDE_PRERELEASE = "update.includePrerelease"
private val updatePrefs: Preferences = Preferences.userNodeForPackage(UpdatePrefsMarker::class.java)
private object UpdatePrefsMarker

/**
 * 读取是否包含测试版本更新。
 */
private fun loadIncludePrerelease(): Boolean {
  return updatePrefs.getBoolean(PREF_INCLUDE_PRERELEASE, false)
}

/**
 * 保存是否包含测试版本更新。
 */
private fun saveIncludePrerelease(include: Boolean) {
  updatePrefs.putBoolean(PREF_INCLUDE_PRERELEASE, include)
}
