package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
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
 * @param modifier 卡片容器修饰符（设置背景色、宽高、外边距等）
 * @param headerModifier 卡片头部修饰符
 * @param bodyModifier 卡片内容区域修饰符
 * @param footerModifier 卡片底部修饰符
 * @param shadow 阴影出现的时机 [ZCardShadow.ALWAYS], [ZCardShadow.HOVER], [ZCardShadow.NEVER]
 * @param header 卡片标题区域 (可选)
 * @param footer 卡片底部区域 (可选)
 * @param contentPadding 内容内边距
 * @param content 内容
 */
@Composable
fun ZCard(
  modifier: Modifier = Modifier,
  headerModifier: Modifier = Modifier,
  bodyModifier: Modifier = Modifier,
  footerModifier: Modifier = Modifier,
  shadow: ZCardShadow = ZCardShadow.ALWAYS,
  header: (@Composable () -> Unit)? = null,
  footer: (@Composable () -> Unit)? = null,
  contentPadding: PaddingValues = ZCardDefaults.ContentPadding,
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
      // 默认背景色，可以被传入的 modifier 中的 background 覆盖
      .background(color = cardStyle.backgroundColor)
      .border(1.dp, cardStyle.borderColor, shape = ZCardDefaults.Shape)
      // 用户传入的 modifier 放在最后，以便覆盖默认样式（如背景色）
      .then(modifier)
      .hoverable(interactionSource),
    contentColor = cardStyle.textColor,
    shape = ZCardDefaults.Shape,
    color = Color.Transparent // Surface 自身设为透明，背景由 Modifier.background 控制
  ) {
    Column {
      // Header 区域
      if (header != null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(ZCardDefaults.HeaderPadding)
            .then(headerModifier) // 应用头部修饰符
        ) {
          header()
        }
        // 分割线
        Divider(color = cardStyle.borderColor, thickness = 1.dp)
      }

      // Body 区域
      Column (
        modifier = Modifier
          .fillMaxWidth()
          .padding(contentPadding)
          .then(bodyModifier) // 应用内容区域修饰符
      ) {
        content()
      }

      // Footer 区域
      if (footer != null) {
        // 分割线
        Divider(color = cardStyle.borderColor, thickness = 1.dp)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(ZCardDefaults.FooterPadding)
            .then(footerModifier) // 应用底部修饰符
        ) {
          footer()
        }
      }
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
    backgroundColor = if (isDarkTheme) Color(0xff1d1e1f) else Color.White,
    borderColor = if (isDarkTheme) Color(0xff414243) else Color(0xffe4e7ed),
    textColor = if (isDarkTheme) Color(0xffe5eaf3) else Color(0xff3e3f41)
  )
}

object ZCardDefaults {
  /**
   * 内容内边距
   */
  val ContentPadding = PaddingValues(20.dp)

  /**
   * 头部内边距
   */
  val HeaderPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)

  /**
   * 底部内边距
   */
  val FooterPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)

  /**
   * 圆角形状
   */
  val Shape = RoundedCornerShape(4.dp)

  /**
   * 阴影高度
   */
  val ShadowElevation = 12.dp

  /**
   * 环境光阴影颜色  rgba(0, 0, 0, 0.12)
   */
  val ShadowAmbientColor = Color(0x1F000000)

  /**
   * 点光源阴影颜色 rgba(0, 0, 0, 0.12)
   */
  val ShadowSpotColor = Color(0x1F000000)
}
