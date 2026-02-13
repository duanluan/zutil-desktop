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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import compose.icons.FeatherIcons
import compose.icons.SimpleIcons
import compose.icons.feathericons.*
import compose.icons.simpleicons.Discord
import compose.icons.simpleicons.Tencentqq
import org.jetbrains.compose.resources.painterResource
import top.zhjh.common.composable.ToastManager
import top.zhjh.composable.ToolContent
import top.zhjh.enums.ToolCategory
import top.zhjh.enums.ToolItem
import top.zhjh.util.FilePickerUtil
import top.zhjh.util.ModelDownloadSettings
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
import java.io.File
import java.net.URI
import java.util.prefs.Preferences
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 应用根入口。
 * 负责：
 * 1. 检测并保存 UI 缩放比例。
 * 2. 构建主窗口与工具子窗口。
 * 3. 统一主题与缩放容器。
 */
@Composable
fun ApplicationScope.AppRoot() {
  // 系统缩放检测（只初始化一次）
  val autoScale = remember { detectSystemScale() }
  // 基于系统缩放得到默认百分比
  val defaultScalePercent = remember(autoScale) {
    sanitizeScalePercent(scaleToPercent(autoScale.scale))
  }
  // 当前 UI 缩放百分比（持久化存储）
  var scalePercent by remember { mutableStateOf(loadUiScalePercent(defaultScalePercent)) }
  // 当前已打开的工具窗口（避免重复打开）
  val openWindows = remember { mutableStateListOf<ToolItem>() }

  // 修改缩放比例：规范化并写入配置
  val onScalePercentChange: (Int) -> Unit = { percent ->
    val clamped = sanitizeScalePercent(percent)
    scalePercent = clamped
    saveUiScalePercent(clamped)
  }
  // 打开工具窗口：去重后加入列表
  val onToolOpen: (ToolItem) -> Unit = { tool ->
    if (!openWindows.contains(tool)) {
      openWindows.add(tool)
    }
  }

  // 主窗口
  Window(
    onCloseRequest = ::exitApplication,
    title = "ZUtil 工具箱",
    state = rememberWindowState(position = WindowPosition(Alignment.Center))
  ) {
    // 统一缩放与主题入口
    ScaledContent(autoScale, scalePercent) {
      ZTheme {
        App(
          autoScale = autoScale,
          scalePercent = scalePercent,
          onScalePercentChange = onScalePercentChange,
          onToolOpen = onToolOpen
        )
      }
    }
  }

  // 为每个已打开工具创建独立窗口
  for (tool in openWindows) {
    key(tool) {
      // 针对不同工具配置默认窗口尺寸
      val windowState = when (tool) {
        ToolItem.TIMESTAMP -> rememberWindowState(width = 800.dp, height = 720.dp, position = WindowPosition(Alignment.Center))
        ToolItem.JSON -> rememberWindowState(width = 1100.dp, height = 800.dp, position = WindowPosition(Alignment.Center))
        ToolItem.SPEECH_TO_TEXT -> rememberWindowState(width = 900.dp, height = 800.dp, position = WindowPosition(Alignment.Center))
      }

      Window(
        onCloseRequest = { openWindows.remove(tool) },
        title = tool.toolName,
        state = windowState
      ) {
        // 子窗口同样应用缩放与主题
        ScaledContent(autoScale, scalePercent) {
          ZTheme {
            ToolContent(tool)
          }
        }
      }
    }
  }
}

/**
 * 主界面：左侧导航 + 右侧内容区。
 * 负责分类切换、搜索过滤与页面内容渲染。
 */
