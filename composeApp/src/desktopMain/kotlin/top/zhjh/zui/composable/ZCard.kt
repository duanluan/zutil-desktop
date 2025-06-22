package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.zhjh.zui.enums.ZCardShadow
import top.zhjh.zui.theme.isAppInDarkTheme

/**
 * 卡片
 *
 * @param shadow 阴影出现的时机
 * @param contentPadding 内容内边距
 * @param modifier 修饰符
 * @param content 内容
 */
@Composable
fun ZCard(
  shadow: ZCardShadow = ZCardShadow.ALWAYS,
  contentPadding: PaddingValues = ZCardDefaults.ContentPadding,
  modifier: Modifier? = null,
  content: @Composable () -> Unit
) {
  // 是否为暗黑模式
  val isDarkTheme = isAppInDarkTheme()
  // 交互源，跟踪组件的交互状态
  val interactionSource = remember { MutableInteractionSource() }
  // 是否悬停
  val isHovered by interactionSource.collectIsHoveredAsState()

  // 根据shadow参数和悬停状态决定是否显示阴影
  val shouldShowShadow = when (shadow) {
    ZCardShadow.ALWAYS -> true
    ZCardShadow.HOVER -> isHovered
    ZCardShadow.NEVER -> false
  }

  // 获取样式
  val cardStyle = getZCardStyle(isDarkTheme)

  Surface(
    modifier = Modifier
      .then(
        if (shouldShowShadow) {
          Modifier.shadow(
            shape = ZCardDefaults.Shape,
            elevation = ZCardDefaults.ShadowElevation,
            ambientColor = ZCardDefaults.ShadowAmbientColor,
            spotColor = ZCardDefaults.ShadowSpotColor
          )
        } else Modifier
      )
      .background(color = cardStyle.backgroundColor)
      .border(1.dp, cardStyle.borderColor, shape = ZCardDefaults.Shape)
      .then(modifier ?: Modifier)
      .hoverable(interactionSource),
    contentColor = cardStyle.textColor,
    shape = ZCardDefaults.Shape,
    // interactionSource = interactionSource,
  ) {
    Column(
      modifier = Modifier.padding(contentPadding)
    ) {
      content()
    }
  }
}

/**
 * ZCard 样式类
 */
private data class ZCardStyle(
  var backgroundColor: Color,
  var borderColor: Color,
  var textColor: Color
)

/**
 * 获取卡片样式
 *
 *
 */
private fun getZCardStyle(isDarkTheme: Boolean): ZCardStyle {
  return ZCardStyle(
    backgroundColor = if (isDarkTheme) Color(0xff1d1e1f) else Color.Transparent,
    borderColor = if (isDarkTheme) Color(0xff414243) else Color(0xffe4e7ed),
    textColor = if (isDarkTheme) Color(0xffe5eaf3) else Color(0xff3e3f41)
  )
}

object ZCardDefaults {
  /**
   * 内边距
   */
  val ContentPadding = PaddingValues(16.dp)

  /**
   * 圆角形状
   */
  val Shape = RoundedCornerShape(4.dp)

  /**
   * 阴影高度
   */
  val ShadowElevation = 12.dp

  /**
   *  环境光阴影颜色  rgba(0, 0, 0, 0.12)
   */
  val ShadowAmbientColor = Color(0x1F000000)

  /**
   * 点光源阴影颜色 rgba(0, 0, 0, 0.12)
   */
  val ShadowSpotColor = Color(0x1F000000)
}
