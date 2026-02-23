package top.zhjh

import ZLink
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import top.zhjh.common.composable.ToastContainer
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.ZTheme

@Composable
@Preview
fun TestApp() {
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
  var textFieldDefault by remember { mutableStateOf("") }
  var textFieldDisabled by remember { mutableStateOf("") }
  var textFieldIcon by remember { mutableStateOf("") }
  var textFieldPassword by remember { mutableStateOf("") }
  var textFieldTextarea by remember { mutableStateOf("") }
  var textFieldTextareaFixed by remember { mutableStateOf("") }

  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var radioStringValue by remember { mutableStateOf("option1") }
  var radioNumberValue by remember { mutableStateOf(1) }
  var radioBooleanValue by remember { mutableStateOf(true) }
  var checkboxLargeSelectedOptions by remember { mutableStateOf(setOf("Option 1")) }
  var checkboxDefaultSelectedOptions by remember { mutableStateOf(emptySet<String>()) }
  var checkboxSmallSelectedOptions by remember { mutableStateOf(emptySet<String>()) }
  var buttonGroupDirectionValue by remember { mutableStateOf("horizontal") }
  var dropdownClearableValue by remember { mutableStateOf("Option1") }
  var dropdownMultiDefaultValue by remember { mutableStateOf(listOf("Option1", "Option2")) }
  var dropdownMultiCollapseValue by remember { mutableStateOf(listOf("Option1", "Option2", "Option3")) }
  var dropdownMultiCollapseTooltipValue by remember { mutableStateOf(listOf("Option1", "Option3", "Option4", "Option5")) }
  var dropdownMultiMaxCollapseValue by remember { mutableStateOf(listOf("Option1", "Option2", "Option3", "Option4")) }

  val formState = rememberZFormState()
  val formModel = remember(username, email, password, confirmPassword) {
    mapOf(
      "username" to username,
      "email" to email,
      "password" to password,
      "confirmPassword" to confirmPassword
    )
  }
  val formRules = remember {
    mapOf(
      "username" to listOf(
        ZFormRule(required = true, message = "请输入用户名"),
        ZFormRule(minLength = 3, maxLength = 20, message = "用户名长度需在 3-20 之间")
      ),
      "email" to listOf(
        ZFormRule(required = true, message = "请输入邮箱"),
        ZFormRule(
          pattern = Regex(
            """^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*${'$'}"""
          ),
          message = "邮箱格式不正确"
        )
      ),
      "password" to listOf(
        ZFormRule(required = true, message = "请输入密码"),
        ZFormRule(minLength = 6, message = "密码至少 6 位")
      ),
      "confirmPassword" to listOf(
        ZFormRule(required = true, message = "请再次输入密码"),
        ZFormRule(
          validator = { value, model ->
            val current = (value as? String).orEmpty()
            val target = (model["password"] as? String).orEmpty()
            if (current == target) null else "两次密码不一致"
          }
        )
      )
    )
  }

  var activeTabName by remember { mutableStateOf("text") }

  val tabs = listOf(
    ZTabPane(label = "Text 文本", name = "text") {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZText("Default")
          ZText("Primary", type = ZColorType.PRIMARY)
          ZText("Success", type = ZColorType.SUCCESS)
          ZText("Info", type = ZColorType.INFO)
          ZText("Warning", type = ZColorType.WARNING)
          ZText("Danger", type = ZColorType.DANGER)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZText("Large", size = ZTextSize.Large)
          ZText("Default", size = ZTextSize.Default)
          ZText("Small", size = ZTextSize.Small)
        }

        ZText("这是一段 paragraph 文本示例（tag = p）。", tag = "p")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZText("Bold", tag = "b")
          ZText("Italic", tag = "i")
          ZText("Inserted", tag = "ins")
          ZText("Deleted", tag = "del")
          ZText("Marked", tag = "mark")
        }

        Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
          ZText("H", modifier = Modifier.alignByBaseline())
          ZText("2", tag = "sub", modifier = Modifier.alignByBaseline())
          ZText("O", modifier = Modifier.alignByBaseline())
          Spacer(modifier = Modifier.width(12.dp))
          ZText("X", modifier = Modifier.alignByBaseline())
          ZText("2", tag = "sup", modifier = Modifier.alignByBaseline())
        }

        ZText(
          text = "truncated 示例：这是一段超长文本，当容器宽度不足时会显示省略号而不是换行。",
          truncated = true,
          modifier = Modifier
            .width(350.dp)
            // .border(1.dp, MaterialTheme.colors.primary)
            // .padding(4.dp)
        )

        ZText(
          text = "lineClamp 示例：这是一段用于演示多行省略的文本内容。设置 lineClamp 为 2 后，超过两行的部分会被截断，并在末尾显示省略号。",
          lineClamp = 2,
          modifier = Modifier
            .width(350.dp)
            // .border(1.dp, MaterialTheme.colors.primary)
            // .padding(4.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          ZText("H1", style = MaterialTheme.typography.h1, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
          ZText("H2", style = MaterialTheme.typography.h2, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
          ZText("H3", style = MaterialTheme.typography.h3, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
          ZText("H4", style = MaterialTheme.typography.h4, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
          ZText("H5", style = MaterialTheme.typography.h5, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
          ZText("H6", style = MaterialTheme.typography.h6, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
        }
      }
    },
    ZTabPane(label = "Radio 单选框", name = "radio") {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ZText("Radio String")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZRadio(
            value = "option1",
            selectedValue = radioStringValue,
            onValueChange = { radioStringValue = it },
            label = "Option 1"
          )
          ZRadio(
            value = "option2",
            selectedValue = radioStringValue,
            onValueChange = { radioStringValue = it },
            label = "Option 2"
          )
          ZRadio(
            value = "option3",
            selectedValue = radioStringValue,
            onValueChange = { radioStringValue = it },
            label = "Option 3"
          )
        }

        ZText("Radio Number")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZRadio(
            value = 1,
            selectedValue = radioNumberValue,
            onValueChange = { radioNumberValue = it },
            label = "1"
          )
          ZRadio(
            value = 2,
            selectedValue = radioNumberValue,
            onValueChange = { radioNumberValue = it },
            label = "2"
          )
          ZRadio(
            value = 3,
            selectedValue = radioNumberValue,
            onValueChange = { radioNumberValue = it },
            label = "3"
          )
        }

        ZText("Radio Boolean")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZRadio(
            value = true,
            selectedValue = radioBooleanValue,
            onValueChange = { radioBooleanValue = it },
            label = "True"
          )
          ZRadio(
            value = false,
            selectedValue = radioBooleanValue,
            onValueChange = { radioBooleanValue = it },
            label = "False"
          )
        }

        ZText("Radio Size")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          ZRadio(
            value = "large",
            selectedValue = "large",
            onValueChange = {},
            label = "Large",
            size = ZRadioSize.Large
          )
          ZRadio(
            value = "default",
            selectedValue = "default",
            onValueChange = {},
            label = "Default",
            size = ZRadioSize.Default
          )
          ZRadio(
            value = "small",
            selectedValue = "small",
            onValueChange = {},
            label = "Small",
            size = ZRadioSize.Small
          )
        }
      }
    },
    ZTabPane(label = "Checkbox 多选框", name = "checkbox") {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
          ZCheckbox(
            checked = "Option 1" in checkboxLargeSelectedOptions,
            onCheckedChange = { checked ->
              checkboxLargeSelectedOptions = if (checked) {
                checkboxLargeSelectedOptions + "Option 1"
              } else {
                checkboxLargeSelectedOptions - "Option 1"
              }
            },
            label = "Option 1",
            size = ZCheckboxSize.Large
          )
          ZCheckbox(
            checked = "Option 2" in checkboxLargeSelectedOptions,
            onCheckedChange = { checked ->
              checkboxLargeSelectedOptions = if (checked) {
                checkboxLargeSelectedOptions + "Option 2"
              } else {
                checkboxLargeSelectedOptions - "Option 2"
              }
            },
            label = "Option 2",
            size = ZCheckboxSize.Large
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
          ZCheckbox(
            checked = "Option 1" in checkboxDefaultSelectedOptions,
            onCheckedChange = { checked ->
              checkboxDefaultSelectedOptions = if (checked) {
                checkboxDefaultSelectedOptions + "Option 1"
              } else {
                checkboxDefaultSelectedOptions - "Option 1"
              }
            },
            label = "Option 1",
            size = ZCheckboxSize.Default
          )
          ZCheckbox(
            checked = "Option 2" in checkboxDefaultSelectedOptions,
            onCheckedChange = { checked ->
              checkboxDefaultSelectedOptions = if (checked) {
                checkboxDefaultSelectedOptions + "Option 2"
              } else {
                checkboxDefaultSelectedOptions - "Option 2"
              }
            },
            label = "Option 2",
            size = ZCheckboxSize.Default
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
          ZCheckbox(
            checked = "Option 1" in checkboxSmallSelectedOptions,
            onCheckedChange = { checked ->
              checkboxSmallSelectedOptions = if (checked) {
                checkboxSmallSelectedOptions + "Option 1"
              } else {
                checkboxSmallSelectedOptions - "Option 1"
              }
            },
            label = "Option 1",
            size = ZCheckboxSize.Small
          )
          ZCheckbox(
            checked = "Option 2" in checkboxSmallSelectedOptions,
            onCheckedChange = { checked ->
              checkboxSmallSelectedOptions = if (checked) {
                checkboxSmallSelectedOptions + "Option 2"
              } else {
                checkboxSmallSelectedOptions - "Option 2"
              }
            },
            label = "Option 2",
            size = ZCheckboxSize.Small
          )
        }
      }
    },
    ZTabPane(label = "Button 按钮", name = "button") {
      Column {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(onClick = {}) {}
          ZButton(onClick = { ToastManager.success("Default") }) { Text("Default") }
          ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(plain = true, onClick = { ToastManager.success("Plain") }) { Text("Plain") }
          ZButton(type = ZColorType.PRIMARY, plain = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, plain = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, plain = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, plain = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, plain = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(round = true, onClick = { ToastManager.success("Round") }) { Text("Round") }
          ZButton(type = ZColorType.PRIMARY, round = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, round = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, round = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, round = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, round = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(circle = true, icon = { Icon(FeatherIcons.Search, contentDescription = null) }, onClick = { ToastManager.success("Circle") }) {}
          ZButton(type = ZColorType.PRIMARY, circle = true, icon = { Icon(FeatherIcons.Search, contentDescription = null) }, onClick = { ToastManager.success("Primary") }) {}
          ZButton(type = ZColorType.SUCCESS, circle = true, icon = { Icon(FeatherIcons.Edit, contentDescription = null) }, onClick = { ToastManager.success("Success") }) {}
          ZButton(type = ZColorType.INFO, circle = true, icon = { Icon(FeatherIcons.Check, contentDescription = null) }, onClick = { ToastManager.success("Info") }) {}
          ZButton(type = ZColorType.WARNING, circle = true, icon = { Icon(FeatherIcons.Mail, contentDescription = null) }, onClick = { ToastManager.success("Warning") }) {}
          ZButton(type = ZColorType.DANGER, circle = true, icon = { Icon(FeatherIcons.Trash2, contentDescription = null) }, onClick = { ToastManager.success("Danger") }) {}
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(enabled = false, onClick = { ToastManager.success("Default") }) { Text("Default") }
          ZButton(type = ZColorType.PRIMARY, enabled = false, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, enabled = false, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, enabled = false, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, enabled = false, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, enabled = false, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(enabled = false, plain = true, onClick = { ToastManager.success("Plain") }) { Text("Plain") }
          ZButton(type = ZColorType.PRIMARY, enabled = false, plain = true, onClick = { ToastManager.success("Primary") }) { Text("Primary") }
          ZButton(type = ZColorType.SUCCESS, enabled = false, plain = true, onClick = { ToastManager.success("Success") }) { Text("Success") }
          ZButton(type = ZColorType.INFO, enabled = false, plain = true, onClick = { ToastManager.success("Info") }) { Text("Info") }
          ZButton(type = ZColorType.WARNING, enabled = false, plain = true, onClick = { ToastManager.success("Warning") }) { Text("Warning") }
          ZButton(type = ZColorType.DANGER, enabled = false, plain = true, onClick = { ToastManager.success("Danger") }) { Text("Danger") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)) {
          ZButtonGroup {
            ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Previous Page") }) {
              Icon(FeatherIcons.ChevronLeft, contentDescription = null)
              Spacer(modifier = Modifier.width(1.dp))
              Text("Previous Page")
            }
            ZButton(type = ZColorType.PRIMARY, onClick = { ToastManager.success("Next Page") }) {
              Text("Next Page")
              Spacer(modifier = Modifier.width(1.dp))
              Icon(FeatherIcons.ChevronRight, contentDescription = null)
            }
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(28.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        ) {
          ZRadio(
            value = "horizontal",
            selectedValue = buttonGroupDirectionValue,
            onValueChange = { buttonGroupDirectionValue = it },
            label = "Horizontal"
          )
          ZRadio(
            value = "vertical",
            selectedValue = buttonGroupDirectionValue,
            onValueChange = { buttonGroupDirectionValue = it },
            label = "Vertical"
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 4.dp, bottom = 5.dp)) {
          ZButtonGroup(
            direction = if (buttonGroupDirectionValue == "horizontal") {
              ZButtonGroupDirection.Horizontal
            } else {
              ZButtonGroupDirection.Vertical
            }
          ) {
            ZButton(
              type = ZColorType.PRIMARY,
              icon = { Icon(FeatherIcons.Home, contentDescription = null) },
              modifier = Modifier.width(45.dp),
              onClick = { ToastManager.success("Home") }
            ) {}
            ZButton(
              type = ZColorType.PRIMARY,
              icon = { Icon(FeatherIcons.Sliders, contentDescription = null) },
              modifier = Modifier.width(45.dp),
              onClick = { ToastManager.success("Sliders") }
            ) {}
            ZButton(
              type = ZColorType.PRIMARY,
              icon = { Icon(FeatherIcons.Copy, contentDescription = null) },
              modifier = Modifier.width(45.dp),
              onClick = { ToastManager.success("Copy") }
            ) {}
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)) {
          ZButton(type = ZColorType.PRIMARY, loading = true, loadingIconSpacing = 5.dp, onClick = { ToastManager.success("Loading") }) {
            Text("Loading")
          }
          ZButton(
            type = ZColorType.PRIMARY,
            loading = true,
            loadingIcon = { Icon(FeatherIcons.Tool, contentDescription = null) },
            loadingIconSpacing = 5.dp,
            onClick = { ToastManager.success("Loading") }
          ) {
            Text("Loading")
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)) {
          ZButton(size = ZButtonSize.Large, onClick = { ToastManager.success("Large") }) { Text("Large") }
          ZButton(size = ZButtonSize.Default, onClick = { ToastManager.success("Default") }) { Text("Default") }
          ZButton(size = ZButtonSize.Small, onClick = { ToastManager.success("Small") }) { Text("Small") }
          ZButton(
            size = ZButtonSize.Large,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Search") }
          ) { Text("Search") }
          ZButton(
            size = ZButtonSize.Default,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Search") }
          ) { Text("Search") }
          ZButton(
            size = ZButtonSize.Small,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Search") }
          ) { Text("Search") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(size = ZButtonSize.Large, round = true, onClick = { ToastManager.success("Large Round") }) { Text("Large") }
          ZButton(size = ZButtonSize.Default, round = true, onClick = { ToastManager.success("Default Round") }) { Text("Default") }
          ZButton(size = ZButtonSize.Small, round = true, onClick = { ToastManager.success("Small Round") }) { Text("Small") }
          ZButton(
            size = ZButtonSize.Large,
            round = true,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Search Round") }
          ) { Text("Search") }
          ZButton(
            size = ZButtonSize.Default,
            round = true,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Search Round") }
          ) { Text("Search") }
          ZButton(
            size = ZButtonSize.Small,
            round = true,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Search Round") }
          ) { Text("Search") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZButton(
            size = ZButtonSize.Large,
            circle = true,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Large Circle") }
          ) {}
          ZButton(
            size = ZButtonSize.Default,
            circle = true,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Default Circle") }
          ) {}
          ZButton(
            size = ZButtonSize.Small,
            circle = true,
            icon = { Icon(FeatherIcons.Search, contentDescription = null) },
            onClick = { ToastManager.success("Small Circle") }
          ) {}
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)) {
          ZButton(
            type = ZColorType.PRIMARY,
            href = "https://github.com/duanluan/zutil-desktop",
            icon = { Icon(FeatherIcons.Link, contentDescription = null) }
          ) {
            Text("zutil-desktop")
          }
        }
      }
    },
    ZTabPane(label = "TextField 输入框", name = "textfield") {
      Column {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(value = textFieldDefault, onValueChange = { textFieldDefault = it }, placeholder = "Please input")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(value = textFieldDisabled, onValueChange = { textFieldDisabled = it }, enabled = false, placeholder = "Please input")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(
            value = textFieldIcon,
            onValueChange = { textFieldIcon = it },
            placeholder = "Please input",
            leadingIcon = { Icon(FeatherIcons.Search, contentDescription = null) },
            trailingIcon = { Icon(FeatherIcons.ChevronDown, contentDescription = null) },
            singleLine = false
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(
            value = textFieldPassword,
            onValueChange = { textFieldPassword = it },
            type = ZTextFieldType.PASSWORD,
            placeholder = "Please input password",
            showPassword = true
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(
            value = textFieldTextarea,
            onValueChange = { textFieldTextarea = it },
            placeholder = "Please input",
            type = ZTextFieldType.TEXTAREA,
            leadingIcon = { Icon(FeatherIcons.Search, contentDescription = null) },
            trailingIcon = { Icon(FeatherIcons.ChevronDown, contentDescription = null) }
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
          ZTextField(
            value = textFieldTextareaFixed,
            onValueChange = { textFieldTextareaFixed = it },
            placeholder = "Please input",
            type = ZTextFieldType.TEXTAREA,
            resize = false,
            minLines = 3,
            maxLines = 3
          )
        }
      }
    },
    ZTabPane(label = "DropdownMenu 选择器", name = "dropdown") {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
      ) {
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          modifier = Modifier.width(220.dp),
          disabledOptions = setOf("Option2")
        )
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          enabled = false,
          modifier = Modifier.width(220.dp)
        )
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          modifier = Modifier.width(220.dp),
          clearable = true,
          value = dropdownClearableValue,
          onOptionSelected = { dropdownClearableValue = it }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          ZDropdownMenu(
            options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
            placeholder = "Select",
            size = ZDropdownMenuSize.Large,
            modifier = Modifier.width(160.dp)
          )
          ZDropdownMenu(
            options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
            placeholder = "Select",
            size = ZDropdownMenuSize.Default,
            modifier = Modifier.width(160.dp)
          )
          ZDropdownMenu(
            options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
            placeholder = "Select",
            size = ZDropdownMenuSize.Small,
            modifier = Modifier.width(160.dp)
          )
        }

        ZText("default")
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          modifier = Modifier.width(360.dp),
          multiple = true,
          values = dropdownMultiDefaultValue,
          onOptionsSelected = { dropdownMultiDefaultValue = it }
        )

        ZText("use collapse-tags")
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          modifier = Modifier.width(360.dp),
          multiple = true,
          collapseTags = true,
          values = dropdownMultiCollapseValue,
          onOptionsSelected = { dropdownMultiCollapseValue = it }
        )

        ZText("use collapse-tags-tooltip")
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          modifier = Modifier.width(360.dp),
          multiple = true,
          collapseTags = true,
          collapseTagsTooltip = true,
          values = dropdownMultiCollapseTooltipValue,
          onOptionsSelected = { dropdownMultiCollapseTooltipValue = it }
        )

        ZText("use max-collapse-tags")
        ZDropdownMenu(
          options = listOf("Option1", "Option2", "Option3", "Option4", "Option5"),
          placeholder = "Select",
          modifier = Modifier.width(360.dp),
          multiple = true,
          maxCollapseTags = 3,
          values = dropdownMultiMaxCollapseValue,
          onOptionsSelected = { dropdownMultiMaxCollapseValue = it }
        )
      }
    },
    ZTabPane(label = "Form 表单", name = "form") {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        ZText("ZForm 验证示例")
        ZForm(
          state = formState,
          model = formModel,
          rules = formRules,
          labelWidth = 90.dp,
          statusIcon = true,
          modifier = Modifier.fillMaxWidth()
        ) {
          ZFormItem(label = "用户名", prop = "username", value = username) {
            ZTextField(
              value = username,
              onValueChange = { username = it },
              placeholder = "请输入用户名"
            )
          }
          ZFormItem(label = "邮箱", prop = "email", value = email) {
            ZTextField(
              value = email,
              onValueChange = { email = it },
              placeholder = "请输入邮箱"
            )
          }
          ZFormItem(label = "密码", prop = "password", value = password) {
            ZTextField(
              value = password,
              onValueChange = { password = it },
              type = ZTextFieldType.PASSWORD,
              showPassword = true,
              placeholder = "请输入密码"
            )
          }
          ZFormItem(label = "确认密码", prop = "confirmPassword", value = confirmPassword) {
            ZTextField(
              value = confirmPassword,
              onValueChange = { confirmPassword = it },
              type = ZTextFieldType.PASSWORD,
              showPassword = true,
              placeholder = "请再次输入密码"
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          ZButton(type = ZColorType.PRIMARY, onClick = {
            if (formState.validate()) {
              ToastManager.success("表单校验通过")
            } else {
              ToastManager.error("表单校验失败")
            }
          }) {
            Text("提交校验")
          }
          ZButton(onClick = {
            formState.clearValidate()
            ToastManager.success("已清除校验状态")
          }) {
            Text("清除校验")
          }
          ZButton(onClick = {
            username = ""
            email = ""
            password = ""
            confirmPassword = ""
            formState.clearValidate()
            ToastManager.success("已重置表单")
          }) {
            Text("重置表单")
          }
        }
      }
    },
    ZTabPane(label = "Card 卡片", name = "card") {
      Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
        ZCard(modifier = Modifier.weight(1f)) {
          ZText("Always")
        }
        ZCard(shadow = ZCardShadow.HOVER, modifier = Modifier.weight(1f)) {
          ZText("Hover")
        }
        ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.weight(1f)) {
          ZText("Never")
        }
      }
    },
    ZTabPane(label = "Link 链接", name = "link") {
      Row(verticalAlignment = Alignment.CenterVertically) {
        ZLink("人民网", "http://www.people.com.cn/")
        ZText("_网上的人民日报")
      }
    }
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    ZButton(onClick = onToggleTheme) {
      Text(if (isDarkTheme) "切换到日间模式" else "切换到夜间模式")
    }

    ZTabs(
      tabs = tabs,
      activeName = activeTabName,
      contentPadding = ZTabsDefaults.ContentPadding,
      type = ZTabsType.LINE,
      onActiveNameChange = { activeTabName = it },
      modifier = Modifier.fillMaxWidth()
    )
  }
}
