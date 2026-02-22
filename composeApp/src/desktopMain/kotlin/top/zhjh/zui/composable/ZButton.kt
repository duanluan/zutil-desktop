package top.zhjh.zui.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.enums.ZColorType.*
import top.zhjh.zui.theme.isAppInDarkTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 按钮组方向。
 */
enum class ZButtonGroupDirection {
  Horizontal,
  Vertical
}

enum class ZButtonSize {
  Large,
  Default,
  Small
}

internal data class ZButtonMetrics(
  val minHeight: Dp,
  val circleSize: Dp,
  val iconSize: Dp,
  val iconSpacing: Dp,
  val contentPadding: PaddingValues,
  val textStyle: TextStyle
)

private val LocalIsInButtonGroup = compositionLocalOf { false }

/**
 * 按钮组容器。
 *
 * 用于将多个 [ZButton] 组合在一起显示，`direction` 控制横向或纵向布局。
 */
@Composable
fun ZButtonGroup(
  modifier: Modifier = Modifier,
  direction: ZButtonGroupDirection = ZButtonGroupDirection.Horizontal,
  itemSpacing: Dp = ZButtonDefaults.GroupItemSpacing,
  content: @Composable () -> Unit
) {
  val groupModifier = modifier.clip(ZButtonDefaults.Shape)

  CompositionLocalProvider(LocalIsInButtonGroup provides true) {
    when (direction) {
      ZButtonGroupDirection.Horizontal -> {
        Row(
          modifier = groupModifier,
          horizontalArrangement = Arrangement.spacedBy(itemSpacing),
          verticalAlignment = Alignment.CenterVertically
        ) {
          content()
        }
      }

      ZButtonGroupDirection.Vertical -> {
        Column(
          modifier = groupModifier,
          verticalArrangement = Arrangement.spacedBy(itemSpacing),
          horizontalAlignment = Alignment.Start
        ) {
          content()
        }
      }
    }
  }
}

/**
 * 按钮
 *
 * @param onClick 点击事件
 * @param modifier 修饰符
 * @param type 类型，默认 [ZColorType.INFO]
 * @param icon 按钮内图标组件
 * @param contentPadding 内边距，默认 [ZButtonDefaults.ContentPadding]
 * @param contentAlignment 内容水平对齐方式，默认 [Alignment.Center]
 * @param content 内容区域的可组合函数
 * @param plain 是否为无装饰，默认 false
 * @param round 两边是否为半圆角，默认 false
 * @param circle 是否为圆形，默认 false
 * @param enabled 是否启用，默认 true
 */
