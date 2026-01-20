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
      // 1. 核心 API (替换掉了之前的 maven 依赖)
      implementation(files("libs/sherpa-onnx-v1.12.23.jar"))
      // 2. Windows 原生库
      implementation(files("libs/sherpa-onnx-native-lib-win-x64-v1.12.23.jar"))
      // 3. Linux 原生库
      implementation(files("libs/sherpa-onnx-native-lib-linux-x64-v1.12.23.jar"))
      // 4. Mac 原生库 (如果你能下载到 jar 就加这一行，否则先注释掉)
      // implementation(files("libs/sherpa-onnx-native-lib-osx-aarch64-v1.12.23.jar"))
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
    }
  }
}

// https://github.com/JetBrains/compose-hot-reload?tab=readme-ov-file#configure-the-main-class
tasks.withType<ComposeHotRun>().configureEach {
  mainClass.set("top.zhjh.MainKt")
}
