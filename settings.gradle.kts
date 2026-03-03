rootProject.name = "zutil-desktop"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val useLocalZuiComposeDesktop = providers
  .gradleProperty("useLocalZuiComposeDesktop")
  .map { it.equals("true", ignoreCase = true) }
  .orElse(false)
  .get()
val localZuiComposeDesktopDir = providers
  .gradleProperty("localZuiComposeDesktopDir")
  .orElse("../zui-compose-desktop")
  .get()

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
    mavenLocal()
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

if (useLocalZuiComposeDesktop) {
  val localZuiComposeDesktop = file(localZuiComposeDesktopDir)
  if (localZuiComposeDesktop.isDirectory) {
    includeBuild(localZuiComposeDesktop) {
      dependencySubstitution {
        substitute(module("top.zhjh:zui-compose-desktop")).using(project(":zui"))
      }
    }
    println("Using local zui-compose-desktop from: ${localZuiComposeDesktop.absolutePath}")
  } else {
    println("Local zui-compose-desktop not found at: ${localZuiComposeDesktop.absolutePath}, fallback to remote dependency.")
  }
}

plugins {
  /**
   * An installation of the JetBrains Runtime is required: Launching Compose Hot Reload with the Kotlin Multiplatform IDE plugin will re-use IntelliJ's installation of the JetBrains Runtime.
   * If you want Gradle to automatically download the JetBrains Runtime, add the following code to your settings.gradle.kts file
   */
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
