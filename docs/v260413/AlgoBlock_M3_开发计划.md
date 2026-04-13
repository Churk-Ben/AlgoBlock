# AlgoBlock（M1\~M3）开发计划

## 1. Summary

- 目标：依据《AlgoBlock 技术文档》，在 **JDK 21 + Windows** 环境下，采用 **Gradle 多模块**实现可交付的 M1\~M3 版本。

- 交付边界：覆盖文档中的 M1、M2、M3 能力，达到“前 4 关完整可游玩”的标准（含解析、求值、关卡与评分、基础渲染输入、语法高亮与 Tab 补全）。

- 技术约定：字体固定使用 `assets/fonts` 下 Hack 字体；保持 `game-core` 对 `game-gl` 零依赖；单 JVM、双线程（渲染线程 + 逻辑线程）模型。

- 输出形式：先完成工程骨架和最小闭环，再按里程碑增量实现、测试与验收。

## 2. Current State Analysis

- 仓库当前仅包含：
  - `docs/AlgoBlock_技术文档.md`（完整技术方案）

  - `assets/fonts/`（Hack 字体已就绪）

  - `docs/` (技术文档,开发文档,项目书等)

  - 根目录 `build.gradle.kts`（空文件）

- 现状判断：
  - 尚无 `settings.gradle.kts`、无任何源码目录、无测试目录。

  - 尚未建立 `game-api / game-core / game-gl` 模块与依赖边界。

  - 尚无可执行主程序、关卡 JSON、Shader、输入系统、渲染管线实现。

- 机会点：
  - 文档已给出清晰模块划分、关键类、里程碑与风险，可直接转化为执行任务。

  - 字体资源已具备，可在 M1 直接接入 FontAtlas 验证。

## 3. Proposed Changes

### 3.1 工程与构建（先行基建）

- 文件：
  - `settings.gradle.kts`

  - `build.gradle.kts`（根）

  - `game-api/build.gradle.kts`

  - `game-core/build.gradle.kts`

  - `game-gl/build.gradle.kts`

- 变更内容（What/Why/How）：
  - **What**：建立 Gradle 多模块结构，声明公共仓库、JDK 21 toolchain、测试框架与模块依赖。

  - **Why**：对齐“Gradle 多模块”决策，同时保持文档定义的逻辑分层与可测试性。

  - **How**：
    - `game-api`：纯 JDK，无第三方依赖。

    - `game-core`：依赖 `game-api` + Gson + JUnit5。

    - `game-gl`：依赖 `game-core` + LWJGL（core/glfw/opengl/stb + windows natives）。

    - 根工程统一编码、测试任务与版本约束。

### 3.2 M1：核心模型 + 最小渲染可见

- 文件（新增）：
  - `game-api/src/main/java/...`：`Block`、`NullaryBlock/UnaryBlock/BinaryBlock`、`EvalContext`、`ValidationResult`、`BlockMeta`

  - `game-core/src/main/java/.../blocks/**`：内置积木（按文档目录拆分）

  - `game-core/src/test/java/...`：11+ 个积木行为测试

  - `game-gl/src/main/java/.../renderer/`：`FontAtlas`、`TerminalBuffer`、`TextRenderer`、最小 `Main`

- 变更内容（What/Why/How）：
  - **What**：实现积木基础层级与第一批可验证积木；完成窗口初始化与字符网格绘制闭环。

  - **Why**：M1 验收依赖“积木单测全绿 + 终端可显示文字”。

  - **How**：
    - `EvalContext` 内置 `stepBudget` 与 trace 存储。

    - `TerminalBuffer` 作为 UI 唯一写入对象，不在组件层直接触达 OpenGL。

    - `FontAtlas` 从 `assets/fonts/Hack Regular Nerd Font Complete Mono.ttf` 加载并烘焙 ASCII 字符。

### 3.3 M2：解析求值闭环 + 基础 REPL

- 文件（新增）：
  - `game-core/src/main/java/.../engine/`：`Lexer`、`Parser`、`BlockRegistry`、异常类

  - `game-gl/src/main/java/.../input/`：`InputEvent`、`InputEventQueue`、`KeyMapper`

  - `game-gl/src/main/java/.../ui/`：`TerminalWidget`（最小 REPL）

