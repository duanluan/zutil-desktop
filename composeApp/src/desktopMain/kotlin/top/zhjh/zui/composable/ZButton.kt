package top.zhjh.zui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.isAppInDarkTheme

/**
 * 按钮
 *
 * @param onClick 点击事件
 * @param modifier 修饰符
 * @param type 类型，默认 [ZColorType.INFO]
 * @param enabled 是否启用，默认为 true
 * @param icon 按钮内图标
 * @param iconDescription 按钮内图标描述文本，用于无障碍功能
 * @param contentPadding 内边距，默认 [ZButtonDefaults.ContentPadding]
 * @param content 按钮内容区域的可组合函数
 * @param plain 是否为无装饰按钮，默认为 false
 */
@Composable
fun ZButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  type: ZColorType = ZColorType.DEFAULT,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  iconDescription: String? = null,
  contentPadding: PaddingValues = ZButtonDefaults.ContentPadding,
  plain: Boolean = false,
  content: (@Composable RowScope.() -> Unit)? = null,
) {
  // 圆角半径
  val shape = ZButtonDefaults.Shape
  // 可交换源，监听鼠标悬停状态
  val interactionSource = remember { MutableInteractionSource() }
  // 是否悬停状态
  val isHovered by interactionSource.collectIsHoveredAsState()
  // 是否点击状态
  val isPressed by interactionSource.collectIsPressedAsState()
  // 判断是否为暗黑模式
  val isDarkTheme = isAppInDarkTheme()

  // 获取按钮样式
  val buttonStyle = getButtonStyle(type, isDarkTheme, plain, isHovered, isPressed)

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .clip(shape)
      .background(buttonStyle.backgroundColor)
      .border(BorderStroke(1.dp, buttonStyle.borderColor), shape)
      .then(
        // 按钮启用时添加点击事件
        if (enabled) Modifier.clickable(
          interactionSource = interactionSource,
          indication = null,
          enabled = enabled,
          onClick = onClick
        ) else Modifier
      )
      .defaultMinSize(minHeight = ZButtonDefaults.MinHeight)
  ) {
    // 提供内容颜色的上下文，使所有子组件继承此颜色
    CompositionLocalProvider(LocalContentColor provides buttonStyle.textColor) {
      Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(contentPadding)
      ) {
        if (icon != null) {
          Icon(
            modifier = Modifier
              .then(
                // 有内容时添加右边距
                if (content != null) Modifier.padding(end = ZButtonDefaults.IconSpacing)
                else Modifier
              )
              .size(ZButtonDefaults.IconSize),
            imageVector = icon,
            contentDescription = iconDescription
          )
        }
        // 内容函数
        content?.invoke(this)
      }
    }
  }
}

/**
 * 按钮样式数据类
 */
private data class ButtonStyle(
  var backgroundColor: Color,
  var borderColor: Color,
  var textColor: Color
)

/**
 * 根据按钮类型、悬停状态和主题模式获取按钮样式
 */
