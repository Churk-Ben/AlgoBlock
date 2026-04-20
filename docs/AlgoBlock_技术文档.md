# AlgoBlock 技术文档

> 版本：v1.1
> 日期：2026-04-20  
> 说明：本文档描述 **当前仓库已实现状态**，不包含未落地规划。

---

## 目录

1. [项目定位](#1-项目定位)
2. [当前架构总览](#2-当前架构总览)
3. [技术栈与构建](#3-技术栈与构建)
4. [模块详解](#4-模块详解)
5. [游戏主流程](#5-游戏主流程)
6. [渲染与输入实现](#6-渲染与输入实现)
7. [积木与表达式系统](#7-积木与表达式系统)
8. [关卡与评分系统](#8-关卡与评分系统)
9. [测试现状](#9-测试现状)
10. [已实现与待扩展边界](#10-已实现与待扩展边界)

---

## 1 项目定位

AlgoBlock 是一个声明式序列变换谜题游戏。玩家通过嵌套积木表达式，将输入序列变换为目标序列并通关。

当前版本核心目标：

- 用 `Block` 抽象表达序列变换。
- 用 `Parser + EvalContext` 实现表达式解析和受限求值。
- 用 LWJGL 渲染终端风格 UI，并提供补全、高亮、特效反馈。
- 用 JSON 定义关卡，支持关卡规则校验与三星评分。

---

## 2 当前架构总览

### 2.1 模块结构（Gradle Multi-Project）

```text
algoblock/
├── game-api    // 基础合约：Block 抽象、上下文、注解、验证结果
├── game-core   // 引擎：积木实现、解析器、规则、评分、关卡加载
└── game-gl     // 图形层：窗口、输入、渲染、页面状态机（TEA 风格）
```

### 2.2 依赖方向

```text
game-api  <- game-core <- game-gl
```

约束：

- `game-api` 不依赖其他项目模块。
- `game-core` 依赖 `game-api`，不依赖图形层。
- `game-gl` 依赖 `game-core`，负责交互与可视化。

### 2.3 运行模型

当前是单 JVM 进程，包含三个关键线程：

- 主线程：GLFW 初始化与事件等待。
- 渲染线程：OpenGL 上下文、UI 渲染与交换缓冲。
- 逻辑线程：消费输入事件，映射输入意图，驱动应用状态更新。

---

## 3 技术栈与构建

### 3.1 语言与版本

- Java 21（`options.release = 21`）
- Gradle 多模块构建（根项目统一配置）

### 3.2 核心依赖

| 模块      | 依赖                                 | 用途                     |
| --------- | ------------------------------------ | ------------------------ |
| game-core | Gson 2.10.1                          | 关卡 JSON 反序列化       |
| game-gl   | LWJGL 3.3.4（lwjgl/glfw/opengl/stb） | 窗口、输入、OpenGL、字体 |
| game-gl   | JLayer 1.0.1                         | 音效播放                 |
| 测试      | JUnit Jupiter 5.10.2                 | 单元测试                 |

说明：

- 按平台自动选择 LWJGL native（Windows/Linux/macOS + 架构）。
- 构建脚本使用 `com.gradleup.shadow`（game-gl）用于打包可执行 fat-jar。

### 3.3 常用命令

```bash
./gradlew clean build
./gradlew test
./gradlew :game-gl:run
./gradlew :game-gl:shadowJar
```

---

## 4 模块详解

### 4.1 `game-api`（合约层）

主要类型：

- `Block<O>`：所有积木基类，定义 `evaluate()`、`signature()`、`validate()`、`nodeCount()`。
- `NullaryBlock` / `UnaryBlock` / `BinaryBlock`：按参数个数分层。
- `EvalContext`：输入只读快照、步数预算、trace、临时变量表（如 `it`）。
- `BlockMeta`：运行时注解，记录名字、签名、描述、元数。
- `ValidationResult` / `TLEException`：校验与步数超限机制。

设计特点：

- 求值必须显式 `consumeStep()`，用于预算控制。
- `EvalContext` 提供 `putVar/getVar`，支撑 `Map/Filter` 右侧函数块读取当前元素。

### 4.2 `game-core`（引擎层）

#### 核心子系统

- `BlockRegistry`
  - 在构造时注册内置积木（含 `_INPUT_`、`DoubleOp`、`EvenPred`）。
  - 通过反射无参构造实例化积木。
  - 基于 `@BlockMeta` 或继承关系推断 arity。
  - 提供 `allMeta()` 给 UI 补全使用。

- `Lexer` / `Parser`
  - `Lexer` 支持 token：`IDENT`、`NUMBER`、`<`、`>`、`,`、`EOF`。
  - `Parser` 使用递归下降，支持数字字面量（映射为 `ConstIntBlock`）。
  - 支持表达式形态如：
    - `Array<PopEach<PrioQueue<_INPUT_>>>`
    - `Map<_INPUT_><DoubleOp>`
    - `Zip<_INPUT_><3>`
  - Parse 末尾调用 `root.validate()`。

- `GameCoreService`
  - 提交流程：可用积木校验 -> 解析 -> 强制积木校验 -> 求值 -> 判题 -> 评分。
  - 返回 `SubmissionResult(accepted, score, trace, result, message)`。

- `LevelRules`
  - `usesOnlyAvailableBlocks`：正则扫描表达式 token，限制只能使用关卡开放积木。
  - `containsForcedBlocks`：遍历积木树，检查必选积木是否出现。

- `Judge` / `Scorer`
  - 判题采用 `List.equals` 做输出一致性判断。
  - 三星规则：正确性 + 最简解 + 速度。

- `LevelLoader` / `LevelRegistry`
  - Gson 按 `LOWER_CASE_WITH_UNDERSCORES` 读取 JSON。
  - 当前入口直接 `loadRange(1, 30)` 加载 30 关。

### 4.3 `game-gl`（图形与交互层）

#### 应用状态模型

- `AppProgram` 管理三页：
  - `StartPage`（开始菜单）
  - `GamePage`（主游戏页）
  - `DiagnosticsPage`（诊断页）
- `AppModel` 持有当前页面状态和当前关卡。
- `TeaRuntime` 维护 model 原子快照、同步 dispatch、渲染入口。

#### 命令执行

- `AppCmdHandler` 异步处理：
  - `Submit`：单线程执行 `GameCoreService.submit`，回传 `SubmitFinished`。
  - `PlaySound`：线程池播放资源音效。
  - `Exit`：直接退出进程。

---

## 5 游戏主流程

```text
Main
 ├─ 初始化 GLFW 窗口与 framebuffer 回调
 ├─ 加载 levels(1..30)
 ├─ 构建 BlockRegistry + GameCoreService
 ├─ 构建 AppProgram + TeaRuntime + 输入队列
 ├─ 启动 logic 线程
 ├─ 启动 render 线程
 └─ 主线程等待窗口关闭并回收资源
```

### 5.1 逻辑线程流程

```text
InputEventQueue.take()
  -> InputIntentMapper.map(event)
  -> InputIntentQueue.offer/take
  -> uiRuntime.dispatch(AppMsg.Intent(...))
```

输入分层：

- 设备事件：`CharEvent/KeyEvent/WheelEvent/PasteEvent`
- 语义意图：`TextTyped/Submit/Tab/NavigatePrev/...`
- 意图带 TTL（导航类短时有效），防止过期输入污染状态。

### 5.2 渲染线程流程

```text
snapshot model -> program.view() 生成 RenderFrame
-> TextRenderer.upload/draw
-> CursorRenderer.draw
-> EffectsRenderer.draw
-> glfwSwapBuffers
```

特性：

- 根据 viewport 动态计算可见列行并重建 `TerminalBuffer`。
- 文本层、光标层、特效层按顺序叠加。

---

## 6 渲染与输入实现

### 6.1 文本渲染

- `TerminalBuffer` 为字符网格，Cell = `(char c, int fg, int bg)`。
- `TextRenderer` 当前采用 OpenGL 立即模式（`glBegin(GL_QUADS)`）绘制：
  - 背景格
  - 字形纹理采样前景
- `FontAtlas`：
  - 基于 `stb_truetype` 动态烘焙字形到 RGBA atlas。
  - 懒加载 glyph（按需插入），支持 fallback 字符。
  - 使用 `GL_NEAREST` 保持终端像素风格。

### 6.2 光标与特效

- `CursorRenderer` 使用独立 shader（`cursor_vert.glsl` / `cursor_frag.glsl`）。
- `EffectsRenderer` 分发 `UiEffectRenderer`：
  - `CrtEffect`（扫描线+暗角）
  - `DimEffect`（聚焦遮罩）
  - `GlitchEffect`（错位故障）

### 6.3 输入系统

- GLFW 回调在 `GlfwInputAdapter` 里接入。
- `KeyMapper` 将 GLFW key 归一到 `InputKey`。
- `InputEventQueue` 与 `InputIntentQueue` 均为 `LinkedBlockingQueue`。

---

## 7 积木与表达式系统

### 7.1 当前内置积木

| 类别           | 积木                                                         |
| -------------- | ------------------------------------------------------------ |
| basic          | `Identity`, `Array`                                          |
| collection     | `Stack`, `Queue`, `PrioQueue`                                |
| transform      | `Sort`, `Reverse`, `Map`, `Filter`, `Zip`, `Flat`, `PopEach` |
| io/fn          | `_INPUT_`, `DoubleOp`, `EvenPred`                            |
| parser-literal | `ConstIntBlock`（数字字面量）                                |

### 7.2 语义说明（关键点）

- `Map` / `Filter`：逐元素将当前项写入 `ctx["it"]`，右子积木读取处理。
- `Zip`：右参为分组大小，默认最小为 1。
- `Sort` / `PrioQueue`：要求元素可比较，否则运行时报错。
- `_INPUT_`：返回输入副本，避免外部修改原始输入。

### 7.3 表达式语法（当前实现）

非正式形式：

```text
expr := NUMBER | IDENT ('<' expr (',' expr)? '>')*
```

备注：

- 解析器以 block 的 arity 校验参数个数。
- 语法支持 `Map<_INPUT_><DoubleOp>` 这种“多段尖括号”写法。

---

## 8 关卡与评分系统

### 8.1 关卡模型

`Level` 字段：

- `schemaVersion`, `id`, `title`, `story`
- `input`, `output`
- `availableBlocks`, `forcedBlocks`, `bonusCombos`
- `optimalSize`, `timePar`, `stepBudget`
- `discoveryHint`

当前资源：`/levels/level-1.json` 到 `/levels/level-30.json`。

### 8.2 提交规则

提交时依次检查：

1. 仅使用 `availableBlocks`。
2. 表达式解析成功。
3. 包含全部 `forcedBlocks`。
4. 求值完成并与目标输出一致。

### 8.3 评分规则

`stars = correctness + minimal + speed`，每项 0/1：

- `correctness`：结果完全正确。
- `minimal`：`nodeCount() <= optimalSize`。
- `speed`：提交耗时 `<= timePar`。

---

## 9 测试现状

当前 `game-core` 已有 JUnit 覆盖：

- `BlocksTest`：核心积木行为（Stack/PrioQueue/Map+Filter/Zip+Flat/Array+Reverse）。
- `ParserTest`：嵌套表达式解析、Map 解析、arity 失败路径。
- `GameCoreServiceTest`：AC/语法错误/未开放积木/缺失强制积木/不可比较排序。
- `LevelLoaderTest`：关卡批量加载与字段验证。

测试定位：

- 重点验证引擎稳定性与规则链路。
- UI 渲染与输入映射目前主要依赖运行期手工验证。

---

## 10 已实现与待扩展边界

### 10.1 已实现

- Gradle 三模块架构与清晰依赖方向。
- 30 关 JSON 关卡加载与规则校验。
- 表达式解析、求值、评分全链路。
- TEA 风格 UI 状态管理（三页面）。
- OpenGL 文本渲染、光标与多种屏幕特效。
- 自动补全、基础语法高亮、音效反馈。

### 10.2 当前未落地（与旧文档差异项）

以下内容在当前仓库 **尚未实现**，仅可作为后续规划：

- Mod 外部 jar 动态加载（`URLClassLoader` 热加载流程）。
- 独立 `mod` 子目录与脚本化编译管线。
- 基于 Transform Feedback 的 GPU 粒子拖尾。
- AI 接入的关卡提示与教学。

---

_AlgoBlock Technical Document v1.1 (code-aligned) · 2026-04-20_