@Composable
fun ZButton(
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier,
  size: ZButtonSize? = null,
  type: ZColorType = DEFAULT,
  href: String? = null,
  icon: @Composable (() -> Unit)? = null,
  loading: Boolean = false,
  loadingIcon: @Composable (() -> Unit)? = null,
  loadingIconSpacing: Dp? = null,
  contentPadding: PaddingValues? = null,
  contentAlignment: Alignment = Alignment.Center,
  plain: Boolean = false,
  round: Boolean = false,
  circle: Boolean = false,
  enabled: Boolean = true,
  content: @Composable (RowScope.() -> Unit)? = null,
) {
  // 表单场景中优先继承 ZForm 尺寸；普通场景保持默认按钮高度。
  val resolvedSize = size ?: ZButtonDefaults.fromFormSize(LocalZFormSize.current)
  val metrics = ZButtonDefaults.metrics(resolvedSize)
  val resolvedContentPadding = contentPadding ?: metrics.contentPadding
  val finalContentPadding = if (circle) PaddingValues(0.dp) else resolvedContentPadding
  val resolvedIconSpacing = if (loading) {
    loadingIconSpacing ?: metrics.iconSpacing
  } else {
    metrics.iconSpacing
  }
  val isInButtonGroup = LocalIsInButtonGroup.current
  val uriHandler = LocalUriHandler.current

  // 圆角半径
  val shape = when {
    isInButtonGroup -> RoundedCornerShape(0.dp)
    round -> RoundedCornerShape(50) // 圆角按钮（左右两端为半圆）
    else -> ZButtonDefaults.Shape // 默认形状
  }
  // 是否为暗黑模式
  val isDarkTheme = isAppInDarkTheme()
  // 交互源，跟踪组件的交互状态
  val interactionSource = remember { MutableInteractionSource() }
  // 是否悬停
  val isHovered by interactionSource.collectIsHoveredAsState()
  // 是否点击
  val isPressed by interactionSource.collectIsPressedAsState()

  // 获取样式
  val buttonStyle = getZButtonStyle(type, isDarkTheme, plain, isHovered, isPressed, enabled)
  val visualStyle = if (loading) {
    getLoadingButtonStyle(buttonStyle, type, plain, isDarkTheme)
  } else {
    buttonStyle
  }
  val isClickable = enabled && !loading
  val resolvedOnClick: () -> Unit = {
    val targetHref = href?.trim().orEmpty()
    if (targetHref.isNotEmpty()) {
      uriHandler.openUri(targetHref)
    } else {
      onClick()
    }
  }
  val resolvedIcon = if (loading) {
    loadingIcon ?: { ZButtonDefaultLoadingIcon() }
  } else {
    icon
  }

  // 非圆形
  if (!circle) {
    Box(
      contentAlignment = contentAlignment,
      modifier = Modifier
        .clip(shape)
        .background(visualStyle.backgroundColor)
        .border(BorderStroke(1.dp, visualStyle.borderColor), shape)
          // 按钮启用时添加点击事件
        .then(if (isClickable) Modifier.clickable(onClick = resolvedOnClick) else Modifier)
        .defaultMinSize(minHeight = metrics.minHeight)
        .then(modifier)
    ) {
      ZButtonContent(
        buttonStyle = visualStyle,
        textStyle = metrics.textStyle,
        iconSize = metrics.iconSize,
        iconSpacing = resolvedIconSpacing,
        contentPadding = finalContentPadding,
        icon = resolvedIcon,
        loading = loading,
        content = content,
        circle = false
      )
    }
  } else {
    Box(
      contentAlignment = contentAlignment,
      modifier = Modifier
          // 按钮启用时添加点击事件
        .then(if (isClickable) Modifier.clickable(onClick = resolvedOnClick) else Modifier)
        .defaultMinSize(minWidth = metrics.circleSize, minHeight = metrics.circleSize)
        .then(modifier)
    ) {
      // 使用Canvas绘制圆形
      Canvas(modifier = Modifier.matchParentSize()) {
        if (type == DEFAULT) {
          // DEFAULT类型只绘制边框圆形
          drawCircle(
            color = visualStyle.borderColor,
            style = Stroke(1.dp.toPx())
          )
        } else {
          // 非DEFAULT类型绘制背景圆形
          drawCircle(
            color = visualStyle.backgroundColor
          )

          // 如果是plain类型，也绘制边框
          if (plain) {
            drawCircle(
              color = visualStyle.borderColor,
              style = Stroke(1.dp.toPx())
            )
          }
        }
      }

      ZButtonContent(
        buttonStyle = visualStyle,
        textStyle = metrics.textStyle,
        iconSize = metrics.iconSize,
        iconSpacing = resolvedIconSpacing,
        contentPadding = finalContentPadding,
        icon = resolvedIcon,
        loading = loading,
        content = content,
        circle = true
      )
    }
  }
}

