package top.zhjh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import compose.icons.FeatherIcons
import compose.icons.SimpleIcons
import compose.icons.feathericons.*
import compose.icons.simpleicons.Discord
import compose.icons.simpleicons.Tencentqq
import org.jetbrains.compose.resources.painterResource
import top.zhjh.composable.ToolContent
import top.zhjh.enums.ToolCategory
import top.zhjh.enums.ToolItem
import top.zhjh.zui.composable.ZButton
import top.zhjh.zui.composable.ZCard
import top.zhjh.zui.composable.ZTextField
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.ZTheme
import zutil_desktop.composeapp.generated.resources.Res
import zutil_desktop.composeapp.generated.resources.commonly_used
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.net.URI
import java.util.prefs.Preferences
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
@Preview
fun App() {
  val baseDensity = LocalDensity.current
  val autoScale = remember(baseDensity) { detectSystemScale() }
  val defaultScalePercent = sanitizeScalePercent(scaleToPercent(autoScale.scale))
  var scalePercent by remember { mutableStateOf(loadUiScalePercent(defaultScalePercent)) }
  val targetScale = sanitizeScale(
    scalePercentToScale(scalePercent),
    fallback = autoScale.scale
  )
  val safeBaseDensity = if (baseDensity.density > 0f) baseDensity.density else 1f
  val scaleMultiplier = targetScale / safeBaseDensity
  val scaledDensity = remember(baseDensity, scaleMultiplier) {
    Density(
      density = baseDensity.density * scaleMultiplier,
      fontScale = baseDensity.fontScale * scaleMultiplier
    )
  }

  CompositionLocalProvider(LocalDensity provides scaledDensity) {
    ZTheme() {
      Row(modifier = Modifier.fillMaxWidth()) {
        // 使用枚举来管理当前选中的分类，默认选中常用
        var selectedCategory by remember { mutableStateOf(ToolCategory.COMMON) }

        val searchText = remember { mutableStateOf("") }
        val openWindows = remember { mutableStateListOf<ToolItem>() }

        // --- 左侧导航栏 ---
        NavigationRail(modifier = Modifier.width(72.dp)) { // 给个固定宽度美观些

          // 1. 常用
          NavigationRailItem(
            icon = {
              Icon(
                // 暂时复用之前的资源，建议后续换成矢量图 Icons.Filled.Home
                painter = painterResource(Res.drawable.commonly_used),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
            },
            label = { Text(ToolCategory.COMMON.label) },
            selected = selectedCategory == ToolCategory.COMMON,
            onClick = { selectedCategory = ToolCategory.COMMON }
          )

          // 2. AI
          NavigationRailItem(
            icon = { Icon(FeatherIcons.Cpu, null) }, // 如果报错找不到图标，暂时用 FeatherIcons.Star
            label = { Text(ToolCategory.AI.label) },
            selected = selectedCategory == ToolCategory.AI,
            onClick = { selectedCategory = ToolCategory.AI }
          )

          Spacer(Modifier.weight(1f)) // 把设置顶到底部

          // 3. 设置 (通常设置在最底部)
        NavigationRailItem(
          icon = { Icon(FeatherIcons.Settings, null) },
          label = { Text(ToolCategory.SETTINGS.label) },
          selected = selectedCategory == ToolCategory.SETTINGS,
          onClick = { selectedCategory = ToolCategory.SETTINGS }
        )

        NavigationRailItem(
          icon = { Icon(FeatherIcons.Info, null) },
          label = { Text(ToolCategory.ABOUT.label) },
          selected = selectedCategory == ToolCategory.ABOUT,
          onClick = { selectedCategory = ToolCategory.ABOUT }
        )
      }

        // --- 右侧内容区域 ---
        Column(Modifier.fillMaxSize().padding(10.dp)) {

          // 只有在非设置页面显示搜索框
          if (selectedCategory != ToolCategory.SETTINGS && selectedCategory != ToolCategory.ABOUT) {
            ZTextField(
              value = searchText.value,
              onValueChange = { searchText.value = it },
              placeholder = "搜索${selectedCategory.label}工具...",
              leadingIcon = { Icon(FeatherIcons.Search, contentDescription = null) },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          // 内容切换逻辑
          Box(modifier = Modifier.weight(1f)) {
            if (selectedCategory == ToolCategory.SETTINGS) {
              SettingsPage(
                autoScale = autoScale,
                scalePercent = scalePercent,
                onScalePercentChange = { percent ->
                  val clamped = sanitizeScalePercent(percent)
                  scalePercent = clamped
                  saveUiScalePercent(clamped)
                }
              )
            } else if (selectedCategory == ToolCategory.ABOUT) {
              AboutPage()
            } else {
              // 工具列表页面
              ToolListGrid(
                category = selectedCategory,
                filterText = searchText.value,
                onToolClick = { tool ->
                  // 避免重复打开
                  if (!openWindows.contains(tool)) {
                    openWindows.add(tool)
                  }
                }
              )
            }
          }

          // --- 窗口管理逻辑 (保持不变，增加新工具的窗口大小配置) ---
          for (tool in openWindows) {
            val windowState = when (tool) {
              ToolItem.TIMESTAMP -> rememberWindowState(width = 800.dp, height = 720.dp, position = WindowPosition(Alignment.Center))
              ToolItem.JSON -> rememberWindowState(width = 1100.dp, height = 800.dp, position = WindowPosition(Alignment.Center))

              ToolItem.SPEECH_TO_TEXT -> rememberWindowState(width = 900.dp, height = 800.dp, position = WindowPosition(Alignment.Center)) // AI 工具通常需要大一点
            }

            Window(
              onCloseRequest = { openWindows.remove(tool) },
              title = tool.toolName,
              state = windowState
            ) {
              CompositionLocalProvider(LocalDensity provides scaledDensity) {
                ToolContent(tool)
              }
            }
          }
        }
      }
    }
  }
}

// 抽离：设置页面组件
@Composable
private fun SettingsPage(
  autoScale: ScaleDetection,
  scalePercent: Int,
  onScalePercentChange: (Int) -> Unit
) {
  var percentText by remember { mutableStateOf(scalePercent.toString()) }
  LaunchedEffect(scalePercent) {
    val normalized = scalePercent.toString()
    if (percentText != normalized) {
      percentText = normalized
    }
  }

  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("常规设置", style = MaterialTheme.typography.h5)

    ZCard(
      shadow = ZCardShadow.NEVER,
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("界面缩放", style = MaterialTheme.typography.subtitle1)

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("缩放比例：")
          Spacer(modifier = Modifier.width(8.dp))
          ZTextField(
            value = percentText,
            onValueChange = { value ->
              percentText = value
              val parsed = value.toIntOrNull()
              if (parsed != null) {
                if (parsed > SCALE_MAX_PERCENT) {
                  val clamped = SCALE_MAX_PERCENT
                  percentText = clamped.toString()
                  onScalePercentChange(clamped)
                } else if (parsed >= SCALE_MIN_PERCENT) {
                  onScalePercentChange(parsed)
                }
              }
            },
            numericOnly = true,
            onMouseWheel = { delta ->
              val step = if (delta < 0f) SCALE_WHEEL_STEP else -SCALE_WHEEL_STEP
              onScalePercentChange((scalePercent + step).coerceIn(SCALE_MIN_PERCENT, SCALE_MAX_PERCENT))
            },
            onFocusChanged = { focused ->
              if (!focused) {
                val parsed = percentText.toIntOrNull()
                val resolved = when {
                  parsed == null -> scalePercent
                  parsed < SCALE_MIN_PERCENT -> SCALE_MIN_PERCENT
                  parsed > SCALE_MAX_PERCENT -> SCALE_MAX_PERCENT
                  else -> parsed
                }
                percentText = resolved.toString()
                if (resolved != scalePercent) {
                  onScalePercentChange(resolved)
                }
              }
            },
            trailingIcon = { Text("%", fontSize = 12.sp) },
            modifier = Modifier.width(120.dp)
          )
        }

        Text(
          "默认缩放${formatPercent(autoScale.scale)}，来源：${autoScale.source}",
          style = MaterialTheme.typography.caption
        )
      }
    }
  }
}

