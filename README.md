# ZUtil Desktop

基于 [ZUtil](https://github.com/duanluan/zutil) 与 Compose Multiplatform 的桌面端工具箱项目。  
目标是把常用开发工具与离线 AI 工具集成到一个可跨平台分发的桌面应用中。

## 当前功能

### 常用工具

- **时间戳工具**
  - 当前时间戳（秒/毫秒）实时查看与复制
  - 时间戳 -> 日期时间（支持时区、格式切换与自定义格式）
  - 日期时间 -> 时间戳（支持秒/毫秒输出）
- **JSON 工具**
  - 格式化、压缩、转义/去转义、Unicode 与中文互转
  - 树形视图与源码视图切换
  - 历史记录回填与复制
- **UUID 生成工具**
  - 多版本 UUID 生成（含批量）
  - 多格式输出（String/Hex/Base64/Binary）
  - 大小写与中划线策略控制

### AI 工具

- **语音转文本（离线）**
  - 基于 Sherpa-ONNX 的本地识别流程
  - 支持模型下载、模型目录管理
  - 支持常见音频格式输入（自动重采样/必要时 FFmpeg 转码）

### 应用能力

- **设置页**
  - UI 缩放比例
  - 窗口尺寸与位置记忆开关
  - 模型下载目录设置
- **关于页**
  - 版本信息与更新检查
  - 社区与项目链接

## ZUI 组件库

项目内置了 **ZUI 组件库展示页（ZUI 分类）**，用于预览和验证组件交互。  
你也可以直接查看组件库仓库：

- **zui-compose-desktop**: https://github.com/duanluan/zui-compose-desktop

在 Demo 页面中目前包含：

- 组件库介绍页（仓库入口）
- Text / Button / TextField / Dropdown / Switch
- Radio / Checkbox / Form
- Menu / Card / Link / Container

## 技术栈

- **语言**: Kotlin 2.2.20
- **UI 框架**: Compose Multiplatform 1.10.0
- **构建工具**: Gradle 8.14.3
- **热重载**: Compose Hot Reload

## 开发与运行

### 直接运行

```bash
./gradlew :composeApp:run
```

### 热重载运行（推荐开发时使用）

```bash
./gradlew :composeApp:hotRunDesktop --auto
```

### 编译检查

```bash
./gradlew :composeApp:compileKotlinDesktop
```

### 升级 Gradle Wrapper

```bash
# https://gradle.org/releases/
./gradlew wrapper --gradle-version 8.14.3
```
