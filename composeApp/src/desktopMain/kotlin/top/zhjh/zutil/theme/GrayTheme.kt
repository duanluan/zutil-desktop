package top.zhjh.zutil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GrayLightColorPalette = lightColors(
  primary = Color(0xff6b798e),
)

private val GrayDarkColorPalette = darkColors(
  primary = Color(0xff2c2f3b),
)

@Composable
fun GrayTheme(content: @Composable () -> Unit) {
  // 检测系统是否为夜间模式
  val isDarkTheme = isSystemInDarkTheme()

  // 根据系统模式选择主题
  val colors = if (isDarkTheme) {
    GrayDarkColorPalette
  } else {
    GrayLightColorPalette
  }

  MaterialTheme(
    colors = colors,
    content = content
  )
}
