rootProject.name = "zutil-desktop"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    mavenLocal()
  }
}

plugins {
  /**
   * 使用 Foojay Disco API 配置 Gradle 工具链，此处用于下载 JetBrains Runtime（JBR）
   *
   * https://github.com/JetBrains/compose-hot-reload?tab=readme-ov-file#set-up-automatic-provisioning-of-the-jetbrains-runtime-jbr-via-gradle
   * https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention
   *
   * 因为 libs 版本目录在 settings 后初始化，所以此处不能用 alias(libs.plugins.xxx) 的写法
   */
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

include(":composeApp")
