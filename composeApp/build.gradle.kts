import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import java.io.File
import java.util.concurrent.TimeUnit

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.osdetector)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting

    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material)
      implementation(libs.compose.ui)
      // commonMain 资源依赖：https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-setup.html#build-script-and-directory-setup
      implementation(libs.compose.components.resources)
      implementation(libs.androidx.lifecycle.viewmodel)
      implementation(libs.androidx.lifecycle.runtime.compose)
      implementation(libs.androidx.lifecycle.viewmodel.compose)
    }
    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(libs.icons.feather)
      implementation(libs.icons.simple)
      implementation(libs.zutil.all)
      implementation(libs.zutil.awt)
      implementation(libs.commons.compress)

      // sherpa-onnx
      implementation(files("libs/sherpa-onnx-v1.12.23.jar"))
      val classifier = osdetector.classifier
      // sherpa-onnx native libraries
      val nativeJar = when (classifier) {
        "windows-x86_64" -> "libs/sherpa-onnx-native-lib-win-x64-v1.12.23.jar"
        "linux-x86_64" -> "libs/sherpa-onnx-native-lib-linux-x64-v1.12.23.jar"
        "linux-aarch_64" -> "libs/sherpa-onnx-native-lib-linux-aarch64-v1.12.23.jar"
        "osx-x86_64" -> "libs/sherpa-onnx-native-lib-osx-x64-v1.12.21.jar"
        "osx-aarch_64" -> "libs/sherpa-onnx-native-lib-osx-aarch64-v1.12.21.jar"
        else -> error("Unsupported platform: $classifier")
      }
      implementation(files(nativeJar))
    }
  }
}


compose.desktop {
  application {
    mainClass = "top.zhjh.MainKt"

    nativeDistributions {
      targetFormats(
        TargetFormat.Dmg,
        TargetFormat.Msi,
        TargetFormat.Exe,
        TargetFormat.Deb,
        TargetFormat.Rpm,
        TargetFormat.AppImage
      )
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
  // Hot Reload 启动的应用入口类。这里保持和 compose.desktop.application.mainClass 一致，
  // 避免任务之间配置不一致导致“能运行但热重载失效”。
  mainClass.set("top.zhjh.MainKt")

  /**
   * 下面这个 doFirst 是“启动前防残留保护”。
   *
   * 背景：
   * Compose Hot Reload 在 --auto/连续模式下，会再拉起一个 Gradle 持续编译进程，
   * 并把它的 PID 记录在 build/run/desktopMain/desktopMain.gradle.pid。
   *
   * 问题：
   * 如果上一次异常退出（IDE 强停、窗口闪退、连接断开、任务被超时中断），
   * 该持续编译进程可能还活着，或者 pid 文件还留着。
   * 下一次启动会出现：
   * “Previous Gradle process with pid 'xxxx' found; Waiting...”
   * 然后一直卡住等待旧进程结束。
   *
   * 目标：
   * 在每次 hotRunDesktop 启动前，主动检查并清理“当前项目的旧 Hot Reload 进程”及脏 pid 文件，
   * 让启动过程可自愈，不依赖手工 taskkill。
   */
  doFirst {
    // Hot Reload 运行目录（compose-hot-reload 生成 pid/argfile/log 的位置）。
    val runDir = layout.buildDirectory.dir("run/desktopMain").get().asFile

    // 持续编译子进程的 pid 文件。
    val gradlePidFile = File(runDir, "desktopMain.gradle.pid")
    // 没有 pid 文件就说明没有残留线索，直接继续正常启动。
    if (!gradlePidFile.exists()) return@doFirst

    // 读取 pid。使用 toLongOrNull 防止文件损坏（空内容/非数字）导致启动崩溃。
    val pid = gradlePidFile.readText().trim().toLongOrNull()
    if (pid == null) {
      // pid 文件格式非法：删文件并继续，避免后续每次都卡同一处。
      logger.warn("Invalid hot reload pid file, deleting: ${gradlePidFile.absolutePath}")
      gradlePidFile.delete()
      return@doFirst
    }

    // 根据 pid 查系统进程。
    val process = ProcessHandle.of(pid).orElse(null)
    if (process == null || !process.isAlive) {
      // 进程已经不存在，但 pid 文件残留：清理后继续。
      gradlePidFile.delete()
      return@doFirst
    }

    // 进程还活着时，必须做“身份校验”，防止误杀与热重载无关的进程。
    val cmdLine = process.info().commandLine().orElse("")
    val normalizedCmd = cmdLine.lowercase()
    val projectPath = project.projectDir.absolutePath.lowercase()

    // 通过命令行特征判断是否为 Hot Reload 的持续编译任务：
    // 1) 任务名 hotReloadDesktopMain
    // 2) compose.reload.isHotReloadBuild=true 标记
    val looksLikeHotReloadProcess =
      normalizedCmd.contains("hotreloaddesktopmain") ||
        normalizedCmd.contains("compose.reload.ishotreloadbuild=true")

    // 再通过项目绝对路径判定“是否属于当前仓库”，避免多项目并行开发时误伤其它项目。
    val belongsToThisProject = normalizedCmd.contains(projectPath)

    if (looksLikeHotReloadProcess && belongsToThisProject) {
      // 满足“双重校验”才强制结束：
      // 先杀子进程，再杀父进程，避免父进程退出后子进程继续占用资源。
      logger.lifecycle("Killing stale Compose Hot Reload Gradle process: pid=$pid")
      process.descendants().forEach { child ->
        runCatching { child.destroyForcibly() }
      }
      runCatching { process.destroyForcibly() }

      // 最多等待 3 秒让 OS 完成回收，减少后续任务“端口/文件句柄仍占用”的概率。
      runCatching { process.onExit().get(3, TimeUnit.SECONDS) }

      // 清理 pid 文件，防止下一轮再次误判为残留。
      gradlePidFile.delete()
    } else {
      // 不是当前项目的 Hot Reload 进程：只删 pid 文件，不做 kill。
      // 这样做更安全，避免误杀用户其它任务。
      logger.warn("desktopMain.gradle.pid points to another live process (pid=$pid). Deleting pid file only.")
      gradlePidFile.delete()
    }
  }
}
