package top.zhjh.zui.demo

import ZLink
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.zhjh.common.composable.ToastContainer
import top.zhjh.zui.composable.*
import top.zhjh.zui.demo.components.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.theme.ZTheme

private const val ZUI_COMPOSE_DESKTOP_REPO = "https://github.com/duanluan/zui-compose-desktop"

@Composable
@Preview
fun ZuiDemoApp() {
  ZuiComponentShowcase(
    modifier = Modifier.fillMaxSize(),
    useInternalScroll = true
  )
}

@Composable
fun ZuiComponentShowcase(
  modifier: Modifier = Modifier,
  useInternalScroll: Boolean = false
) {
  var isDarkTheme by remember { mutableStateOf(false) }

  ZTheme(isDarkTheme = isDarkTheme) {
    Surface(modifier = modifier) {
      if (useInternalScroll) {
        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize()) {
          ZuiComponentDemoContent(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
            modifier = Modifier.fillMaxSize(),
            contentScrollState = scrollState
          )
          VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
          )
          ToastContainer()
        }
      } else {
        Box(modifier = Modifier.fillMaxSize()) {
          ZuiComponentDemoContent(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
            modifier = Modifier.fillMaxSize()
          )
          ToastContainer()
        }
      }
    }
  }
}

@Composable
private fun ZuiComponentDemoContent(
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  modifier: Modifier = Modifier,
  contentScrollState: ScrollState? = null
) {
  var activeTabName by remember { mutableStateOf("zui-intro") }
  val asideScrollState = rememberScrollState()

  data class DemoItem(
    val label: String,
    val name: String,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit
  )

  val demos = listOf(
    DemoItem(label = "ZUI 组件库介绍", name = "zui-intro") { zuiIntroWindowContent() },
    DemoItem(label = "Text 文本", name = "text") { textDemoContent() },
    DemoItem(label = "Radio 单选框", name = "radio") { radioDemoContent() },
    DemoItem(label = "Checkbox 多选框", name = "checkbox") { checkboxDemoContent() },
    DemoItem(label = "Button 按钮", name = "button") { buttonDemoContent() },
    DemoItem(label = "TextField 输入框", name = "textfield") { textFieldDemoContent() },
    DemoItem(label = "DropdownMenu 选择器", name = "dropdown") { dropdownDemoContent(isDarkTheme) },
    DemoItem(label = "Switch 开关", name = "switch") { switchDemoContent() },
    DemoItem(label = "Container 布局容器", name = "container") { containerDemoContent(isDarkTheme) },
    DemoItem(label = "Menu 菜单", name = "menu") { menuDemoContent(isDarkTheme) },
    DemoItem(label = "Form 表单", name = "form") { formDemoContent() },
    DemoItem(label = "Card 卡片", name = "card") { cardDemoContent() },
    DemoItem(label = "Link 链接", name = "link") { linkDemoContent() },
    DemoItem(label = "Popconfirm 气泡确认框", name = "popconfirm") { popconfirmDemoContent() },
    DemoItem(label = "Tooltip 文字提示", name = "tooltip") { tooltipDemoContent() }
  )
  val demoByName = remember(demos) {
    demos.associateBy { it.name }
  }
  fun demoMenuItem(name: String): ZMenuItem {
    val demo = demoByName.getValue(name)
    return ZMenuItem(
      index = demo.name,
      title = demo.label,
      enabled = demo.enabled
    )
  }
  val menuItems = remember(demos) {
    listOf(
      ZSubMenu(
        index = "overview",
        titleContent = {
          Text(text = "Overview 总览", fontWeight = FontWeight.Bold)
        },
        children = listOf(
          demoMenuItem("zui-intro")
        )
      ),
      ZSubMenu(
        index = "basic",
        titleContent = {
          Text(text = "Basic 基础组件", fontWeight = FontWeight.Bold)
        },
        children = listOf(
          demoMenuItem("button"),
          demoMenuItem("container"),
          demoMenuItem("link"),
          demoMenuItem("text")
        )
      ),
      ZSubMenu(
        index = "form",
        titleContent = {
          Text(text = "Form 表单组件", fontWeight = FontWeight.Bold)
        },
        children = listOf(
          demoMenuItem("form"),
          demoMenuItem("checkbox"),
          demoMenuItem("textfield"),
          demoMenuItem("radio"),
          demoMenuItem("dropdown"),
          demoMenuItem("switch")
        )
      ),
      ZSubMenu(
        index = "data",
        titleContent = {
          Text(text = "Data 数据展示", fontWeight = FontWeight.Bold)
        },
        children = listOf(
          demoMenuItem("card")
        )
      ),
      ZSubMenu(
        index = "navigation",
        titleContent = {
          Text(text = "Navigation 导航", fontWeight = FontWeight.Bold)
        },
        children = listOf(
          demoMenuItem("menu")
        )
      ),
      ZSubMenu(
        index = "feedback",
        titleContent = {
          Text(text = "Feedback 反馈组件", fontWeight = FontWeight.Bold)
        },
        children = listOf(
          demoMenuItem("popconfirm"),
          demoMenuItem("tooltip")
        )
      )
    )
  }

  ZContainer(
    modifier = modifier.fillMaxSize()
  ) {
    ZAside(width = 240.dp) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          ZButton(onClick = onToggleTheme) {
            Text(if (isDarkTheme) "切换到日间模式" else "切换到夜间模式")
          }
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.TopStart
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(end = 6.dp)
                .verticalScroll(asideScrollState)
            ) {
              ZMenu(
                items = menuItems,
                mode = ZMenuMode.Vertical,
                activeIndex = activeTabName,
                defaultOpeneds = listOf("overview", "basic", "form", "data", "navigation", "feedback"),
                onSelect = { activeTabName = it },
                modifier = Modifier.fillMaxWidth()
              )
            }
            VerticalScrollbar(
              adapter = rememberScrollbarAdapter(asideScrollState),
              modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
          }
        }
      }
    }
    ZMain {
      val mainContentModifier = Modifier
        .fillMaxSize()
        .then(
          if (contentScrollState != null) {
            Modifier.verticalScroll(contentScrollState)
          } else {
            Modifier
          }
        )
        .padding(ZTabsDefaults.ContentPadding)
      Box(
        modifier = mainContentModifier,
        contentAlignment = Alignment.TopStart
      ) {
        demos.firstOrNull { it.name == activeTabName }?.content?.invoke()
      }
    }
  }
}

@Composable
private fun zuiIntroWindowContent() {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    ZText("ZUI Compose Desktop", fontWeight = FontWeight.Bold)
    ZText("用于 Compose Desktop 的组件库，提供统一风格与常用交互能力。")

    ZCard(
      shadow = ZCardShadow.NEVER,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ZText("项目仓库")
        ZLink(text = ZUI_COMPOSE_DESKTOP_REPO, url = ZUI_COMPOSE_DESKTOP_REPO)
      }
    }

    ZText("当前示例覆盖：Text / Button / Form / Menu / Card / Link 等组件。")
    ZText("你可以通过左侧菜单切换组件示例，快速验证样式与交互行为。")
  }
}
