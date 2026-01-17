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
   * An installation of the JetBrains Runtime is required: Launching Compose Hot Reload with the Kotlin Multiplatform IDE plugin will re-use IntelliJ's installation of the JetBrains Runtime.
   * If you want Gradle to automatically download the JetBrains Runtime, add the following code to your settings.gradle.kts file
   */
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