@Composable
private fun ZButtonContent(
  buttonStyle: ZButtonStyle,
  textStyle: TextStyle,
  iconSize: Dp,
  iconSpacing: Dp,
  contentPadding: PaddingValues,
  icon: (@Composable (() -> Unit))? = null,
  loading: Boolean = false,
  content: (@Composable RowScope.() -> Unit)? = null,
  circle: Boolean = false
) {
  val loadingRotation = if (loading) {
    val transition = rememberInfiniteTransition(label = "z_button_loading_rotation")
    val angle by transition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(
          durationMillis = ZButtonDefaults.LoadingRotationDurationMillis,
          easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
      ),
      label = "z_button_loading_rotation_angle"
    )
    angle
  } else {
    0f
  }

  // 提供内容颜色的上下文，使所有子组件继承此颜色
  CompositionLocalProvider(
    LocalContentColor provides buttonStyle.textColor,
    LocalTextStyle provides textStyle
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(contentPadding)
    ) {
      if (icon != null) {
        Box(
          modifier = Modifier
            .size(iconSize)
            .graphicsLayer {
              rotationZ = if (loading) loadingRotation else 0f
              transformOrigin = TransformOrigin.Center
              clip = loading
            },
          contentAlignment = Alignment.Center
        ) {
          icon()
        }
        if (!circle && content != null) {
          Spacer(modifier = Modifier.width(iconSpacing))
        }
      }
      // 圆形按钮没有图标也要占位
      else if (circle) {
        Box(modifier = Modifier.size(iconSize))
      }
      // 内容函数
      content?.invoke(this)
    }
  }
}

@Composable
private fun ZButtonDefaultLoadingIcon() {
  val color = LocalContentColor.current
  Canvas(modifier = Modifier.fillMaxSize()) {
    val center = center
    val rayCount = 8
    val innerRadius = size.minDimension * 0.15f
    val outerRadius = size.minDimension * 0.47f
    val strokeWidth = size.minDimension * 0.14f
    repeat(rayCount) { index ->
      val angle = ((2.0 * PI) / rayCount * index - PI / 2.0).toFloat()
      val start = Offset(
        x = center.x + cos(angle) * innerRadius,
        y = center.y + sin(angle) * innerRadius
      )
      val end = Offset(
        x = center.x + cos(angle) * outerRadius,
        y = center.y + sin(angle) * outerRadius
      )
      drawLine(
        color = color.copy(alpha = 0.35f + 0.65f * (index + 1) / rayCount.toFloat()),
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
      )
    }
  }
}

/**
 * ZButton 样式类
 */
private data class ZButtonStyle(
  var backgroundColor: Color,
  var borderColor: Color,
  var textColor: Color
)

private fun getLoadingButtonStyle(
  baseStyle: ZButtonStyle,
  type: ZColorType,
  isPlain: Boolean,
  isDarkTheme: Boolean
): ZButtonStyle {
  if (type == PRIMARY && !isPlain) {
    val loadingColor = if (isDarkTheme) {
      ZButtonDefaults.LoadingPrimaryDarkColor
    } else {
      ZButtonDefaults.LoadingPrimaryLightColor
    }
    return baseStyle.copy(
      backgroundColor = loadingColor,
      borderColor = loadingColor
    )
  }
  return baseStyle
}

/**
 * 根据按钮类型、悬停状态和主题模式获取按钮样式
 */
