# AlgoBlock M3 技术现状报告（v260413）

## 1. 报告目的与范围

本报告基于仓库当前实现状态编写，目标是为后续 AI 开发提供可直接开工的低上下文入口，减少重复召回以下信息：

- 工程结构与依赖边界
- 核心执行链路（解析 -> 求值 -> 判题 -> 评分 -> UI）
- 已实现能力与未实现能力
- 关键代码索引与高频变更点
- 运行/验证方式与已知风险

范围覆盖当前 `M1~M3` 已落地内容，不扩展到 `M4+` 目标设计。

## 2. 当前结论（Executive Summary）

- 项目已从空仓状态演进为可构建、可运行、可测试的 Gradle 多模块工程。
- `game-core` 的解析、求值、关卡加载、规则校验、评分流程均已打通。
- `game-gl` 可创建窗口、采集输入、渲染终端文本，并显示关卡与提交结果。
- 前 4 关 JSON 已落地，支持可用积木限制与强制积木限制。
- 当前图形层属于原型实现：文本绘制基于 `STBEasyFont`，`FontAtlas` 仅做字体存在性校验，尚未形成完整字形纹理管线。

## 3. 工程与构建基线

### 3.1 模块结构

- 根工程：`algoblock`
- 子模块：
  - `game-api`：积木抽象与执行上下文
  - `game-core`：积木实现、解析器、关卡、判题评分
  - `game-gl`：GLFW 窗口、输入事件、终端渲染与交互

### 3.2 构建配置

- Gradle：多模块 Kotlin DSL。
- Java：Toolchain 固定 21。
- 依赖源：`mavenCentral()`。
- Toolchain Resolver：已启用 `org.gradle.toolchains.foojay-resolver-convention`。

### 3.3 关键依赖

- `game-core`：
  - `api(project(":game-api"))`
  - `com.google.code.gson:gson:2.10.1`
  - JUnit 5 + JUnit Platform Launcher
- `game-gl`：
  - `implementation(project(":game-core"))`
  - LWJGL 3.3.4：`lwjgl` / `glfw` / `opengl` / `stb`
  - Windows natives：`natives-windows`

### 3.4 已验证命令

- 核心测试：`gradle :game-core:test`
- 全量构建：`gradle build`
- 图形运行：`gradle :game-gl:run`

## 4. 架构边界与职责

### 4.1 模块依赖方向

- `game-api` <- `game-core` <- `game-gl`
- 禁止反向依赖：`game-core` 不依赖 `game-gl`。

### 4.2 核心职责切分

- `game-api`：
  - 定义积木模型：`Block` / `UnaryBlock` / `BinaryBlock`
  - 提供执行上下文：`EvalContext`（输入、step budget、trace、vars）
  - 提供校验结果与异常语义
- `game-core`：
  - 提供积木注册与实例化：`BlockRegistry`
  - 实现解析器：`Lexer` + `Parser`
  - 实现关卡加载与规则：`LevelLoader` + `LevelRules`
  - 实现判题评分：`Judge` + `Scorer`
  - 对外统一提交入口：`GameCoreService`
- `game-gl`：
  - 主循环与窗口生命周期：`Main`
  - 输入采集与队列：`InputEventQueue`
  - UI 交互编排：`TerminalWidget`
  - 缓冲与文本绘制：`TerminalBuffer` + `TextRenderer`

## 5. 关键执行链路

### 5.1 提交判题链路

1. `TerminalWidget` 收到 Enter 事件。
2. 读取当前表达式字符串与用时，调用 `GameCoreService.submit(...)`。
3. `LevelRules` 先做规则预检：
   - 只能使用 `available_blocks`
   - 必须包含 `forced_blocks`
4. `Parser.parse(...)` 生成 Block 树并调用 `validate()`。
5. 以 `EvalContext(input, stepBudget)` 执行 `root.evaluate(ctx)`。
6. `Judge.check(...)` 比较结果与关卡期望输出。
7. `Scorer.score(...)` 计算 correctness/minimal/speed 与星级。
8. 返回 `SubmissionResult`，UI 渲染 AC/WA 与星级。

### 5.2 图形渲染链路

