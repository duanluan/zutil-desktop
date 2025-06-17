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
 * 自定义按钮组件 ZButton
 *
 * @param onClick 点击按钮时触发的回调函数
 * @param modifier 应用于按钮的修饰符，默认为空
 * @param contentPadding 按钮内容区域的内边距，默认为 8.dp
 * @param enabled 按钮是否启用，默认为 true（启用）
 * @param icon 按钮内显示的图标，可选参数
 * @param iconDescription 图标的描述文本，用于无障碍功能，可选参数
 * @param content 按钮内容区域的可组合函数，可选参数
 */
@Composable
fun ZButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(8.dp),
  type: ZColorType = ZColorType.DEFAULT,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  iconDescription: String? = null,
  content: (@Composable RowScope.() -> Unit)? = null
) {
  // 圆角半径
  val shape = RoundedCornerShape(4.dp)
  // 可交换源，监听鼠标悬停状态
  val interactionSource = remember { MutableInteractionSource() }
  // 是否悬停状态
  val isHovered by interactionSource.collectIsHoveredAsState()
  // 是否点击状态
  val isPressed by interactionSource.collectIsPressedAsState()
  // 判断是否为暗黑模式
  val isDarkTheme = isAppInDarkTheme()

  // 获取按钮样式
  val buttonStyle = getButtonStyle(type, isHovered, isPressed, isDarkTheme)

  Box(
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
      ),

    ) {

    // 提供内容颜色的上下文，使所有子组件继承此颜色
    CompositionLocalProvider(LocalContentColor provides buttonStyle.textColor) {
      Row(
        modifier = Modifier.defaultMinSize(minHeight = 32.dp).padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (icon != null) {
          Icon(
            modifier = Modifier
              .then(
                // 有内容时添加右边距
                if (content != null) Modifier.padding(end = 2.dp)
                else Modifier
              )
              .size(14.dp),
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
private fun getButtonStyle(type: ZColorType, isHovered: Boolean, isPressed: Boolean, isDarkTheme: Boolean): ButtonStyle {
  return when (type) {
    ZColorType.DEFAULT -> {
      when {
        // 暗黑模式下悬停状态
        isDarkTheme && isHovered -> ButtonStyle(
          backgroundColor = Color(0xFF18222c),
          borderColor = if (isPressed) Color(0xFF409eff) else Color(0xFF213d5b),
          textColor = Color(0xFF409eff)
        )
        // 暗黑模式下非悬停状态
        isDarkTheme -> ButtonStyle(
          backgroundColor = Color.Transparent,
          borderColor = Color(0xFF4c4d4f),
          textColor = Color(0xFFcfd3dc)
        )
        // 悬停状态
        isHovered -> ButtonStyle(
          backgroundColor = Color(0xFFECF5FF),
          borderColor = if (isPressed) Color(0xFF409eff) else Color(0xFFC6E2FF),
          textColor = Color(0xFF409EFF)
        )
        // 非悬停状态
        else -> ButtonStyle(
          backgroundColor = Color.Transparent,
          borderColor = Color(0xFFDCDDE6),
          textColor = Color(0xFF606266)
        )
      }
    }

    ZColorType.PRIMARY -> {
      when {
        // 暗黑模式下悬停状态
        isDarkTheme && isHovered -> ButtonStyle(
          backgroundColor = if (isPressed) Color(0xFF66b1ff) else Color(0xFF3375b9),
          borderColor = if (isPressed) Color(0xFF66b1ff) else Color(0xFF3375b9),
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
          backgroundColor = if (isPressed) Color(0xFF337ecc) else Color(0xFF79bbff),
          borderColor = if (isPressed) Color(0xFF337ecc) else Color(0xFF79bbff),
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

    ZColorType.SUCCESS -> {
      when {
        // 暗黑模式下悬停状态
        isDarkTheme && isHovered -> ButtonStyle(
          backgroundColor = if (isPressed) Color(0xFF85ce61) else Color(0xFF4e8e2f),
          borderColor = if (isPressed) Color(0xFF85ce61) else Color(0xFF4e8e2f),
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
          backgroundColor = if (isPressed) Color(0xFF529b2e) else Color(0xFF95d475),
          borderColor = if (isPressed) Color(0xFF529b2e) else Color(0xFF95d475),
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

    ZColorType.INFO -> {
      when {
        // 暗黑模式下悬停状态
        isDarkTheme && isHovered -> ButtonStyle(
          backgroundColor = if (isPressed) Color(0xFFa6a9ad) else Color(0xFF6b6d71),
          borderColor = if (isPressed) Color(0xFFa6a9ad) else Color(0xFF6b6d71),
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
          backgroundColor = if (isPressed) Color(0xFF73767a) else Color(0xFFb1b3b8),
          borderColor = if (isPressed) Color(0xFF73767a) else Color(0xFFb1b3b8),
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

    ZColorType.WARNING -> {
      when {
        // 暗黑模式下悬停状态
        isDarkTheme && isHovered -> ButtonStyle(
          backgroundColor = if (isPressed) Color(0xFFebb563) else Color(0xFFa77730),
          borderColor = if (isPressed) Color(0xFFebb563) else Color(0xFFa77730),
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
          backgroundColor = if (isPressed) Color(0xFFb88230) else Color(0xFFeebe77),
          borderColor = if (isPressed) Color(0xFFb88230) else Color(0xFFeebe77),
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

    ZColorType.DANGER -> {
      when {
        // 暗黑模式下悬停状态
        isDarkTheme && isHovered -> ButtonStyle(
          backgroundColor = if (isPressed) Color(0xFFf78989) else Color(0xFFb25252),
          borderColor = if (isPressed) Color(0xFFf78989) else Color(0xFFb25252),
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
          backgroundColor = if (isPressed) Color(0xFFc45656) else Color(0xFFf89898),
          borderColor = if (isPressed) Color(0xFFc45656) else Color(0xFFf89898),
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
