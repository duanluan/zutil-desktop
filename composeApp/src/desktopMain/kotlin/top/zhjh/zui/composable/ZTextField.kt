package top.zhjh.zui.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
  // 是否聚焦
  var isFocused by remember { mutableStateOf(false) }

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier
      .clip(shape)
      .onFocusChanged { focusState ->
        isFocused = focusState.isFocused
      }
      // 边框，聚焦时修改边框颜色
      .border(width = 1.dp, color = if (isFocused) Color(0xff409eff) else Color(0xffdcdfe6), shape = shape)
      // 默认全宽
      .fillMaxWidth()
      .defaultMinSize(minHeight = ZTextFieldDefaults.MinHeight),
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    maxLines = maxLines,
    minLines = minLines,
    decorationBox = { innerTextField ->
      Row(
        // 内容垂直居中
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(ZTextFieldDefaults.ContentPadding)
      ) {
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
            Text(placeholder, style = textStyle.copy(color = Color(0xffb4b7bd)))
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
