# ZUtil Desktop

基于 [ZUtil](https://github.com/duanluan/zutil) 的 Compose Multiplatform 桌面端工具箱。

## 功能

- **时间戳工具**：支持当前时间戳获取、时间戳与日期时间的相互转换。
- **UI 组件库预览**：内置自定义组件（按钮、输入框、卡片等）的展示与交互测试。
- **多主题支持**：支持日间/夜间模式切换（Dark/Light Theme）。

## 技术栈

- **语言**：Kotlin 2.2.20
- **UI 框架**：Compose Multiplatform 1.10.0
- **构建工具**：Gradle
- **热重载**：Compose Hot Reload

## 开发指南

### 热启动 (Hot Reload)

本项目集成了 Compose Hot Reload 功能，支持在不重启应用的情况下实时预览代码变更。

**自动热重载模式（推荐）：**
修改代码并保存后，应用会自动刷新 UI。

```bash
./gradlew :composeApp:hotRunDesktop --auto
```

### 升级 Gradle

如果需要升级项目的 Gradle Wrapper 版本，请执行以下命令：

```bash
# https://gradle.org/releases/
./gradlew wrapper --gradle-version 8.10.2
```