1. `Main` 初始化 GLFW + OpenGL 上下文。
2. 主线程循环：
   - 查询 framebuffer 尺寸
   - 清屏
   - `TextRenderer.upload(buffer)` 绘制文本
   - 交换缓冲并轮询事件
3. 逻辑线程循环：
   - 从 `InputEventQueue.take()` 阻塞取事件
   - 调用 `TerminalWidget.onEvent(event)` 更新 `TerminalBuffer`
4. 文本渲染目前基于 `STBEasyFont` 即时生成顶点。

## 6. 语法与数据约定

### 6.1 表达式语法（当前实现）

- 标识符积木：`IDENT`
- 数字常量：`NUMBER`（解析为 `ConstIntBlock`）
- 泛型式参数：
  - 一元：`A<B>`
  - 二元：`A<B,C>`
- 示例：
  - `Array<PopEach<PrioQueue<_INPUT_>>>`
  - `Map<_INPUT_><DoubleOp>`

说明：当前 `Parser` 对语法容错较弱，按递归下降严格匹配 token，不做错误恢复。

### 6.2 关卡 JSON（已落地字段）

- `schema_version`
- `id`
- `title`
- `story`
- `input`
- `output`
- `available_blocks`
- `forced_blocks`
- `bonus_combos`
- `optimal_size`
- `time_par`
- `step_budget`
- `discovery_hint`

## 7. 已实现能力清单（按里程碑）

### 7.1 M1

- 积木抽象与上下文模型已实现。
- 基础积木与集合/变换类积木已实现并可执行。
- `game-gl` 窗口可启动并绘制终端文本。

### 7.2 M2

- `Lexer` + `Parser` + `BlockRegistry` 已打通。
- 支持 `_INPUT_` 与嵌套表达式求值。
- 输入系统采用事件队列，渲染线程与逻辑线程解耦。

### 7.3 M3

- 关卡加载、规则校验、判题、评分已实现。
- 前 4 关 JSON 已提供并可跑通。
- 语法高亮与 Tab 补全已可用。

## 8. 关键文件索引（高频开发入口）

### 8.1 构建与根配置

- `settings.gradle.kts`
- `build.gradle.kts`
- `game-core/build.gradle.kts`
- `game-gl/build.gradle.kts`

### 8.2 API 层

- `game-api/src/main/java/com/algoblock/api/Block.java`
- `game-api/src/main/java/com/algoblock/api/EvalContext.java`
- `game-api/src/main/java/com/algoblock/api/UnaryBlock.java`
- `game-api/src/main/java/com/algoblock/api/BinaryBlock.java`

### 8.3 Core 执行层

- `game-core/src/main/java/com/algoblock/core/engine/GameCoreService.java`
- `game-core/src/main/java/com/algoblock/core/engine/Parser.java`
- `game-core/src/main/java/com/algoblock/core/engine/BlockRegistry.java`
- `game-core/src/main/java/com/algoblock/core/engine/LevelRules.java`
- `game-core/src/main/java/com/algoblock/core/engine/Judge.java`
- `game-core/src/main/java/com/algoblock/core/engine/Scorer.java`
- `game-core/src/main/java/com/algoblock/core/levels/LevelLoader.java`

### 8.4 GL 交互与渲染

- `game-gl/src/main/java/com/algoblock/gl/Main.java`
- `game-gl/src/main/java/com/algoblock/gl/ui/TerminalWidget.java`
- `game-gl/src/main/java/com/algoblock/gl/ui/SyntaxHighlighter.java`
- `game-gl/src/main/java/com/algoblock/gl/ui/Completer.java`
- `game-gl/src/main/java/com/algoblock/gl/renderer/TerminalBuffer.java`
- `game-gl/src/main/java/com/algoblock/gl/renderer/TextRenderer.java`
- `game-gl/src/main/java/com/algoblock/gl/input/InputEventQueue.java`

### 8.5 测试入口

- `game-core/src/test/java/com/algoblock/core/ParserTest.java`
- `game-core/src/test/java/com/algoblock/core/BlocksTest.java`
- `game-core/src/test/java/com/algoblock/core/LevelLoaderTest.java`
- `game-core/src/test/java/com/algoblock/core/GameCoreServiceTest.java`

## 9. 当前行为特征与实现细节

### 9.1 规则校验特征

