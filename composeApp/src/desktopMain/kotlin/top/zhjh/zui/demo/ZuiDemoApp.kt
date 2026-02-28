package top.zhjh.zui.demo

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.zhjh.common.composable.ToastContainer
import top.zhjh.zui.composable.*
import top.zhjh.zui.demo.components.*
import top.zhjh.zui.theme.ZTheme

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
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(scrollState)
          )
          VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
          )
          ToastContainer()
        }
      } else {
        Box(modifier = Modifier.fillMaxWidth()) {
          ZuiComponentDemoContent(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
            modifier = Modifier.fillMaxWidth()
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
  modifier: Modifier = Modifier
) {
  var activeTabName by remember { mutableStateOf("text") }

  data class DemoItem(
    val label: String,
    val name: String,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit
  )

  val demos = listOf(
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
    DemoItem(label = "Link 链接", name = "link") { linkDemoContent() }
  )
  val menuItems = remember(demos) {
    demos.map { demo ->
      ZMenuItem(
        index = demo.name,
        title = demo.label,
        enabled = demo.enabled
      )
    }
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    ZButton(onClick = onToggleTheme) {
      Text(if (isDarkTheme) "切换到日间模式" else "切换到夜间模式")
    }

    ZContainer(
      modifier = Modifier.fillMaxWidth()
    ) {
      ZAside(width = 240.dp) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopStart
        ) {
          ZMenu(
            items = menuItems,
            mode = ZMenuMode.Vertical,
            activeIndex = activeTabName,
            onSelect = { activeTabName = it },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
      ZMain {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(ZTabsDefaults.ContentPadding),
          contentAlignment = Alignment.TopStart
        ) {
          demos.firstOrNull { it.name == activeTabName }?.content?.invoke()
        }
      }
    }
  }
}
