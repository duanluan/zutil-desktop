plugins {
  // alias 用于引用版本管理文件 libs.versions.toml 中定义的内容，apply false 表示不应用插件（顶级不需要应用）
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  // This prevents the Compose Hot Reload plugin from being loaded multiple times in each of your subprojects.
  alias(libs.plugins.composeHotReload) apply false
}
