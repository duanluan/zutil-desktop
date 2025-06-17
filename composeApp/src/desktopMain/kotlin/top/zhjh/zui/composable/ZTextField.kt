package top.zhjh.zui.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ZTextField(
  value: String,
  onValueChange: (String) -> Unit,
  // 行高，默认 20.dp
  lineHeight: Int = 20,
  // 字体大小，默认 14.sp
  fontSize: TextUnit = 12.sp,
  modifier: Modifier = Modifier,
  // 是否只读
  readOnly: Boolean = false,
  // 是否单行
  singleLine: Boolean = true,
  // 最大行数
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  // 最小行数
  minLines: Int = 1,
  // 左侧图标
  leadingIcon: (@Composable () -> Unit)? = null,
  // 右侧图标
  trailingIcon: (@Composable () -> Unit)? = null,
  // 占位符
  placeholder: String? = null,
) {
  // 共享的文字样式，确保一致性
  val textStyle = LocalTextStyle.current.copy(
    fontSize = fontSize,
    lineHeight = lineHeight.sp
  )

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier
      // 默认全宽
      .fillMaxWidth()
      // 边框
      .border(width = 1.dp, color = MaterialTheme.colors.primary),
    readOnly = readOnly,
    maxLines = maxLines,
    minLines = minLines,
    textStyle = textStyle,
    decorationBox = { innerTextField ->
      Row(
        // 单行时垂直居中，多行时顶部对齐
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        modifier = Modifier
          // 如果没有 leadingIcon，则添加左边距
          .then(if (leadingIcon == null) Modifier.padding(start = 4.dp) else Modifier)
      ) {
        // 左侧图标
        if (leadingIcon != null) {
          Box(
            modifier = Modifier
              // 图标高度
              .height(lineHeight.dp)
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
            Text(placeholder, style = textStyle)
          }
        }
        // 右侧图标
        if (trailingIcon != null) {
          Box(
            modifier = Modifier
              // 图标高度
              .height(lineHeight.dp)
          ) {
            trailingIcon()
          }
        }
      }
    }
  )
}

