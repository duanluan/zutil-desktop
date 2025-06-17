package top.zhjh.zui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val typography = Typography(
  // 正文
  body1 = TextStyle(fontSize = 14.sp),
  // 辅助文本
  body2 = TextStyle(fontSize = 12.sp),
)

@Composable
fun ZTheme(
  isDarkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  // 使用自定义的主题提供者
  ThemeOverrideProvider(darkThemeOverride = isDarkTheme) {
    // 使用自定义的函数而不是 isSystemInDarkTheme()
    val isDark = isAppInDarkTheme()

    // 使用确定的主题值设置 MaterialTheme
    MaterialTheme(
      colors = if (isDark) {
        darkColors(
          background = Color(0xff141414)
        )
      } else {
        lightColors(
          background = Color.White
        )
      },
      typography = typography,
      content = content
    )
  }
}