@Composable
private fun AboutPage() {
  val scrollState = rememberScrollState()
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
  val thanksCards = listOf(
    LinkCardData(
      title = "zutil",
      lines = listOf("追求更快更全的 Java 工具类"),
      url = ZUTIL_PROJECT_URL
    )
  )
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
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "ZUtil 工具箱",
              style = MaterialTheme.typography.subtitle1,
              modifier = Modifier.clickable { openUrl(GITHUB_REPO_URL) }
            )
          }
          Text("版本号：$APP_VERSION")
          Spacer(modifier = Modifier.width(8.dp))
          ZButton(
            type = ZColorType.DEFAULT,
            plain = true,
            enabled = false,
            onClick = {}
          ) {
            Text("检查更新")
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
}

// 抽离：工具网格组件
@Composable
fun ToolListGrid(
  category: ToolCategory,
  filterText: String,
  onToolClick: (ToolItem) -> Unit
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 200.dp),
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 核心过滤逻辑：先按分类筛选，再按搜索词筛选
    val tools = ToolItem.entries.filter {
      it.category == category &&
        (filterText.isEmpty() || it.toolName.contains(filterText, ignoreCase = true))
    }

    items(tools.size) { i ->
      val tool = tools[i]
      ZButton(
        type = ZColorType.PRIMARY,
        contentPadding = PaddingValues(15.dp),
        contentAlignment = Alignment.CenterStart,
        onClick = { onToolClick(tool) }
      ) {
        // 根据工具类型动态显示图标
        val iconModifier = Modifier.size(24.dp).padding(end = 8.dp)
        if (tool.category == ToolCategory.AI) {
          Icon(FeatherIcons.Cpu, null, modifier = iconModifier)
        } else {
          Icon(painterResource(Res.drawable.commonly_used), null, modifier = iconModifier)
        }

        Text(tool.toolName)
      }
    }
  }
}