@Composable
private fun getButtonStyle(type: ZColorType, isDarkTheme: Boolean, isPlain: Boolean, isHovered: Boolean, isPressed: Boolean): ButtonStyle {
  return when (type) {
    ZColorType.DEFAULT -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff409eff),
            textColor = Color(0xff409eff)
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff4c4d4f),
            textColor = Color(0xffcfd3dc)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff409eff),
            textColor = Color(0xff409eff)
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xffdcdfe6),
            textColor = Color(0xff606266)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = Color(0xff18222c),
            borderColor = if (isPressed) Color(0xff409eff) else Color(0xff213d5b),
            textColor = Color(0xff409eff)
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xff4c4d4f),
            textColor = Color(0xffcfd3dc)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = Color(0xffECF5FF),
            borderColor = if (isPressed) Color(0xff409eff) else Color(0xffC6E2FF),
            textColor = Color(0xff409EFF)
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color.Transparent,
            borderColor = Color(0xffDCDDE6),
            textColor = Color(0xff606266)
          )
        }
      }
    }

    ZColorType.PRIMARY -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff66b1ff) else Color(0xff409eff),
            borderColor = if (isPressed) Color(0xff66b1ff) else Color(0xff409eff),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff18222c),
            borderColor = Color(0xff2a598a),
            textColor = Color(0xff409eff)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff337ecc) else Color(0xff409eff),
            borderColor = if (isPressed) Color(0xff337ecc) else Color(0xff409eff),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xffecf5ff),
            borderColor = Color(0xffa0cfff),
            textColor = Color(0xff409eff)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff66b1ff) else Color(0xff3375b9),
            borderColor = if (isPressed) Color(0xff66b1ff) else Color(0xff3375b9),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff409eff),
            borderColor = Color(0xff409eff),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff337ecc) else Color(0xff79bbff),
            borderColor = if (isPressed) Color(0xff337ecc) else Color(0xff79bbff),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xff409eff),
            borderColor = Color(0xff409eff),
            textColor = Color.White
          )
        }
      }
    }

    ZColorType.SUCCESS -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff85ce61) else Color(0xff67c23a),
            borderColor = if (isPressed) Color(0xff85ce61) else Color(0xff67c23a),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff1c2518),
            borderColor = Color(0xff3e6b27),
            textColor = Color(0xff67c23a)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff529b2e) else Color(0xff67c23a),
            borderColor = if (isPressed) Color(0xff529b2e) else Color(0xff67c23a),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xfff0f9eb),
            borderColor = Color(0xffb3e19d),
            textColor = Color(0xff67c23a)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff85ce61) else Color(0xff4e8e2f),
            borderColor = if (isPressed) Color(0xff85ce61) else Color(0xff4e8e2f),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff67c23a),
            borderColor = Color(0xff67c23a),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff529b2e) else Color(0xff95d475),
            borderColor = if (isPressed) Color(0xff529b2e) else Color(0xff95d475),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xff67c23a),
            borderColor = Color(0xff67c23a),
            textColor = Color.White
          )
        }
      }
    }

    ZColorType.INFO -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff909399),
            borderColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff909399),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff202121),
            borderColor = Color(0xff525457),
            textColor = Color(0xff909399)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff73767a) else Color(0xff909399),
            borderColor = if (isPressed) Color(0xff73767a) else Color(0xff909399),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xfff4f4f5),
            borderColor = Color(0xffc8c9cc),
            textColor = Color(0xff909399)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff6b6d71),
            borderColor = if (isPressed) Color(0xffa6a9ad) else Color(0xff6b6d71),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff909399),
            borderColor = Color(0xff909399),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xff73767a) else Color(0xffb1b3b8),
            borderColor = if (isPressed) Color(0xff73767a) else Color(0xffb1b3b8),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xff909399),
            borderColor = Color(0xff909399),
            textColor = Color.White
          )
        }

      }
    }

    ZColorType.WARNING -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffebb563) else Color(0xffe6a23c),
            borderColor = if (isPressed) Color(0xffebb563) else Color(0xffe6a23c),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff292218),
            borderColor = Color(0xff7d5b28),
            textColor = Color(0xffe6a23c)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffb88230) else Color(0xffe6a23c),
            borderColor = if (isPressed) Color(0xffb88230) else Color(0xffe6a23c),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xfffdf6ec),
            borderColor = Color(0xfff3d19e),
            textColor = Color(0xffe6a23c)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffebb563) else Color(0xffa77730),
            borderColor = if (isPressed) Color(0xffebb563) else Color(0xffa77730),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xffe6a23c),
            borderColor = Color(0xffe6a23c),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffb88230) else Color(0xffeebe77),
            borderColor = if (isPressed) Color(0xffb88230) else Color(0xffeebe77),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xffe6a23c),
            borderColor = Color(0xffe6a23c),
            textColor = Color.White
          )
        }
      }
    }

    ZColorType.DANGER -> {
      if (isPlain) {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xfff78989) else Color(0xfff56c6c),
            borderColor = if (isPressed) Color(0xfff78989) else Color(0xfff56c6c),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xff2b1d1d),
            borderColor = Color(0xff854040),
            textColor = Color(0xfff56c6c)
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffc45656) else Color(0xfff56c6c),
            borderColor = if (isPressed) Color(0xffc45656) else Color(0xfff56c6c),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
            backgroundColor = Color(0xfffef0f0),
            borderColor = Color(0xfffab6b6),
            textColor = Color(0xfff56c6c)
          )
        }
      } else {
        when {
          // 暗黑模式下悬停状态
          isDarkTheme && isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xfff78989) else Color(0xffb25252),
            borderColor = if (isPressed) Color(0xfff78989) else Color(0xffb25252),
            textColor = Color.White
          )
          // 暗黑模式下非悬停状态
          isDarkTheme -> ButtonStyle(
            backgroundColor = Color(0xfff56c6c),
            borderColor = Color(0xfff56c6c),
            textColor = Color.White
          )
          // 悬停状态
          isHovered -> ButtonStyle(
            backgroundColor = if (isPressed) Color(0xffc45656) else Color(0xfff89898),
            borderColor = if (isPressed) Color(0xffc45656) else Color(0xfff89898),
            textColor = Color.White
          )
          // 非悬停状态
          else -> ButtonStyle(
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
  private val ButtonHorizontalPadding = 8.dp

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

