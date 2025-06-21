package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.zhjh.zui.theme.isAppInDarkTheme

/**
 * 输入框
 *
 * @param value 值
 * @param onValueChange 值变化事件
 * @param modifier 修饰符
 * @param enabled 是否可用，默认 true
 * @param readOnly 是否只读，默认 false
 * @param singleLine 是否单行，默认 true
 * @param maxLines 最大行数，单行时为 1，否则为 [Int.MAX_VALUE]
 * @param minLines 最小行数，默认 1
 * @param leadingIcon 左侧图标
 * @param trailingIcon 右侧图标
 * @param placeholder 占位符文本，如果值为空时显示
 */
@Composable
fun ZTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = ZTextFieldDefaults.FontSize),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  placeholder: String? = null,
) {
  // 圆角半径
  val shape = ZTextFieldDefaults.Shape
  // 判断是否为暗黑模式
  val isDarkTheme = isAppInDarkTheme()
  // 交互源，跟踪组件的交互状态
  val interactionSource = remember { MutableInteractionSource() }
  // 是否悬停
  val isHovered by interactionSource.collectIsHoveredAsState()
  // 是否聚焦
  val isFocused: State<Boolean> = interactionSource.collectIsFocusedAsState()

  // 获取样式
  val textFieldStyle = getZTextFieldStyle(isDarkTheme, isHovered, isFocused, enabled)
  // 应用字体颜色
  val finalTextStyle = textStyle.copy(color = textFieldStyle.textColor)

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    interactionSource = interactionSource,
    modifier = Modifier
      // 悬停检测
      .hoverable(interactionSource)
      .clip(shape)
      .background(color = textFieldStyle.backgroundColor)
      // 边框，聚焦时修改边框颜色
      .border(width = 1.dp, color = textFieldStyle.borderColor, shape = shape)
      // 默认全宽
      .fillMaxWidth()
      .defaultMinSize(minHeight = ZTextFieldDefaults.MinHeight)
      .then(modifier),
    enabled = enabled,
    readOnly = readOnly,
    textStyle = finalTextStyle,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    maxLines = maxLines,
    minLines = minLines,
    // 光标
    cursorBrush = textFieldStyle.cursorColor?.let { SolidColor(it) } ?: SolidColor(LocalContentColor.current),  // 当 cursorColor 为空时使用默认颜色
    decorationBox = { innerTextField ->
      Row(
        // 内容垂直居中
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(ZTextFieldDefaults.ContentPadding)
      ) {
        // 提供内容颜色的上下文，使所有子组件继承此颜色
        CompositionLocalProvider(LocalContentColor provides textFieldStyle.textColor) {
          // 左侧图标
          if (leadingIcon != null) {
            Box(
              modifier = Modifier
                // 图标右边距
                .padding(end = ZTextFieldDefaults.IconSpacing)
                // 图标大小
                .size(ZTextFieldDefaults.IconSize)

            ) {
              leadingIcon()
            }
          }
          // 输入内容
          Box(modifier = Modifier.weight(1f)) {
            // 输入内容
            innerTextField()
            // 输入为空时显示占位符
            if (value.isEmpty() && placeholder != null) {
              Text(placeholder)
            }
          }
          // 右侧图标
          if (trailingIcon != null) {
            Box(
              modifier = Modifier
                // 图标左边距
                .padding(start = ZTextFieldDefaults.IconSpacing)
                // 图标大小
                .size(ZTextFieldDefaults.IconSize)
            ) {
              trailingIcon()
            }
          }
        }
      }
    }
  )
}

/**
 * ZTextField 样式类
 */
private data class ZTextFieldStyle(
  var backgroundColor: Color,
  var borderColor: Color,
  var textColor: Color,
  var cursorColor: Color? = null
)

/**
 * 获取输入框样式
 * @param isDarkTheme 是否暗黑模式
 * @param isHovered 是否悬停
 * @param isFocused 是否聚焦状态
 *
 */
private fun getZTextFieldStyle(isDarkTheme: Boolean, isHovered: Boolean, isFocused: State<Boolean>, enabled: Boolean): ZTextFieldStyle {
  // 禁用时
  if (!enabled) {
    return ZTextFieldStyle(
      backgroundColor = if (isDarkTheme) Color(0xff262727) else Color(0xfff5f7fa),
      borderColor = if (isDarkTheme) Color(0xff414243) else Color(0xffe4e7ed),
      textColor = if (isDarkTheme) Color(0xff8d9095) else Color(0xffa8abb2),
    )
  }
  // 聚焦时
  if (isFocused.value) {
    return ZTextFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = if (isDarkTheme) Color(0xff409eff) else Color(0xff409eff),
      textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
      cursorColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266)
    )
  }
  // 悬停时
  if (isHovered) {
    return ZTextFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = if (isDarkTheme) Color(0xff6c6e72) else Color(0xffc0c4cc),
      textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266)
    )
  }
  // 默认
  return ZTextFieldStyle(
    backgroundColor = Color.Transparent,
    borderColor = if (isDarkTheme) Color(0xff4c4d4f) else Color(0xffdcdfe6),
    textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266)
  )
}

/**
 * ZTextField 默认值，参考 [androidx.compose.material.TextFieldDefaults]
 */
@Immutable
object ZTextFieldDefaults {
  private val TextFieldHorizontalPadding = 8.dp

  /**
   * 内边距
   */
  val ContentPadding = PaddingValues(
    start = TextFieldHorizontalPadding,
    end = TextFieldHorizontalPadding,
    top = 0.dp,
    bottom = 0.dp
  )

  /**
   * 最小高度
   */
  val MinHeight = 30.dp

  /**
   * 字体大小
   */
  val FontSize = 14.sp

  /**
   * 图标大小
   */
  val IconSize = 14.dp

  /**
   * 图标与文字间距
   */
  val IconSpacing = 6.dp

  /**
   * 圆角形状
   */
  val Shape = RoundedCornerShape(4.dp)
}
