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
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
      packageName = "ZUtil"
      packageVersion = "1.0.0"
      vendor = "duanluan"
      description = "ZUtil Desktop Toolkit"
      copyright = "© 2026 duanluan. All rights reserved."

      // includeAllModules = true
      // 显式包含需要的模块
      modules(
        // gradlew suggestRuntimeModules
        "java.instrument", "java.scripting", "java.sql", "jdk.unsupported"
        // 报错 Failed to launch JVM，异常写入日志查询到
        , "jdk.accessibility"
      )

      windows {
        // 开启开始菜单图标
        menu = true
        // 开启桌面快捷方式
        shortcut = true
        // 指定开始菜单的文件夹名称 (可选)
        menuGroup = "ZUtil"
        // 是否仅为当前用户安装 (true=不需要管理员权限安装到 AppData, false=安装到 Program Files)
        // perUserInstall = true
        iconFile.set(project.file("src/desktopMain/resources/icon.ico"))

        // 设置一个固定的升级 UUID，如果不设置，每次打包生成的 UUID 都不同，导致新版本无法覆盖旧版本，而是会变成两个程序
        upgradeUuid = "29329738-2032-4752-9599-234277218322"
      }

      linux {
        // 开启桌面快捷方式 (取决于桌面环境，如 GNOME 默认禁用桌面图标，KDE 会显示)
        shortcut = true
        // 指定菜单分类 (例如 Utility, Development 等)
        appCategory = "Utility"
        // 菜单组名称
        menuGroup = "ZUtil"
        iconFile.set(project.file("src/desktopMain/resources/icon.png"))
      }

      macOS {
        // 设置 Dock 栏显示的名称
        dockName = "ZUtil"
        // 设置 Bundle ID (防止与同名应用冲突)
        bundleID = "top.zhjh.zutil-desktop"
        iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
      }
    }
  }
}

// https://github.com/JetBrains/compose-hot-reload?tab=readme-ov-file#configure-the-main-class
tasks.withType<ComposeHotRun>().configureEach {
  mainClass.set("top.zhjh.MainKt")
}