@Composable
private fun App(
  autoScale: ScaleDetection,
  scalePercent: Int,
  onScalePercentChange: (Int) -> Unit,
  onToolOpen: (ToolItem) -> Unit
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    // 使用枚举来管理当前选中的分类，默认选中常用
    var selectedCategory by remember { mutableStateOf(ToolCategory.COMMON) }

    // 搜索关键字（仅对工具列表生效）
    val searchText = remember { mutableStateOf("") }

    // --- 左侧导航栏 ---
    // 固定宽度保持布局稳定与视觉一致
    NavigationRail(modifier = Modifier.width(72.dp)) {
      // 选中/未选中的颜色
      val selectedColor = Color(0xff409eff)
      val unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)


      // 1. 常用
      val commonSelected = selectedCategory == ToolCategory.COMMON
      val commonColor = if (commonSelected) selectedColor else unselectedColor
      NavigationRailItem(
        icon = {
          Icon(
            // 暂时复用之前的资源，建议后续换成矢量图 Icons.Filled.Home
            painter = painterResource(Res.drawable.commonly_used),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = commonColor
          )
        },
        label = { Text(ToolCategory.COMMON.label, color = commonColor) },
        selected = commonSelected,
        onClick = { selectedCategory = ToolCategory.COMMON }
      )

      // 2. AI
      val aiSelected = selectedCategory == ToolCategory.AI
      val aiColor = if (aiSelected) selectedColor else unselectedColor
      NavigationRailItem(
        // 如果报错找不到图标，可临时替换为其他内置图标
        icon = { Icon(FeatherIcons.Cpu, null, tint = aiColor) },
        label = { Text(ToolCategory.AI.label, color = aiColor) },
        selected = aiSelected,
        onClick = { selectedCategory = ToolCategory.AI }
      )

      // 把设置与关于放到底部
      Spacer(Modifier.weight(1f))

      // 3. 设置 (通常设置在最底部)
      val settingsSelected = selectedCategory == ToolCategory.SETTINGS
      val settingsColor = if (settingsSelected) selectedColor else unselectedColor
      NavigationRailItem(
        icon = { Icon(FeatherIcons.Settings, null, tint = settingsColor) },
        label = { Text(ToolCategory.SETTINGS.label, color = settingsColor) },
        selected = settingsSelected,
        onClick = { selectedCategory = ToolCategory.SETTINGS }
      )

      // 4. 关于
      val aboutSelected = selectedCategory == ToolCategory.ABOUT
      val aboutColor = if (aboutSelected) selectedColor else unselectedColor
      NavigationRailItem(
        icon = { Icon(FeatherIcons.Info, null, tint = aboutColor) },
        label = { Text(ToolCategory.ABOUT.label, color = aboutColor) },
        selected = aboutSelected,
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
            onScalePercentChange = onScalePercentChange
          )
        } else if (selectedCategory == ToolCategory.ABOUT) {
          AboutPage()
        } else {
          // 工具列表页面
          ToolListGrid(
            category = selectedCategory,
            filterText = searchText.value,
            onToolClick = onToolOpen
          )
        }
      }

    }
  }
}

/**
 * 缩放容器：通过替换 LocalDensity 实现整体 UI 缩放。
 * 结合系统缩放与用户设置的比例计算最终密度。
 */
@Composable
private fun ScaledContent(
  autoScale: ScaleDetection,
  scalePercent: Int,
  content: @Composable () -> Unit
) {
  val baseDensity = LocalDensity.current
  // 将百分比转换为比例并做安全限制
  val targetScale = sanitizeScale(
    scalePercentToScale(scalePercent),
    fallback = autoScale.scale
  )
  // 避免密度为 0 导致除零
  val safeBaseDensity = if (baseDensity.density > 0f) baseDensity.density else 1f
  val scaleMultiplier = targetScale / safeBaseDensity
  val scaledDensity = remember(baseDensity, scaleMultiplier) {
    Density(
      density = baseDensity.density * scaleMultiplier,
      fontScale = baseDensity.fontScale * scaleMultiplier
    )
  }

  CompositionLocalProvider(LocalDensity provides scaledDensity) {
    content()
  }
}

/**
 * 设置页：提供 UI 缩放与模型下载目录设置。
 */
