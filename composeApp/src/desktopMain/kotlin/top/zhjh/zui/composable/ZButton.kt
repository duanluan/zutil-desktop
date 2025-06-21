package top.zhjh.zui.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.enums.ZColorType.*
import top.zhjh.zui.theme.isAppInDarkTheme

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
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  type: ZColorType = DEFAULT,
  icon: @Composable (() -> Unit)? = null,
  contentPadding: PaddingValues = ZButtonDefaults.ContentPadding,
  contentAlignment: Alignment = Alignment.Center,
  plain: Boolean = false,
  round: Boolean = false,
  circle: Boolean = false,
  enabled: Boolean = true,
  content: @Composable (RowScope.() -> Unit)? = null,
) {
  // 圆角半径
  val shape = when {
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

  // 非圆形
  if (!circle) {
    Box(
      contentAlignment = contentAlignment,
      modifier = Modifier
        .clip(shape)
        .background(buttonStyle.backgroundColor)
        .border(BorderStroke(1.dp, buttonStyle.borderColor), shape)
          // 按钮启用时添加点击事件
        .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
        .defaultMinSize(minHeight = ZButtonDefaults.MinHeight)
        .then(modifier)
    ) {
      ZButtonContent(
        buttonStyle = buttonStyle,
        contentPadding = contentPadding,
        icon = icon,
        content = content,
        circle = false
      )
    }
  } else {
    Box(
      contentAlignment = contentAlignment,
      modifier = Modifier
          // 按钮启用时添加点击事件
        .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
        .defaultMinSize(minHeight = ZButtonDefaults.MinHeight)
        .then(modifier)
    ) {
      // 使用Canvas绘制圆形
      Canvas(modifier = Modifier.matchParentSize()) {
        if (type == DEFAULT) {
          // DEFAULT类型只绘制边框圆形
          drawCircle(
            color = buttonStyle.borderColor,
            style = Stroke(1.dp.toPx())
          )
        } else {
          // 非DEFAULT类型绘制背景圆形
          drawCircle(
            color = buttonStyle.backgroundColor
          )

          // 如果是plain类型，也绘制边框
          if (plain) {
            drawCircle(
              color = buttonStyle.borderColor,
              style = Stroke(1.dp.toPx())
            )
          }
        }
      }

      ZButtonContent(
        buttonStyle = buttonStyle,
        contentPadding = contentPadding,
        icon = icon,
        content = content,
        circle = true
      )
    }
  }
}

@Composable
private fun ZButtonContent(
  buttonStyle: ZButtonStyle,
  contentPadding: PaddingValues,
  icon: (@Composable (() -> Unit))? = null,
  content: (@Composable RowScope.() -> Unit)? = null,
  circle: Boolean = false
) {
  // 提供内容颜色的上下文，使所有子组件继承此颜色
  CompositionLocalProvider(LocalContentColor provides buttonStyle.textColor) {
    Row(
      verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(contentPadding)
    ) {
      if (icon != null) {
        Box(
          modifier = Modifier.then(
            if (!circle && content != null) Modifier.padding(end = ZButtonDefaults.IconSpacing)
            else Modifier
          ).size(ZButtonDefaults.IconSize)
        ) {
          icon()
        }
      }
      // 圆形按钮没有图标也要占位
      else if (circle) {
        Box(modifier = Modifier.size(ZButtonDefaults.IconSize))
      }
      // 内容函数
      content?.invoke(this)
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
  private val ButtonHorizontalPadding = 10.dp

  /**
   * 内边距
   */
  val ContentPadding = PaddingValues(
    start = ButtonHorizontalPadding,
    end = ButtonHorizontalPadding,
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
  val IconSize = 14.dp

  /**
   * 图标大小
   */
  val IconSpacing = 2.dp

  /**
   * 圆角形状
   */
  val Shape = RoundedCornerShape(4.dp)
}
