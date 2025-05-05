package top.zhjh.zutil.common.composable


import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MyTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions(),
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  interactionSource: MutableInteractionSource? = null,
  shape: Shape = TextFieldDefaults.TextFieldShape,
  colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
  // 修改左右内边距
  contentPadding: PaddingValues = PaddingValues(0.dp),
  // leadingIcon 边距
  leadingIconPadding: PaddingValues = PaddingValues(start = 0.dp),
  // trailingIcon 边距
  trailingIconPadding: PaddingValues = PaddingValues(end = 0.dp),
  // 图标大小
  iconSize: Dp = 24.dp
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  // If color is not provided via the text style, use content color as a default
  val textColor = textStyle.color.takeOrElse {
    colors.textColor(enabled).value
  }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

  val hasLeadingIcon = leadingIcon != null
  val hasTrailingIcon = trailingIcon != null
  // 有左图标时，文本左内边距+=图标大小+图标边距
  val textPaddingStart = if (hasLeadingIcon) {
    contentPadding.calculateStartPadding(LocalLayoutDirection.current) + iconSize + leadingIconPadding.calculateStartPadding(LocalLayoutDirection.current)
  } else {
    contentPadding.calculateStartPadding(LocalLayoutDirection.current)
  }
  // 有右图标时，文本右内边距+=图标大小+图标边距
  val textPaddingEnd = if (hasTrailingIcon) {
    contentPadding.calculateEndPadding(LocalLayoutDirection.current) + iconSize + trailingIconPadding.calculateEndPadding(LocalLayoutDirection.current)
  } else {
    contentPadding.calculateEndPadding(LocalLayoutDirection.current)
  }

  Box(modifier = modifier.height(24.dp)) {
    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
      value = value,
      modifier = Modifier
        // 撑满父级高度
        .fillMaxHeight()
        .indicatorLine(enabled, isError, interactionSource, colors)
        // .defaultErrorSemantics(isError, getString(Strings.DefaultErrorMessage))
        .semantics { if (isError) error("Invalid input") }
        .defaultMinSize(
          minWidth = TextFieldDefaults.MinWidth,
          // minHeight = TextFieldDefaults.MinHeight
        ),
      onValueChange = onValueChange,
      enabled = enabled,
      readOnly = readOnly,
      textStyle = mergedTextStyle,
      cursorBrush = SolidColor(colors.cursorColor(isError).value),
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      interactionSource = interactionSource,
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      decorationBox = @Composable { innerTextField ->
        // places leading icon, text field with label and placeholder, trailing icon
        TextFieldDefaults.TextFieldDecorationBox(
          value = value,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          placeholder = placeholder,
          label = label,
          // 不使用内置的 leadingIcon
          leadingIcon = null,
          // 不使用内置的 trailingIcon
          trailingIcon = null,
          singleLine = singleLine,
          enabled = enabled,
          isError = isError,
          interactionSource = interactionSource,
          shape = shape,
          colors = colors,
          // 修改内边距
          contentPadding = PaddingValues(
            start = textPaddingStart,
            end = textPaddingEnd,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
          )
        )
      }
    )

    // 放置 leadingIcon
    if (hasLeadingIcon) {
      Box(
        modifier = Modifier
          .padding(leadingIconPadding)
          .size(iconSize)
          .align(Alignment.CenterStart)
      ) {
        leadingIcon!!()
      }
    }
    // 放置 trailingIcon
    if (hasTrailingIcon) {
      Box(
        modifier = Modifier
          .padding(trailingIconPadding)
          .size(iconSize)
          .align(Alignment.CenterEnd)
      ) {
        trailingIcon!!()
      }
    }
  }
}

@Composable
fun MyTextField(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions(),
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  interactionSource: MutableInteractionSource? = null,
  shape: Shape = TextFieldDefaults.TextFieldShape,
  colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
  // 修改左右内边距
  contentPadding: PaddingValues = PaddingValues(0.dp),
  // leadingIcon 边距
  leadingIconPadding: PaddingValues = PaddingValues(start = 0.dp),
  // trailingIcon 边距
  trailingIconPadding: PaddingValues = PaddingValues(end = 0.dp),
  // 图标大小
  iconSize: Dp = 24.dp
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  // If color is not provided via the text style, use content color as a default
  val textColor = textStyle.color.takeOrElse {
    colors.textColor(enabled).value
  }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

  val hasLeadingIcon = leadingIcon != null
  val hasTrailingIcon = trailingIcon != null
  // 有左图标时，文本左内边距+=图标大小+图标边距
  val textPaddingStart = if (hasLeadingIcon) {
    contentPadding.calculateStartPadding(LocalLayoutDirection.current) + iconSize + leadingIconPadding.calculateStartPadding(LocalLayoutDirection.current)
  } else {
    contentPadding.calculateStartPadding(LocalLayoutDirection.current)
  }
  // 有右图标时，文本右内边距+=图标大小+图标边距
  val textPaddingEnd = if (hasTrailingIcon) {
    contentPadding.calculateEndPadding(LocalLayoutDirection.current) + iconSize + trailingIconPadding.calculateEndPadding(LocalLayoutDirection.current)
  } else {
    contentPadding.calculateEndPadding(LocalLayoutDirection.current)
  }

  Box(modifier = modifier.height(24.dp)) {
    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
      value = value,
      modifier = Modifier
        .indicatorLine(enabled, isError, interactionSource, colors)
        // .defaultErrorSemantics(isError, getString(Strings.DefaultErrorMessage))
        .semantics { if (isError) error("Invalid input") }
        .defaultMinSize(
          minWidth = TextFieldDefaults.MinWidth,
          // minHeight = TextFieldDefaults.MinHeight
        ),
      onValueChange = onValueChange,
      enabled = enabled,
      readOnly = readOnly,
      textStyle = mergedTextStyle,
      cursorBrush = SolidColor(colors.cursorColor(isError).value),
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      interactionSource = interactionSource,
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      decorationBox = @Composable { innerTextField ->
        // places leading icon, text field with label and placeholder, trailing icon
        TextFieldDefaults.TextFieldDecorationBox(
          value = value.text,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          placeholder = placeholder,
          label = label,
          // 不使用内置的 leadingIcon
          leadingIcon = null,
          // 不使用内置的 trailingIcon
          trailingIcon = null,
          singleLine = singleLine,
          enabled = enabled,
          isError = isError,
          interactionSource = interactionSource,
          shape = shape,
          colors = colors,
          // 修改内边距
          contentPadding = PaddingValues(
            start = textPaddingStart,
            end = textPaddingEnd,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
          )
        )
      }
    )

    // 放置 leadingIcon
    if (hasLeadingIcon) {
      Box(
        modifier = Modifier
          .padding(leadingIconPadding)
          .size(iconSize)
          .align(Alignment.CenterStart)
      ) {
        leadingIcon!!()
      }
    }
    // 放置 trailingIcon
    if (hasTrailingIcon) {
      Box(
        modifier = Modifier
          .padding(trailingIconPadding)
          .size(iconSize)
          .align(Alignment.CenterEnd)
      ) {
        trailingIcon!!()
      }
    }
  }
}