private fun getZButtonStyle(type: ZColorType, isDarkTheme: Boolean, isPlain: Boolean, isHovered: Boolean, isPressed: Boolean, enabled: Boolean): ZButtonStyle {
  if (!enabled) {
    return when (type) {
      DEFAULT -> {
        if (isDarkTheme) {
          ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff414243),
            textColor = Color(0xff888888)
          )
        } else {
          ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xfff2f2f6),
            textColor = Color(0xffbcbec4)
          )
        }
      }

      PRIMARY -> {
        if (isDarkTheme) {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xff18222c) else Color(0xff2a598a),
            borderColor = if (isPlain) Color(0xff1d3043) else Color(0xff2a598a),
            textColor = if (isPlain) Color(0xff2a598a) else Color(0xff8fa7c1)
          )
        } else {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xffecf4fe) else Color(0xffa0cefe),
            borderColor = if (isPlain) Color(0xffd8ecfe) else Color(0xffa0cefe),
            textColor = if (isPlain) Color(0xffa0cfff) else Color.White
          )
        }
      }

      SUCCESS -> {
        if (isDarkTheme) {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xff1c2518) else Color(0xff3e6b27),
            borderColor = if (isPlain) Color(0xff25371c) else Color(0xff3e6b27),
            textColor = if (isPlain) Color(0xff3e6b27) else Color(0xff99b18d)
          )
        } else {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xfff0f8ea) else Color(0xffb2e09c),
            borderColor = if (isPlain) Color(0xffe0f2d8) else Color(0xffb2e09c),
            textColor = if (isPlain) Color(0xffb3e19d) else Color.White
          )
        }
      }

      INFO -> {
        if (isDarkTheme) {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xff202121) else Color(0xff525457),
            borderColor = if (isPlain) Color(0xff2d2d2f) else Color(0xff525457),
            textColor = if (isPlain) Color(0xff4f5153) else Color(0xffa6a7a9)
          )
        } else {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xfff4f4f4) else Color(0xffc8c8cc),
            borderColor = if (isPlain) Color(0xffe9e9eb) else Color(0xffc8c8cc),
            textColor = if (isPlain) Color(0xffc8c8cc) else Color.White
          )
        }
      }

      WARNING -> {
        if (isDarkTheme) {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xff292218) else Color(0xff7d5b28),
            borderColor = if (isPlain) Color(0xff3e301c) else Color(0xff7d5b28),
            textColor = if (isPlain) Color(0xff7d5b28) else Color(0xffbaa88e)
          )
        } else {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xfffcf6ec) else Color(0xfff2d09e),
            borderColor = if (isPlain) Color(0xfffaecd8) else Color(0xfff2d09e),
            textColor = if (isPlain) Color(0xfff3d19e) else Color.White
          )
        }
      }

      DANGER -> {
        if (isDarkTheme) {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xff2b1d1d) else Color(0xff854040),
            borderColor = if (isPlain) Color(0xff412626) else Color(0xff854040),
            textColor = if (isPlain) Color(0xff854040) else Color(0xffbf9a9a)
          )
        } else {
          ZButtonStyle(
            backgroundColor = if (isPlain) Color(0xfffef0f0) else Color(0xfffab6b6),
            borderColor = if (isPlain) Color(0xfffde2e2) else Color(0xfffab6b6),
            textColor = if (isPlain) Color(0xfffab6b6) else Color.White
          )
        }
      }
    }
  }
  return when (type) {
    DEFAULT -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff409eff),
            textColor = Color(0xff409eff)
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff4c4d4f),
            textColor = Color(0xffcfd3dc)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff409eff),
            textColor = Color(0xff409eff)
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xffdcdfe6),
            textColor = Color(0xff606266)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = Color(0xff18222c),
            borderColor = if (isPressed) Color(0xff409eff) else Color(0xff213d5b),
            textColor = Color(0xff409eff)
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff4c4d4f),
            textColor = Color(0xffcfd3dc)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = Color(0xffECF5FF),
            borderColor = if (isPressed) Color(0xff409eff) else Color(0xffC6E2FF),
            textColor = Color(0xff409EFF)
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xffDCDDE6),
            textColor = Color(0xff606266)
          )
        }
      }
    }

    PRIMARY -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff66b1ff) else Color(0xff409eff),
            borderColor = if (isPressed) Color(0xff66b1ff) else Color(0xff409eff),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff18222c),
            borderColor = Color(0xff2a598a),
            textColor = Color(0xff409eff)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff337ecc) else Color(0xff409eff),
            borderColor = if (isPressed) Color(0xff337ecc) else Color(0xff409eff),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xffecf5ff),
            borderColor = Color(0xffa0cfff),
            textColor = Color(0xff409eff)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff66b1ff) else Color(0xff3375b9),
            borderColor = if (isPressed) Color(0xff66b1ff) else Color(0xff3375b9),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff409eff),
            borderColor = Color(0xff409eff),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff337ecc) else Color(0xff79bbff),
            borderColor = if (isPressed) Color(0xff337ecc) else Color(0xff79bbff),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xff409eff),
            borderColor = Color(0xff409eff),
            textColor = Color.White
          )
        }
      }
    }

    SUCCESS -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff85ce61) else Color(0xff67c23a),
            borderColor = if (isPressed) Color(0xff85ce61) else Color(0xff67c23a),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff1c2518),
            borderColor = Color(0xff3e6b27),
            textColor = Color(0xff67c23a)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff529b2e) else Color(0xff67c23a),
            borderColor = if (isPressed) Color(0xff529b2e) else Color(0xff67c23a),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xfff0f9eb),
            borderColor = Color(0xffb3e19d),
            textColor = Color(0xff67c23a)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff85ce61) else Color(0xff4e8e2f),
            borderColor = if (isPressed) Color(0xff85ce61) else Color(0xff4e8e2f),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff67c23a),
            borderColor = Color(0xff67c23a),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff529b2e) else Color(0xff95d475),
            borderColor = if (isPressed) Color(0xff529b2e) else Color(0xff95d475),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xff67c23a),
            borderColor = Color(0xff67c23a),
            textColor = Color.White
          )
        }
      }
    }

    INFO -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff909399),
            borderColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff909399),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff202121),
            borderColor = Color(0xff525457),
            textColor = Color(0xff909399)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff73767a) else Color(0xff909399),
            borderColor = if (isPressed) Color(0xff73767a) else Color(0xff909399),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xfff4f4f5),
            borderColor = Color(0xffc8c9cc),
            textColor = Color(0xff909399)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff6b6d71),
            borderColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff6b6d71),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff909399),
            borderColor = Color(0xff909399),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xff73767a) else Color(0xffb1b3b8),
            borderColor = if (isPressed) Color(0xff73767a) else Color(0xffb1b3b8),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xff909399),
            borderColor = Color(0xff909399),
            textColor = Color.White
          )
        }

      }
    }

    WARNING -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffebb563) else Color(0xffe6a23c),
            borderColor = if (isPressed) Color(0xffebb563) else Color(0xffe6a23c),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff292218),
            borderColor = Color(0xff7d5b28),
            textColor = Color(0xffe6a23c)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffb88230) else Color(0xffe6a23c),
            borderColor = if (isPressed) Color(0xffb88230) else Color(0xffe6a23c),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xfffdf6ec),
            borderColor = Color(0xfff3d19e),
            textColor = Color(0xffe6a23c)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffebb563) else Color(0xffa77730),
            borderColor = if (isPressed) Color(0xffebb563) else Color(0xffa77730),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xffe6a23c),
            borderColor = Color(0xffe6a23c),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffb88230) else Color(0xffeebe77),
            borderColor = if (isPressed) Color(0xffb88230) else Color(0xffeebe77),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xffe6a23c),
            borderColor = Color(0xffe6a23c),
            textColor = Color.White
          )
        }
      }
    }

    DANGER -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xfff78989) else Color(0xfff56c6c),
            borderColor = if (isPressed) Color(0xfff78989) else Color(0xfff56c6c),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xff2b1d1d),
            borderColor = Color(0xff854040),
            textColor = Color(0xfff56c6c)
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffc45656) else Color(0xfff56c6c),
            borderColor = if (isPressed) Color(0xffc45656) else Color(0xfff56c6c),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xfffef0f0),
            borderColor = Color(0xfffab6b6),
            textColor = Color(0xfff56c6c)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xfff78989) else Color(0xffb25252),
            borderColor = if (isPressed) Color(0xfff78989) else Color(0xffb25252),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ZButtonStyle(
            backgroundColor = Color(0xfff56c6c),
            borderColor = Color(0xfff56c6c),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ZButtonStyle(
            backgroundColor = if (isPressed) Color(0xffc45656) else Color(0xfff89898),
            borderColor = if (isPressed) Color(0xffc45656) else Color(0xfff89898),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ZButtonStyle(
            backgroundColor = Color(0xfff56c6c),
            borderColor = Color(0xfff56c6c),
            textColor = Color.White
          )
        }

      }
    }
  }
}

