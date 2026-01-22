import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeHotReload)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting

    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material)
      implementation(libs.compose.material.icons.extended)
      implementation(libs.compose.ui)
      // commonMain 资源依赖：https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-setup.html#build-script-and-directory-setup
      implementation(libs.compose.components.resources)
      implementation(libs.compose.components.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodel)
      implementation(libs.androidx.lifecycle.runtime.compose)
      implementation(libs.androidx.lifecycle.viewmodel.compose)
    }
    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(libs.icons.feather)
      implementation(libs.zutil.all)
      implementation(libs.zutil.awt)

      implementation(libs.commons.compress)
      // sherpa-onnx
      implementation(files("libs/sherpa-onnx-v1.12.23.jar"))
      // sherpa-onnx native libraries
      implementation(files("libs/sherpa-onnx-native-lib-win-x64-v1.12.23.jar"))
      implementation(files("libs/sherpa-onnx-native-lib-linux-x64-v1.12.23.jar"))
      implementation(files("libs/sherpa-onnx-native-lib-osx-x64-v1.12.21.jar"))
      implementation(files("libs/sherpa-onnx-native-lib-osx-aarch64-v1.12.21.jar"))
    }
  }
}


compose.desktop {
  application {
    mainClass = "top.zhjh.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "top.zhjh"
      packageVersion = "1.0.0"
      description = "ZUtil 桌面工具箱"
      copyright = "© 2026 duanluan. All rights reserved."
    }
  }
}

// https://github.com/JetBrains/compose-hot-reload?tab=readme-ov-file#configure-the-main-class
tasks.withType<ComposeHotRun>().configureEach {
  mainClass.set("top.zhjh.MainKt")
}