private data class LinkCardData(
  val title: String,
  val lines: List<String>,
  val url: String,
  val icon: ImageVector? = null
)

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
        repeat(columns - rowItems.size) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

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

private const val APP_VERSION = "1.0.0"
private const val GITHUB_REPO_URL = "https://github.com/duanluan/zutil-desktop"
private const val GITHUB_OWNER_URL = "https://github.com/duanluan"
private const val GITHUB_CONTRIBUTORS_URL = "https://github.com/duanluan/zutil-desktop/graphs/contributors"
private const val ZUTIL_PROJECT_URL = "https://github.com/duanluan/zutil"
private const val QQ_GROUP_URL =
  "http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=vlo9IzNeTEqtUk2cO1Ubiasyl3N5RdMA&authKey=XNuH4tmWfVx3%2Fs%2FcXXfC6QpJdyJ3P0itndvDoud0iTwzBffIAo3o1KChqDcR422B&noverify=0&group_code=273743748"
private const val DISCORD_URL = "https://discord.gg/N39y9EvYC9"
private const val BLOG_URL = "https://blog.zhjh.top"

private data class ScaleDetection(
  val scale: Float,
  val source: String
)

private const val PREF_UI_SCALE_PERCENT = "uiScale.percent"
private const val SCALE_MIN = 0.5f
private const val SCALE_MAX = 3.0f
private const val SCALE_MIN_PERCENT = 50
private const val SCALE_MAX_PERCENT = 300
private const val SCALE_WHEEL_STEP = 1

private val uiScalePrefs: Preferences = Preferences.userNodeForPackage(ScaleDetection::class.java)

private fun loadUiScalePercent(defaultPercent: Int): Int {
  val raw = uiScalePrefs.getInt(PREF_UI_SCALE_PERCENT, defaultPercent)
  return sanitizeScalePercent(raw)
}