/**
 * ZButton 默认值，参考 [androidx.compose.material.ButtonDefaults]
 */
object ZButtonDefaults {
  private val DefaultButtonHorizontalPadding = 10.dp
  private val LargeButtonHorizontalPadding = 16.dp
  private val SmallButtonHorizontalPadding = 8.dp
  private val DefaultFontSize = 14.sp
  private val SmallFontSize = 12.sp

  /**
   * 内边距
   */
  val ContentPadding = PaddingValues(
    start = DefaultButtonHorizontalPadding,
    end = DefaultButtonHorizontalPadding,
    top = 0.dp,
    bottom = 0.dp
  )
  val LargeContentPadding = PaddingValues(
    start = LargeButtonHorizontalPadding,
    end = LargeButtonHorizontalPadding,
    top = 0.dp,
    bottom = 0.dp
  )
  val SmallContentPadding = PaddingValues(
    start = SmallButtonHorizontalPadding,
    end = SmallButtonHorizontalPadding,
    top = 0.dp,
    bottom = 0.dp
  )

  /**
   * 最小高度
   */
  val MinHeight = 30.dp
  val LargeMinHeight = 38.dp
  val SmallMinHeight = 24.dp

  /**
   * 字体大小
   */
  val IconSize = 14.dp
  val LargeIconSize = 16.dp
  val SmallIconSize = 12.dp