- `usesOnlyAvailableBlocks` 使用正则提取标识符并逐个匹配可用集合。
- `containsForcedBlocks` 为字符串包含判断，不是 AST 级匹配。

影响：

- 当积木名发生子串重叠时，`containsForcedBlocks` 可能出现误判边界。
- 规则校验在 parse 前执行，可快速拒绝非法输入。

### 9.2 排序相关积木现状

- `SortBlock` 与 `PrioQueueBlock` 当前都通过 `Comparator.naturalOrder()` 对 `Collection` 排序后返回 `List`。
- `PrioQueueBlock` 在行为上尚未模拟真实堆逐次弹出过程，当前结果与 `SortBlock` 接近。

### 9.3 图形层实现特征

- `TextRenderer` 已负责真实 OpenGL 文本绘制（`STBEasyFont`）。
- `FontAtlas` 目前主要用于字体路径存在性检查，未接入纹理字形渲染。
- `Main.resolveFontPath()` 支持两种相对路径，降低 `:run` 工作目录差异导致的失败。

### 9.4 并发模型

- 逻辑线程通过 `BlockingQueue.take()` 阻塞等待输入事件。
- 渲染线程独立循环，不等待逻辑线程，避免 UI 卡死。
- 目前无复杂同步原语，数据共享点集中在 `TerminalBuffer`。

## 10. 与技术文档的对齐度

### 10.1 已对齐

- 多模块结构与层次解耦。
- 表达式解析 + 惰性求值主线。
- 关卡驱动、限制校验、评分机制。
- 终端式输入交互、语法高亮、候选补全。

### 10.2 偏差与简化

- 渲染未实现完整字体图集与 shader 管线，当前为原型级文本绘制。
- 规则中 `forced_blocks` 不是 AST 级强校验。
- 部分积木实现以结果正确优先，暂未强调复杂度或数据结构语义还原。

## 11. 已知风险与技术债

### 11.1 正确性风险

- `containsForcedBlocks` 的字符串匹配存在名称子串误判可能。
- `SortBlock` / `PrioQueueBlock` 使用原始类型排序，输入元素不可比较时可能抛 `ClassCastException`。

### 11.2 渲染与平台风险

- `STBEasyFont` 可用但显示风格与最终字体方案存在差距。
- 运行依赖本机 OpenGL 能力，低端/异常驱动环境未做降级。

### 11.3 维护性风险

- `Parser.wire(...)` 通过受抑制的原始类型分派，类型安全较弱。
- `TerminalBuffer` 共享读写未做精细同步，后续高频更新场景可能出现可见性问题。

## 12. 后续 AI 开发建议（优先级）

### P0（优先处理）

- 将 `containsForcedBlocks` 升级为基于 AST 的积木出现校验。
- 为 `SortBlock` / `PrioQueueBlock` 增加非 `Comparable` 输入防护与清晰错误消息。
- 补充 `game-core` 针对规则边界和异常输入的测试。

### P1（稳定性）

- 在 `game-gl` 加入最小化线程可见性保障（例如双缓冲快照或轻量同步策略）。
- 将文本渲染从 `STBEasyFont` 过渡到真正 `FontAtlas` 纹理字形方案。

### P2（体验与扩展）

- 关卡切换流程与 UI 状态机完善（当前主要展示 level-1 与输入反馈）。
- 补全错误提示分层：词法错误、语法错误、规则错误、运行错误。

## 13. AI 开发操作约定（减少沟通成本）

- 包名前缀固定：`com.algoblock`
- UI 主题：仅深色模式
- 关卡剧情：当前使用占位文案，不视为最终稿
- 新功能优先放在 `game-core` 可测试层，再在 `game-gl` 做薄接入
- 任何涉及运行状态改动，至少验证：
  - `gradle :game-core:test`
  - `gradle build`

## 14. 快速核对清单（交接即用）

- 能否构建：`gradle build` 应成功。
- 能否测通：`gradle :game-core:test` 应成功。
- 能否启动：`gradle :game-gl:run` 应弹出窗口并显示关卡文案与输入提示。
- 输入链路：输入字符、Tab、Enter 应触发缓冲刷新与结果反馈。

---

最后更新：基于当前仓库实际代码状态（v260413）。
