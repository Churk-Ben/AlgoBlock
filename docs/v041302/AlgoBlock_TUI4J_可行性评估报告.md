# AlgoBlock 引入 TUI4J 可行性评估报告

> 日期：2026-04-13  
> 目标：评估“引入 tui4j 作为页面状态（UI state/状态机）”在当前项目上的可行性与建议落地路径。

## 1. 结论摘要

- 如果目标是“用 TEA/ELM 风格的单向数据流”来组织 UI 状态：可行，且建议优先采用“架构理念迁移（自研 Model/Update/View）”，而不是直接引入某个 TUI 库作为渲染框架。
- 如果目标是“把 `game-gl` 迁移到真实终端 TUI”：可行但代价很大，会改变技术文档中 OpenGL 渲染路线（字体/特效/Compositor）与既有 GLFW 窗口交互，属于方向性重构。
- 在开源协议上：优先考虑 MIT 许可的 `tui4j`（WilliamAGH），避免 GPL-3.0 许可带来的传播性约束。

## 2. 当前项目 UI 的真实形态（决定了适配成本）

- 当前 UI 并不运行在系统终端里，而是运行在 GLFW/OpenGL 窗口中渲染“终端风格字符网格”（pseudo-TUI）。
- 现有 UI 关键点：
  - 输入与交互编排： [TerminalWidget.java](file:///workspace/game-gl/src/main/java/com/algoblock/gl/ui/TerminalWidget.java#L16-L95)
  - “屏幕”抽象： [TerminalBuffer.java](file:///workspace/game-gl/src/main/java/com/algoblock/gl/renderer/TerminalBuffer.java#L1-L65)
  - 渲染主循环与回调： [Main.java](file:///workspace/game-gl/src/main/java/com/algoblock/gl/Main.java#L52-L162)

因此，“引入一个面向 ANSI/终端输出的 TUI 框架”通常不能直接复用其渲染层，但可以复用其状态管理范式。

## 3. 候选库盘点（同名/相近名的两类项目）

### 3.1 WilliamAGH/tui4j（推荐关注）

- 定位：Java 的终端 TUI 框架，受 Bubble Tea 启发，采用 The Elm Architecture（Model / Update / View）思想；README 明确 “uses The Elm Architecture”。
- 依赖方式：Maven Central（`com.williamcallahan:tui4j`）。
- 许可：MIT。
- 参考：
  - GitHub：https://github.com/WilliamAGH/tui4j
  - README 中对 TEA 的说明与 Quick Start（`new Program(new MyModel()).run();`）：https://github.com/WilliamAGH/tui4j

### 3.2 KartoffelChipss/TUI4J（不建议用于本项目）

- 定位：更偏“命令行交互/输入组件 + 文本格式化”的库（表单输入、spinner、table、命令行解析等），不是以“页面状态机/应用架构”作为核心卖点。
- 分发：JitPack（非 Maven Central），工程长期稳定性与供应链管理成本更高。
- 许可：GPL-3.0（对项目分发与闭源/混用约束更强）。
- 参考：
  - GitHub：https://github.com/KartoffelChipss/TUI4J

## 4. 与 AlgoBlock 的适配评估

### 4.1 作为“页面状态/状态机”的适配（可行，推荐）

把 `game-gl` 的 UI 组织方式向 TEA 靠拢，本质上是把现在分散在 `TerminalWidget` 内的“隐式状态 + 直接改 buffer”收敛为：

- `Model`：纯状态（当前输入行、光标位置、候选列表、当前关卡信息、最近一次提交结果、提示信息等）
- `Msg/Event`：输入事件与引擎返回（Key/Char/Tab/Enter、关卡切换、提交结果返回等）
- `update(model, msg) -> model' + commands`：纯函数式更新（commands 可表示异步调用 `GameCoreService.submit`）
- `view(model) -> TerminalBufferDiff | RenderTree`：从状态导出“要显示什么”，再由 renderer 应用到 `TerminalBuffer`

这一套做完，UI 代码会更容易：

- 加“模式”（编辑模式/选择关卡/展示评分/帮助页）而不把逻辑塞进一个类
- 做单元测试（update 是纯逻辑）
- 做回放/调试（Msg 日志 + Model 快照）

注意：上述目标不需要引入第三方 TUI 框架；采用 TEA 范式即可达到“页面状态”的诉求。

### 4.2 直接引入 WilliamAGH/tui4j 当作 UI 框架（可行但不匹配当前形态）

主要阻碍在“渲染后端”：

- `tui4j` 面向真实终端：输出 ANSI、依赖终端 alternate screen、终端尺寸变化、终端输入流等。
- AlgoBlock 当前在 OpenGL 窗口里“自己画字符”，并不走系统终端输出通道。

要想“直接用 tui4j”，通常意味着：

- 方向 1：放弃 `game-gl` 这套 OpenGL 渲染，改成纯终端应用（会偏离技术文档中关于字体/Pass/特效层的设计目标）。
- 方向 2：为 tui4j 写一套自定义 renderer，把它的 view 产物映射到 `TerminalBuffer`（需要深入理解 tui4j 的渲染模型，且后续升级成本高）。

### 4.3 工程依赖与供应链风险

- 当前仓库的构建在“受限网络”环境下已经表现出对外部仓库可达性的敏感（wrapper 分发、Gradle 插件解析、依赖拉取都会被影响）。
- 引入新的第三方库会进一步放大这一点，因此更建议先把构建基线稳定化（见 [AlgoBlock_项目现状与下一步路线图.md](file:///workspace/docs/v041301/AlgoBlock_%E9%A1%B9%E7%9B%AE%E7%8E%B0%E7%8A%B6%E4%B8%8E%E4%B8%8B%E4%B8%80%E6%AD%A5%E8%B7%AF%E7%BA%BF%E5%9B%BE.md)）。

## 5. 建议落地方案（按投入从小到大）

### 方案 A（推荐）：TEA 架构内化，自研轻量状态容器

- 保持 `game-gl` 渲染与 `TerminalBuffer` 不变
- 拆分 `TerminalWidget` 为：
  - `UiModel`（纯状态）
  - `UiUpdate`（处理 InputEvent / EngineEvent）
  - `UiView`（把 Model 渲染到 buffer）
- “引擎调用”以命令（command）方式异步触发，避免 update 内直接阻塞

### 方案 B：只引入“架构依赖”，不引入渲染（谨慎）

- 如果 `tui4j` 提供了足够独立的 TEA 抽象（Model/Program）且能替换/抽象 renderer，可以考虑复用其状态机执行框架，但前提是：
  - 可以彻底绕开 ANSI/终端输出路径
  - 不会把项目绑定在其 API 形态上，导致未来 UI 更换成本升高

### 方案 C：终端化重构（不推荐作为近期路线）

- 迁移到真实终端 TUI（从 `game-gl` 迁到 `game-tui` 或新模块）
- 适合的前提：
  - 明确放弃 OpenGL 的特效/字体渲染目标
  - 产品形态希望更接近“命令行解题游戏”而不是窗口应用

## 6. 结论与下一步建议

- “页面状态”诉求：建议采用方案 A，把 TEA 架构落到 `game-gl` 的 UI 层，获得状态可测试、逻辑可维护、可扩展的收益。
- “要不要引入第三方 tui4j”：如果只是想要 TEA，不必引入；如果未来决定转纯终端应用，则优先考虑 MIT 的 WilliamAGH/tui4j，并在做方向性重构前先写最小 Spike（仅 1 页 UI）验证输入/渲染/打包链路。