@Composable
private fun SettingsPage(
  autoScale: ScaleDetection,
  scalePercent: Int,
  onScalePercentChange: (Int) -> Unit
) {
  var percentText by remember { mutableStateOf(scalePercent.toString()) }
  var downloadDirText by remember { mutableStateOf(ModelDownloadSettings.loadOrDefault()) }
  LaunchedEffect(scalePercent) {
    // 外部缩放变化时同步输入框内容
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
                // 输入合法时即时校验并限制范围
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
              // 滚轮缩放：向上增大，向下减小
              val step = if (delta < 0f) SCALE_WHEEL_STEP else -SCALE_WHEEL_STEP
              onScalePercentChange((scalePercent + step).coerceIn(SCALE_MIN_PERCENT, SCALE_MAX_PERCENT))
            },
            onFocusChanged = { focused ->
              // 失焦时做最终修正，保证有效值
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
          // 显示默认缩放与来源，便于用户理解当前基准
          "默认缩放${formatPercent(autoScale.scale)}，来源：${autoScale.source}",
          style = MaterialTheme.typography.caption
        )
      }
    }

    ZCard(
      shadow = ZCardShadow.NEVER,
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("模型下载目录", style = MaterialTheme.typography.subtitle1)

        Row(verticalAlignment = Alignment.CenterVertically) {
          ZTextField(
            value = downloadDirText,
            onValueChange = { downloadDirText = it },
            onFocusChanged = { focused ->
              // 失焦时保存；无效则回滚并提示
              if (!focused) {
                if (!ModelDownloadSettings.save(downloadDirText)) {
                  ToastManager.error("下载目录无效或不可写")
                  downloadDirText = ModelDownloadSettings.loadOrDefault()
                }
              }
            },
            modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          ZButton(
            type = ZColorType.DEFAULT,
            onClick = {
              // 通过文件选择器选择目录并保存
              val path = FilePickerUtil.pickDirectory("选择模型下载目录")
              if (path != null) {
                if (ModelDownloadSettings.save(path)) {
                  downloadDirText = path
                } else {
                  ToastManager.error("下载目录无效或不可写")
                }
              }
            }
          ) {
            Text("浏览")
          }
          Spacer(modifier = Modifier.width(6.dp))
          ZButton(
            type = ZColorType.DEFAULT,
            onClick = { openDirectory(downloadDirText) }
          ) {
            Text("打开")
          }
        }

        Text(
          "用于语音模型下载与缓存",
          style = MaterialTheme.typography.caption
        )
      }
    }
  }
}

/**
 * 关于页：展示版本信息、贡献者与社区链接。
 */
@Composable
private fun AboutPage() {
  val scrollState = rememberScrollState()
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
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "ZUtil 工具箱",
              style = MaterialTheme.typography.subtitle1,
              modifier = Modifier.clickable { openUrl(GITHUB_REPO_URL) }
            )
          }
          // 版本号展示
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

