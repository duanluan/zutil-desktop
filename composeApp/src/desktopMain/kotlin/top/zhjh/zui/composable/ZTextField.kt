package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.zhjh.zui.theme.isAppInDarkTheme
import java.awt.Cursor

/**
 * 输入框类�?
 */
enum class ZTextFieldType {
  /**
   * 单行文本
   */
  TEXT,

  /**
   * 密码
   */
  PASSWORD,

  /**
   * 多行文本
   */
  TEXTAREA
}

/**
 * 输入�?
 *
 * @param value �?
 * @param onValueChange 值变化事�?
 * @param modifier 修饰�?
 * @param type 输入框类型，默认 [ZTextFieldType.TEXT]
 * @param enabled 是否可用，默�?true
 * @param readOnly 是否只读，默�?false
 * @param showPassword 是否显示密码切换图标 [ZTextFieldType.PASSWORD] 时生效，默认 false
 * @param resize 是否允许手动调整高度（仅�?[ZTextFieldType.TEXTAREA] 时生效），默�?true
 * @param singleLine 是否单行，默�?true
 * @param maxLines 最大行数，单行时为 1，否则为 [Int.MAX_VALUE]
 * @param minLines 最小行数，默认 1
 * @param leadingIcon 左侧图标
 * @param trailingIcon 右侧图标
 * @param placeholder 占位符文本，如果值为空时显示
 * @param numericOnly 是否仅允许数字输�?
 * @param onMouseWheel 鼠标滚轮滚动回调（用于数值调节）
 * @param onFocusChanged 焦点变化回调（true=聚焦，false=失焦�?
 */