- 变更内容（What/Why/How）：
  - **What**：实现 `_INPUT_` + `IDENT<...>` 文法解析、反射实例化、提交后懒惰求值。

  - **Why**：M2 目标是“能解析并求值第 1-4 关示例解”。

  - **How**：
    - `Parser` 使用递归下降，构建 Block 树后统一 `validate()`。

    - `BlockRegistry` 先支持内置注册，保留 Mod 扫描接口骨架（M5 扩展）。

    - 渲染线程仅采集输入并投递队列，逻辑线程消费事件并刷新 `TerminalBuffer`。

### 3.4 M3：关卡与评分 + 高亮补全

- 文件（新增）：
  - `game-core/src/main/java/.../levels/`：`Level`、`LevelLoader`、`LevelRegistry`

  - `game-core/src/main/java/.../engine/`：`Judge`、评分计算器

  - `game-core/src/main/resources/levels/*.json`：前 4 关

  - `game-gl/src/main/java/.../ui/`：`SyntaxHighlighter`、`Completer`

- 变更内容（What/Why/How）：
  - **What**：落地关卡 JSON、可用积木限制、星级判定、补全浮层和语法高亮。

  - **Why**：M3 验收是“前 4 关完整可游玩”。

  - **How**：
    - 关卡字段严格对齐文档 schema（含 `available_blocks`、`forced_blocks`、`optimal_size`、`time_par`、`step_budget`）。

    - Tab 补全仅展示当前关允许积木；`prefix` 匹配来自注册表元数据。

    - 评分分离为独立服务，便于后续扩展隐藏星与统计。

### 3.5 质量与交付（贯穿）

- 文件（新增）：
  - `README.md`（运行方式、模块说明、按里程碑验收）

  - `docs/` 下补充 `开发日志` 与 `验收清单`（如需要）

- 变更内容（What/Why/How）：
  - **What**：建立统一验证标准与最小文档，保证迭代可追踪。

  - **Why**：当前仓库是空白状态，需要把“能跑、能测、能验收”固化为标准流程。

  - **How**：
    - 每里程碑提供“功能验收 + 自动化测试 + 手工运行截图/记录”。

    - 保持 `game-core` 单元测试优先，`game-gl` 以启动与渲染烟雾测试为主。

## 4. Assumptions & Decisions

- 已确认决策：
  - 范围：首个可交付版本到 **M3**。

  - 构建：**Gradle 多模块**。

  - 环境：**JDK 21 + Windows**。

  - 字体：使用仓库中 **Hack** 字体。

  - 资源输出：计划中需包含“必需 + 建议资源”。

- 关键假设：
  - 本地显卡/驱动可支持 OpenGL 4.3 Core Profile（若不足，需软件渲染兜底验证）。

  - 可使用 Gradle Wrapper（若无外网，需离线依赖镜像或本地缓存支持）。

  - M1\~M3 不引入 Mod 热加载与特效层高阶内容（对应 M5+）。

## 5. Verification Steps

- M1 验证：
  - `game-core`：积木行为测试与 `EvalContext` 预算边界测试全部通过。

  - `game-gl`：窗口可打开、终端网格可稳定渲染 ASCII 文本。

- M2 验证：
  - 解析器可正确建树并通过 `validate()`。

  - 示例表达式（第 1-4 关）可从输入字符串走到正确求值结果。

  - 输入事件队列在高频输入下无阻塞崩溃。

- M3 验证：
  - 关卡 JSON 可加载，限制规则生效（可用/强制积木）。

  - `Judge` 与星级判定正确。

  - 语法高亮与 Tab 补全可用，且候选仅来自本关允许积木。

  - 手工通关前 4 关，形成验收记录。

## 6. 你可提供的资源（必需 + 建议）

- 必需资源（会阻塞开发）：
  - 本机 Java 21 可用性确认（`java -version`）。

  - OpenGL 4.3 能力确认（显卡型号/驱动是否满足）。

  - 是否允许联网拉取 Gradle/LWJGL 依赖；如不允许，请提供可用镜像或离线仓库路径。

- 建议资源（提升效率）：
  - 你期望的包名前缀（如 `com.algoblock`）与最终产物命名。\
    answer: 就这个前缀不错.(`com.algoblock`)

  - 第 1-4 关故事文本最终稿（可直接放入关卡 JSON）。\
    answer: 这个暂时没有

  - UI 主题偏好（暗色默认是否固定、是否需要可配置）。\
    answer: UI就深色模式就好