/**
 * 工具网格：根据分类与搜索词渲染工具按钮列表。
 */
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
private fun openDirectory(path: String) {
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

// 应用基础信息
private const val APP_VERSION = "1.0.0"
private const val GITHUB_REPO_URL = "https://github.com/duanluan/zutil-desktop"
private const val GITHUB_OWNER_URL = "https://github.com/duanluan"
private const val GITHUB_CONTRIBUTORS_URL = "https://github.com/duanluan/zutil-desktop/graphs/contributors"
private const val ZUTIL_PROJECT_URL = "https://github.com/duanluan/zutil"
private const val QQ_GROUP_URL =
  "http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=vlo9IzNeTEqtUk2cO1Ubiasyl3N5RdMA&authKey=XNuH4tmWfVx3%2Fs%2FcXXfC6QpJdyJ3P0itndvDoud0iTwzBffIAo3o1KChqDcR422B&noverify=0&group_code=273743748"
private const val DISCORD_URL = "https://discord.gg/N39y9EvYC9"
private const val BLOG_URL = "https://blog.zhjh.top"

/**
 * 缩放检测结果。
 * scale: 实际缩放比例
 * source: 取值来源说明（便于排查与提示）
 */
private data class ScaleDetection(
  val scale: Float,
  val source: String
)

// 缩放相关常量
private const val PREF_UI_SCALE_PERCENT = "uiScale.percent"
private const val SCALE_MIN = 0.5f
private const val SCALE_MAX = 3.0f
private const val SCALE_MIN_PERCENT = 50
private const val SCALE_MAX_PERCENT = 300
private const val SCALE_WHEEL_STEP = 1

// 用于持久化保存 UI 缩放配置
private val uiScalePrefs: Preferences = Preferences.userNodeForPackage(ScaleDetection::class.java)

/**
 * 读取 UI 缩放百分比配置，若不存在则返回默认值。
 */
private fun loadUiScalePercent(defaultPercent: Int): Int {
  val raw = uiScalePrefs.getInt(PREF_UI_SCALE_PERCENT, defaultPercent)
  return sanitizeScalePercent(raw)
}

/**
 * 保存 UI 缩放百分比配置。
 */
private fun saveUiScalePercent(percent: Int) {
  uiScalePrefs.putInt(PREF_UI_SCALE_PERCENT, percent)
}

/**
 * 从多个渠道检测系统缩放比例。
 * 优先级：
 * 1. JVM 属性 sun.java2d.uiScale
 * 2. AWT 屏幕变换矩阵
 * 3. 环境变量（GTK/Qt/X11）
 * 4. DPI 推断
 */
private fun detectSystemScale(): ScaleDetection {
  // 1) JVM 属性
  val jvmScale = parseScaleProperty(System.getProperty("sun.java2d.uiScale"))
  if (jvmScale != null) {
    return ScaleDetection(sanitizeScale(jvmScale), "jvm: sun.java2d.uiScale")
  }

  // 2) AWT 默认屏幕变换矩阵
  val awtScale = runCatching {
    val config = GraphicsEnvironment.getLocalGraphicsEnvironment()
      .defaultScreenDevice
      .defaultConfiguration
    max(config.defaultTransform.scaleX, config.defaultTransform.scaleY).toFloat()
  }.getOrNull()

  if (awtScale != null && awtScale > 0f && awtScale != 1f) {
    return ScaleDetection(sanitizeScale(awtScale), "awt: defaultTransform")
  }

  // 3) 环境变量（Linux/Wayland/Qt 等）
  val envScale = detectEnvScale()
  if (envScale != null) {
    return envScale
  }

  // 4) DPI 推断
  val dpiScale = detectDpiScale()
  if (dpiScale != null) {
    return dpiScale
  }

  // 兜底：如果 awtScale 无效，回退到 1x
  return ScaleDetection(sanitizeScale(awtScale ?: 1f), "awt: defaultTransform")
}

/**
 * 读取常见桌面环境的缩放相关环境变量。
 */
private fun detectEnvScale(): ScaleDetection? {
  // GTK：GDK_SCALE / GDK_DPI_SCALE
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

  // Qt：多屏配置 QT_SCREEN_SCALE_FACTORS
  val qtScreenScale = System.getenv("QT_SCREEN_SCALE_FACTORS")
    ?.split(';', ',', ' ')
    ?.firstOrNull { it.isNotBlank() }
    ?.let { parseScaleProperty(it) }
  if (qtScreenScale != null) {
    return ScaleDetection(sanitizeScale(qtScreenScale), "env: QT_SCREEN_SCALE_FACTORS")
  }

  // X11：XFT_DPI
  val xftDpi = parseScaleProperty(System.getenv("XFT_DPI"))
  if (xftDpi != null && xftDpi > 0f) {
    return ScaleDetection(sanitizeScale(xftDpi / 96f), "env: XFT_DPI")
  }

  return null
}

/**
 * 通过系统 DPI 推断缩放比例。
 * macOS 基准为 72 DPI，其他平台通常为 96 DPI。
 */
private fun detectDpiScale(): ScaleDetection? {
  val dpi = runCatching { Toolkit.getDefaultToolkit().screenResolution }.getOrNull()
  if (dpi == null || dpi <= 0) return null
  val isMac = System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true
  val baseDpi = if (isMac) 72f else 96f
  val scale = dpi / baseDpi
  if (scale <= 0f) return null
  return ScaleDetection(sanitizeScale(scale), "awt: dpi=$dpi")
}

/**
 * 解析缩放属性字符串，支持 "1.25x" 这类格式。
 */
private fun parseScaleProperty(raw: String?): Float? {
  val cleaned = raw?.trim()?.removeSuffix("x")?.removeSuffix("X")?.trim()
  return cleaned?.toFloatOrNull()
}

/**
 * 规范化缩放值，确保在安全范围内。
 */
private fun sanitizeScale(value: Float, fallback: Float = 1f): Float {
  if (value.isNaN() || value.isInfinite()) return fallback
  return value.coerceIn(SCALE_MIN, SCALE_MAX)
}

/**
 * 将缩放比例格式化为百分比字符串。
 */
private fun formatPercent(scale: Float): String {
  return "${(scale * 100).roundToInt()}%"
}

/**
 * 缩放比例 -> 百分比整数。
 */
private fun scaleToPercent(scale: Float): Int {
  return (scale * 100).roundToInt()
}

/**
 * 百分比整数 -> 缩放比例。
 */
private fun scalePercentToScale(percent: Int): Float {
  return percent / 100f
}

/**
 * 规范化百分比，确保在安全范围内。
 */
private fun sanitizeScalePercent(value: Int): Int {
  return value.coerceIn(SCALE_MIN_PERCENT, SCALE_MAX_PERCENT)
}