  /**
   * 图标大小
   */
  val IconSpacing = 2.dp
  val LargeIconSpacing = 3.dp
  val SmallIconSpacing = 1.dp
  val CircleSize = MinHeight
  val LargeCircleSize = LargeMinHeight
  val SmallCircleSize = SmallMinHeight

  fun fromFormSize(size: ZFormSize?): ZButtonSize {
    return when (size) {
      ZFormSize.LARGE -> ZButtonSize.Large
      ZFormSize.SMALL -> ZButtonSize.Small
      else -> ZButtonSize.Default
    }
  }

  internal fun metrics(size: ZButtonSize): ZButtonMetrics {
    return when (size) {
      ZButtonSize.Large -> ZButtonMetrics(
        minHeight = LargeMinHeight,
        circleSize = LargeCircleSize,
        iconSize = LargeIconSize,
        iconSpacing = LargeIconSpacing,
        contentPadding = LargeContentPadding,
        textStyle = TextStyle(fontSize = DefaultFontSize)
      )

      ZButtonSize.Small -> ZButtonMetrics(
        minHeight = SmallMinHeight,
        circleSize = SmallCircleSize,
        iconSize = SmallIconSize,
        iconSpacing = SmallIconSpacing,
        contentPadding = SmallContentPadding,
        textStyle = TextStyle(fontSize = SmallFontSize)
      )

      ZButtonSize.Default -> ZButtonMetrics(
        minHeight = MinHeight,
        circleSize = CircleSize,
        iconSize = IconSize,
        iconSpacing = IconSpacing,
        contentPadding = ContentPadding,
        textStyle = TextStyle(fontSize = DefaultFontSize)
      )
    }
  }

  val GroupItemSpacing = 0.8.dp
  val LoadingPrimaryLightColor = Color(0xff7abbff)
  val LoadingPrimaryDarkColor = Color(0xff2d6eb2)
  const val LoadingRotationDurationMillis = 900

  /**
   * 圆角形状
   */
  val Shape = RoundedCornerShape(4.dp)
}
