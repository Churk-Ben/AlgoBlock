# AlgoBlock 项目现状与下一步路线图（基于当前仓库）

> 日期：2026-04-13  
> 范围：以当前仓库源码为准（docs 仅作参考，已存在与现状不一致的描述）。

## 1. 当前项目状态（可交付面）

### 1.1 工程与模块

- 构建：Gradle Kotlin DSL 多模块工程（root：`algoblock`）
- 模块边界：`game-api` ← `game-core` ← `game-gl`
- Java：统一配置 Toolchain=21（见根 [build.gradle.kts](file:///workspace/build.gradle.kts#L14-L24)）

### 1.2 核心闭环（已打通）

- 解析 / 求值 / 判题 / 评分主链路已打通：UI 提交 → `GameCoreService.submit(...)` → 规则校验/解析/求值/判题/评分 → UI 回显
- 前 4 关资源已落地：`game-core/src/main/resources/levels/level-{1..4}.json`
- UI 形态：`game-gl` 为 GLFW + OpenGL 窗口内渲染“终端风网格”，并提供 REPL 式输入（Tab 补全、语法高亮骨架、Enter 提交）

### 1.3 与技术文档的偏差（以代码为准）

- 文档《M3 开发计划》中的 “Current State Analysis” 仍假设仓库为空，但当前已具备完整工程与代码（见 [AlgoBlock_M3_开发计划.md](file:///workspace/docs/v041301/AlgoBlock_M3_%E5%BC%80%E5%8F%91%E8%AE%A1%E5%88%92.md#L13-L35)）
- 《M3 技术现状报告》中对字体渲染的部分描述已过时：当前 `FontAtlas` / `TextRenderer` 代码已走向字形贴图与 OpenGL 绘制的方向，不再是“仅存在性校验”的状态（见 [AlgoBlock_M3_技术现状报告.md](file:///workspace/docs/v041301/AlgoBlock_M3_%E6%8A%80%E6%9C%AF%E7%8E%B0%E7%8A%B6%E6%8A%A5%E5%91%8A.md#L17-L23)）

## 2. 构建/测试现状（本次执行结果）

### 2.1 当前仓库的关键构建约束

- `settings.gradle.kts` 引入 `org.gradle.toolchains.foojay-resolver-convention`（见 [settings.gradle.kts](file:///workspace/settings.gradle.kts#L1-L9)），用于自动解析/下载 JDK toolchain。

### 2.2 在当前执行环境中的阻塞点

- `./gradlew`：
  - 首次执行需要下载 `gradle-9.4.0-bin.zip`，但该环境访问 `https://services.gradle.org` 发生连接超时，导致 wrapper 无法落地。
- 直接使用系统 `gradle`（8.14.4）执行 `:game-core:test`：
  - 在 settings 阶段解析 `org.gradle.toolchains.foojay-resolver-convention:0.8.0` 失败（无法从 Gradle Plugin Portal 拉取），构建在进入任务执行前就终止。

### 2.3 建议的解决方式（择一）

- 推荐（正常开发机/可联网环境）
  - 允许访问 `services.gradle.org`（wrapper）与 Gradle Plugin Portal（`plugins.gradle.org`）以及 Maven Central（依赖）。
  - 使用 `./gradlew :game-core:test` 与 `./gradlew build` 作为标准验证命令。
- 离线/受限网络环境（需要明确决策）
  - 方案 A：不使用 foojay resolver，要求开发机预装 JDK 21，并移除 settings 中的 resolver 插件依赖。
  - 方案 B：保留 resolver，但为 Gradle wrapper/插件/依赖配置可用的企业镜像仓库（或预热缓存目录）。

## 3. 下一步开发路线（建议按优先级推进）

### P0（跑得稳、跨平台能跑）

- `game-gl` 的 LWJGL natives 目前固定为 `natives-windows`，建议改为按 OS 自动选择或提供属性开关，避免 Linux/macOS 直接运行失败（见 [game-gl/build.gradle.kts](file:///workspace/game-gl/build.gradle.kts#L6-L22)）
- 字体路径与资产约定收敛：
  - 当前 `Main.resolveFontPath()` 存在硬编码的本机字体路径候选，建议统一为 `assets/fonts/**` 并在 README 明确（见 [Main.java](file:///workspace/game-gl/src/main/java/com/algoblock/gl/Main.java#L164-L174)）
- 文档对齐：
  - 在 docs 中明确“技术文档是目标设计、现状报告是当前实现、开发计划已执行部分需折叠/标注完成”，避免后续开发被过时段落误导。

### P1（渲染与 UI 可维护性）

- 明确渲染职责分层：将文本渲染的“上传/绘制”职责拆分，避免未来接入 VAO/VBO/Shader 时代价过高（目前 `TextRenderer.draw()` 为空，渲染主要发生在 `upload()` 内）（见 [TextRenderer.java](file:///workspace/game-gl/src/main/java/com/algoblock/gl/renderer/TextRenderer.java#L164-L166)）
- UI 状态机收敛：
  - `TerminalWidget` 目前承担输入、补全展示、结果回显等多职责，建议引入更清晰的 UI state（见 [TerminalWidget.java](file:///workspace/game-gl/src/main/java/com/algoblock/gl/ui/TerminalWidget.java#L16-L95)）

### P2（玩法与内容扩展：M4+）

- 关卡体系增强：
  - 增加关卡选择/切换流程、存档与进度、失败提示分层（词法/语法/规则/运行错误）
- Mod 系统（对齐技术文档目标）：
  - 以 `game-api` 为合约，落地 `mods/` 扫描 + URLClassLoader 加载 + 元数据注册与沙箱限制
- 体验提升：
  - 更完整的编辑体验：光标移动、历史、候选导航、可视化 trace（如步数消耗/中间状态）

## 4. 建议先落地的两项“立刻能产生收益”的任务

- 构建基线稳定化：确认项目在“可联网的开发机”上 `./gradlew :game-core:test` 与 `./gradlew build` 必过，并把其结果固化为 CI（或最小本地脚本）。
- docs 的“现状对齐修订”：将与现状明显冲突的段落标注为“已完成/过时”，并把最新现状集中在一份文档（本文可作为基线）。

