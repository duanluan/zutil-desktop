package top.zhjh.zui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// 创建一个 CompositionLocal 来提供主题覆盖值
val LocalThemeOverride = staticCompositionLocalOf { false }

// 创建一个替代的函数来检查当前主题
@Composable
fun isAppInDarkTheme(): Boolean {
  // 优先使用我们的覆盖值，如果没有则回退到系统设置
  return LocalThemeOverride.current
}

// 创建一个提供主题覆盖的组件
@Composable
fun ThemeOverrideProvider(
  darkThemeOverride: Boolean = false,
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(
    LocalThemeOverride provides darkThemeOverride,
    content = content
  )
}