@Composable
fun ZTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  size: ZFormSize? = null,
  type: ZTextFieldType = ZTextFieldType.TEXT,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  showPassword: Boolean = false,
  resize: Boolean = true,
  textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = ZTextFieldDefaults.FontSize),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  minLines: Int = 1,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  placeholder: String? = null,
  visualTransformation: VisualTransformation? = null,
  numericOnly: Boolean = false,
  onMouseWheel: ((Float) -> Unit)? = null,
  onFocusChanged: ((Boolean) -> Unit)? = null,
) {
  // 表单场景中优先使�?ZForm 传入的尺寸上下文；非表单场景保持原默认高度�?
  val resolvedSize = size ?: LocalZFormSize.current
  val minHeight = ZFormDefaults.resolveControlHeight(resolvedSize, ZTextFieldDefaults.MinHeight)

  // 圆角半径
  val shape = ZTextFieldDefaults.Shape
  // 判断是否为暗黑模�?
  val isDarkTheme = isAppInDarkTheme()
  // 交互源，跟踪组件的交互状�?
  val interactionSource = remember { MutableInteractionSource() }
  // 是否悬停
  val isHovered by interactionSource.collectIsHoveredAsState()
  // 是否聚焦
  val isFocused: State<Boolean> = interactionSource.collectIsFocusedAsState()
  LaunchedEffect(isFocused.value) {
    onFocusChanged?.invoke(isFocused.value)
  }

  // 获取样式
  val validateStatus = LocalZFormValidateStatus.current
  val textFieldStyle = getZTextFieldStyle(
    isDarkTheme = isDarkTheme,
    isHovered = isHovered,
    isFocused = isFocused,
    enabled = enabled,
    validateStatus = validateStatus
  )
  // 应用字体颜色
  val finalTextStyle = textStyle.copy(color = textFieldStyle.textColor)
  val finalKeyboardOptions = if (numericOnly) {
    keyboardOptions.copy(keyboardType = KeyboardType.Number)
  } else {
    keyboardOptions
  }

  var isPasswordVisible by remember { mutableStateOf(false) }
  val showStatusIcon = LocalZFormStatusIconEnabled.current
  val isTextarea = type == ZTextFieldType.TEXTAREA
  val resolvedVisualTransformation = when {
    visualTransformation != null -> visualTransformation
    type == ZTextFieldType.PASSWORD && !isPasswordVisible -> PasswordVisualTransformation()
    else -> VisualTransformation.None
  }

  val statusTrailingIcon: (@Composable () -> Unit)? = if (showStatusIcon && !isTextarea) {
    {
      val (icon, tint) = when (validateStatus) {
        ZFormValidateStatus.SUCCESS -> FeatherIcons.CheckCircle to ZFormDefaults.SuccessColor
        ZFormValidateStatus.ERROR -> FeatherIcons.AlertCircle to ZFormDefaults.ErrorIconColor
        ZFormValidateStatus.NONE -> null to Color.Unspecified
      }
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(ZTextFieldDefaults.IconSize)
        )
      }
    }
  } else {
    null
  }

  val finalTrailingIcon: (@Composable () -> Unit)? = if (trailingIcon != null) {
    trailingIcon
  } else if (type == ZTextFieldType.PASSWORD && showPassword) {
    {
      val icon = if (isPasswordVisible) FeatherIcons.Eye else FeatherIcons.EyeOff
      Icon(
        imageVector = icon,
        contentDescription = "Toggle Password Visibility",
        modifier = Modifier
          .size(ZTextFieldDefaults.IconSize)
          .pointerHoverIcon(PointerIcon.Hand)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            isPasswordVisible = !isPasswordVisible
          }
      )
    }
  } else {
    null
  }

  val mergedTrailingIcon: (@Composable () -> Unit)? = when {
    statusTrailingIcon != null && finalTrailingIcon != null -> {
      {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          statusTrailingIcon.invoke()
          finalTrailingIcon.invoke()
        }
      }
    }
    statusTrailingIcon != null -> statusTrailingIcon
    else -> finalTrailingIcon
  }

  val finalSingleLine = if (isTextarea) false else singleLine
  val finalMinLines = if (type == ZTextFieldType.TEXTAREA && minLines == 1) 2 else minLines
  val finalMaxLines = if (type == ZTextFieldType.TEXTAREA && maxLines == 1) Int.MAX_VALUE else maxLines

  // 处理拖拽高度的逻辑
  val density = LocalDensity.current
  // 用户手动拖拽后的高度，如果为 null 则使用默认高度逻辑
  var dragHeight by remember { mutableStateOf<Dp?>(null) }
  // 组件当前的实际像素高度（用于在拖拽开始时计算基准�?
  var currentHeightPx by remember { mutableStateOf(0f) }

  BasicTextField(
    value = value,
    onValueChange = { newValue ->
      val filtered = if (numericOnly) newValue.filter { it.isDigit() } else newValue
      onValueChange(filtered)
    },
    interactionSource = interactionSource,
    modifier = Modifier
      // 悬停检�?
      .hoverable(interactionSource)
      .clip(shape)
      .background(color = textFieldStyle.backgroundColor)
      // 边框，聚焦时修改边框颜色
      .border(width = 1.dp, color = textFieldStyle.borderColor, shape = shape)
      // 最小高�?
      .defaultMinSize(minHeight = minHeight)
      .onGloballyPositioned { coordinates ->
        currentHeightPx = coordinates.size.height.toFloat()
      }
      .then(
        // 必须�?TEXTAREA �?开启了 resize，且 dragHeight 有值时才应用高�?
        if (isTextarea && resize && dragHeight != null) Modifier.height(dragHeight!!)
        else Modifier.defaultMinSize(minHeight = minHeight)
      )
      .then(
        if (onMouseWheel != null) {
          Modifier.zMouseWheel(onMouseWheel)
        } else Modifier
      )
      .then(modifier),
    enabled = enabled,
    readOnly = readOnly,
    textStyle = finalTextStyle,
    keyboardOptions = finalKeyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = finalSingleLine,
    minLines = finalMinLines,
    maxLines = finalMaxLines,
    visualTransformation = resolvedVisualTransformation,
    // 光标
    cursorBrush = textFieldStyle.cursorColor?.let { SolidColor(it) } ?: SolidColor(LocalContentColor.current),  // �?cursorColor 为空时使用默认颜�?
    decorationBox = { innerTextField ->
      // 使用 Box 包裹，以便在右下角叠加图�?
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isTextarea) Alignment.TopStart else Alignment.CenterStart
      ) {
        Row(
          // Textarea 顶部对齐，Text 垂直居中
          verticalAlignment = if (isTextarea) Alignment.Top else Alignment.CenterVertically,
          modifier = Modifier
            .padding(ZTextFieldDefaults.ContentPadding)
            // 仅针�?TEXTAREA 增加垂直内边距，保持单行样式不变
            .then(if (isTextarea) Modifier.padding(vertical = 5.dp) else Modifier)
        ) {
          // 左侧图标
          if (leadingIcon != null) {
            // 使用 ZIconWrapper 包裹图标以处理颜色和悬停逻辑
            ZIconWrapper(
              icon = leadingIcon,
              iconColor = textFieldStyle.iconColor,
              iconHoverColor = textFieldStyle.iconHoverColor,
              modifier = Modifier
                // 图标右边�?
                .padding(end = ZTextFieldDefaults.IconSpacing)
                // 图标大小
                .defaultMinSize(
                  minWidth = ZTextFieldDefaults.IconSize,
                  minHeight = ZTextFieldDefaults.IconSize
                )
                // 针对 TEXTAREA 增加图标顶部偏移，使�?offset 避免挤压
                .then(if (isTextarea) Modifier.offset(y = 2.dp) else Modifier)
            )
          }

          // 输入内容
          Box(modifier = Modifier.weight(1f)) {
            // 提供内容颜色的上下文，使所有子组件继承此颜�?
            CompositionLocalProvider(LocalContentColor provides textFieldStyle.textColor) {
              // 输入内容
              innerTextField()
              // 输入为空时显示占位符
              if (value.isEmpty() && placeholder != null) {
                Text(
                  text = placeholder,
                  style = finalTextStyle,
                  color = textFieldStyle.placeholderColor
                )
              }
            }
          }

          // 右侧图标
          if (mergedTrailingIcon != null) {
            ZIconWrapper(
              icon = mergedTrailingIcon,
              iconColor = textFieldStyle.iconColor,
              iconHoverColor = textFieldStyle.iconHoverColor,
              modifier = Modifier
                // 图标左边�?
                .padding(start = ZTextFieldDefaults.IconSpacing)
                // 图标大小
                .defaultMinSize(
                  minWidth = ZTextFieldDefaults.IconSize,
                  minHeight = ZTextFieldDefaults.IconSize
                )
                // 针对 TEXTAREA 增加图标顶部偏移
                .then(if (isTextarea) Modifier.offset(y = 2.dp) else Modifier)
            )
          }
        }

        // 如果�?TEXTAREA，在右下角绘制拖拽标�?
        if (isTextarea && resize) {
          val iconColor = textFieldStyle.iconColor
          androidx.compose.foundation.Canvas(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              // 距离右下角稍微留点空�?
              .padding(bottom = 2.dp, end = 2.dp)
              .size(10.dp)
              // 鼠标悬停时显�?�?箭头
              .pointerHoverIcon(PointerIcon(Cursor(Cursor.SE_RESIZE_CURSOR)))
              // 监听拖拽手势
              .pointerInput(Unit) {
                detectDragGestures(
                  onDragStart = {
                    // 开始拖拽时，如果还没设置过高度，先用当前实际高度初始化
                    if (dragHeight == null) {
                      dragHeight = with(density) { currentHeightPx.toDp() }
                    }
                  },
                  onDrag = { change, dragAmount ->
                    change.consume()
                    // 计算新高�?= 当前高度 + 拖拽垂直距离
                    val currentDp = dragHeight ?: with(density) { currentHeightPx.toDp() }
                    val deltaDp = with(density) { dragAmount.y.toDp() }
                    val newHeight = (currentDp + deltaDp).coerceAtLeast(minHeight)

                    dragHeight = newHeight
                  }
                )
              }
          ) {
            // 绘制第一条线（较短）
            drawLine(
              color = iconColor,
              start = androidx.compose.ui.geometry.Offset(this.size.width, this.size.height * 0.5f),
              end = androidx.compose.ui.geometry.Offset(this.size.width * 0.5f, this.size.height),
              strokeWidth = 1.dp.toPx(),
              cap = StrokeCap.Round
            )
            // 绘制第二条线（较长，在左上方�?
            drawLine(
              color = iconColor,
              start = androidx.compose.ui.geometry.Offset(this.size.width, 0f),
              end = androidx.compose.ui.geometry.Offset(0f, this.size.height),
              strokeWidth = 1.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }
      }
    }
  )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.zMouseWheel(onMouseWheel: (Float) -> Unit): Modifier {
  return onPointerEvent(PointerEventType.Scroll) { event ->
    val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
    if (delta != 0f) {
      onMouseWheel(delta)
    }
  }
}

