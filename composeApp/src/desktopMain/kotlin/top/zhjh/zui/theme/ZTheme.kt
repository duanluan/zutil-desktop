package top.zhjh.zui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val defaultTypography = Typography(
  // 正文
  body1 = TextStyle(fontSize = 14.sp),
  // 辅助文本
  body2 = TextStyle(fontSize = 12.sp),
)

@Composable
fun ZTheme(
  isDarkTheme: Boolean = isSystemInDarkTheme(),
  typography: Typography = defaultTypography,
  defaultFontFamily: FontFamily? = null,
  content: @Composable () -> Unit
) {
  // 合并 typography 和 defaultTypography
  val finalTypography = Typography(
    defaultFontFamily = defaultFontFamily ?: FontFamily.Default,
    body1 = if (typography.body1.fontSize == TextStyle().fontSize) defaultTypography.body1 else typography.body1,
    body2 = if (typography.body2.fontSize == TextStyle().fontSize) defaultTypography.body2 else typography.body2,
  )

  // 使用自定义的主题提供者
  ThemeOverrideProvider(darkThemeOverride = isDarkTheme) {
    // 使用自定义的函数而不是 isSystemInDarkTheme()
    val isDark = isAppInDarkTheme()

    // 使用确定的主题值设置 MaterialTheme
    MaterialTheme(
      colors = if (isDark) {
        darkColors(
          background = Color(0xff141414),
          primary = Color(0xff414243)

        )
      } else {
        lightColors(
          background = Color.White,
          primary = Color(0xffe4e7ed)
        )
      },
      typography = finalTypography,
      content = content
    )
  }
}