private fun saveUiScalePercent(percent: Int) {
  uiScalePrefs.putInt(PREF_UI_SCALE_PERCENT, percent)
}

private fun detectSystemScale(): ScaleDetection {
  val jvmScale = parseScaleProperty(System.getProperty("sun.java2d.uiScale"))
  if (jvmScale != null) {
    return ScaleDetection(sanitizeScale(jvmScale), "jvm: sun.java2d.uiScale")
  }

  val awtScale = runCatching {
    val config = GraphicsEnvironment.getLocalGraphicsEnvironment()
      .defaultScreenDevice
      .defaultConfiguration
    max(config.defaultTransform.scaleX, config.defaultTransform.scaleY).toFloat()
  }.getOrNull()

  if (awtScale != null && awtScale > 0f && awtScale != 1f) {
    return ScaleDetection(sanitizeScale(awtScale), "awt: defaultTransform")
  }

  val envScale = detectEnvScale()
  if (envScale != null) {
    return envScale
  }

  val dpiScale = detectDpiScale()
  if (dpiScale != null) {
    return dpiScale
  }

  return ScaleDetection(sanitizeScale(awtScale ?: 1f), "awt: defaultTransform")
}

private fun detectEnvScale(): ScaleDetection? {
  val gdkScale = parseScaleProperty(System.getenv("GDK_SCALE"))
  val gdkDpiScale = parseScaleProperty(System.getenv("GDK_DPI_SCALE"))
  if (gdkScale != null || gdkDpiScale != null) {
    val scale = (gdkScale ?: 1f) * (gdkDpiScale ?: 1f)
    return ScaleDetection(sanitizeScale(scale), "env: GDK_SCALE/GDK_DPI_SCALE")
  }

  val qtScale = parseScaleProperty(System.getenv("QT_SCALE_FACTOR"))
  if (qtScale != null) {
    return ScaleDetection(sanitizeScale(qtScale), "env: QT_SCALE_FACTOR")
  }

  val qtScreenScale = System.getenv("QT_SCREEN_SCALE_FACTORS")
    ?.split(';', ',', ' ')
    ?.firstOrNull { it.isNotBlank() }
    ?.let { parseScaleProperty(it) }
  if (qtScreenScale != null) {
    return ScaleDetection(sanitizeScale(qtScreenScale), "env: QT_SCREEN_SCALE_FACTORS")
  }

  val xftDpi = parseScaleProperty(System.getenv("XFT_DPI"))
  if (xftDpi != null && xftDpi > 0f) {
    return ScaleDetection(sanitizeScale(xftDpi / 96f), "env: XFT_DPI")
  }

  return null
}

private fun detectDpiScale(): ScaleDetection? {
  val dpi = runCatching { Toolkit.getDefaultToolkit().screenResolution }.getOrNull()
  if (dpi == null || dpi <= 0) return null
  val isMac = System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true
  val baseDpi = if (isMac) 72f else 96f
  val scale = dpi / baseDpi
  if (scale <= 0f) return null
  return ScaleDetection(sanitizeScale(scale), "awt: dpi=$dpi")
}

private fun parseScaleProperty(raw: String?): Float? {
  val cleaned = raw?.trim()?.removeSuffix("x")?.removeSuffix("X")?.trim()
  return cleaned?.toFloatOrNull()
}

private fun sanitizeScale(value: Float, fallback: Float = 1f): Float {
  if (value.isNaN() || value.isInfinite()) return fallback
  return value.coerceIn(SCALE_MIN, SCALE_MAX)
}

private fun formatPercent(scale: Float): String {
  return "${(scale * 100).roundToInt()}%"
}

private fun scaleToPercent(scale: Float): Int {
  return (scale * 100).roundToInt()
}

private fun scalePercentToScale(percent: Int): Float {
  return percent / 100f
}

private fun sanitizeScalePercent(value: Int): Int {
  return value.coerceIn(SCALE_MIN_PERCENT, SCALE_MAX_PERCENT)
}