/**
 * 图标包装器：用于处理图标的颜色和悬停状�?
 */
@Composable
private fun ZIconWrapper(
  icon: @Composable () -> Unit,
  iconColor: Color,
  iconHoverColor: Color,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val currentColor = if (isHovered) iconHoverColor else iconColor

  Box(
    modifier = modifier.hoverable(interactionSource),
    contentAlignment = Alignment.Center
  ) {
    // 提供内容颜色的上下文，使所有子组件继承此颜�?
    CompositionLocalProvider(LocalContentColor provides currentColor) {
      icon()
    }
  }
}

/**
 * ZTextField 样式�?
 */
private data class ZTextFieldStyle(
  var backgroundColor: Color,
  var borderColor: Color,
  var textColor: Color,
  // 占位符颜�?
  var placeholderColor: Color,
  // 图标默认颜色
  var iconColor: Color,
  // 图标悬停颜色
  var iconHoverColor: Color,
  var cursorColor: Color? = null
)

/**
 * 获取输入框样�?
 * @param isDarkTheme 是否暗黑模式
 * @param isHovered 是否悬停
 * @param isFocused 是否聚焦状�?
 *
 */
private fun getZTextFieldStyle(
  isDarkTheme: Boolean,
  isHovered: Boolean,
  isFocused: State<Boolean>,
  enabled: Boolean,
  validateStatus: ZFormValidateStatus
): ZTextFieldStyle {
  // 定义图标颜色常量
  val iconDefault = if (isDarkTheme) Color(0xff8d9095) else Color(0xffa8abb2)
  val iconHover = if (isDarkTheme) Color(0xffa3a6ad) else Color(0xff909399)
  val placeholderColor = if (isDarkTheme) Color(0xff8d9095) else Color(0xffa8abb2)

  // 禁用�?
  if (!enabled) {
    return ZTextFieldStyle(
      backgroundColor = if (isDarkTheme) Color(0xff262727) else Color(0xfff5f7fa),
      borderColor = if (isDarkTheme) Color(0xff414243) else Color(0xffe4e7ed),
      textColor = if (isDarkTheme) Color(0xff8d9095) else Color(0xffa8abb2),
      placeholderColor = placeholderColor,
      iconColor = iconDefault,
      iconHoverColor = iconDefault // 禁用时不响应悬停变色
    )
  }
  // 聚焦�?
  if (validateStatus == ZFormValidateStatus.ERROR) {
    return ZTextFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = ZFormDefaults.ErrorMessageColor,
      textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
      placeholderColor = placeholderColor,
      cursorColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
      iconColor = iconDefault,
      iconHoverColor = iconHover
    )
  }
  if (isFocused.value) {
    return ZTextFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = if (isDarkTheme) Color(0xff409eff) else Color(0xff409eff),
      textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
      placeholderColor = placeholderColor,
      cursorColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
      iconColor = iconDefault,
      iconHoverColor = iconHover
    )
  }
  // 悬停�?
  if (isHovered) {
    return ZTextFieldStyle(
      backgroundColor = Color.Transparent,
      borderColor = if (isDarkTheme) Color(0xff6c6e72) else Color(0xffc0c4cc),
      textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
      placeholderColor = placeholderColor,
      iconColor = iconDefault,
      iconHoverColor = iconHover
    )
  }
  // 默认
  return ZTextFieldStyle(
    backgroundColor = Color.Transparent,
    borderColor = if (isDarkTheme) Color(0xff4c4d4f) else Color(0xffdcdfe6),
    textColor = if (isDarkTheme) Color(0xffcfd3dc) else Color(0xff606266),
    placeholderColor = placeholderColor,
    iconColor = iconDefault,
    iconHoverColor = iconHover
  )
}

/**
 * ZTextField 默认值，参�?[androidx.compose.material.TextFieldDefaults]
 */
@Immutable
object ZTextFieldDefaults {
  private val TextFieldHorizontalPadding = 8.dp

  /**
   * 内边�?
   */
  val ContentPadding = PaddingValues(
    start = TextFieldHorizontalPadding,
    end = TextFieldHorizontalPadding,
  )

  /**
   * 最小高�?
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
   * 图标与文字间�?
   */
  val IconSpacing = 6.dp

  /**
   * 圆角形状
   */
  val Shape = RoundedCornerShape(4.dp)
}

