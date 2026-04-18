# AlgoBlock 技术文档

> **版本** v1.0 · **日期** 2026-04-12  
> Java 程序设计课程作业 · 算法解密游戏

---

## 目录

1. [项目定位](#1-项目定位)
2. [整体架构](#2-整体架构)
3. [技术栈](#3-技术栈)
4. [模块详解](#4-模块详解)
   - 4.1 [game-api](#41-game-api--mod-合约层)
   - 4.2 [game-core](#42-game-core--引擎层)
   - 4.3 [game-gl](#43-game-gl--渲染层)
5. [渲染架构](#5-渲染架构)
   - 5.1 [字体管道](#51-字体管道--fontatlas)
   - 5.2 [Pass 1 文字层](#52-pass-1-文字层)
   - 5.3 [Pass 2 特效层](#53-pass-2-特效层)
   - 5.4 [Compositor](#54-compositor)
6. [输入系统](#6-输入系统)
7. [积木体系](#7-积木体系)
8. [表达式解析器](#8-表达式解析器)
9. [关卡系统](#9-关卡系统)
10. [Mod 系统](#10-mod-系统)
11. [IPC 与进程模型](#11-ipc-与进程模型)
12. [开发路径](#12-开发路径)
13. [AI 辅助指导](#13-ai-辅助指导)
14. [设计决策记录](#14-设计决策记录)

---

## 1 项目定位

AlgoBlock 是一款**声明式序列变换**谜题游戏。玩家通过嵌套组合「积木」写出一条表达式，把给定的整数序列变换为目标序列，从而通关。

### 核心体验三要素

| 要素             | 描述                                                           |
| ---------------- | -------------------------------------------------------------- |
| **声明而非命令** | 玩家写的是「如何描述结果」，不是「一步步怎么做」               |
| **提交才知对错** | Parse（建树）与 Evaluate（求值）严格分离，提交前不可见中间结果 |
| **积木全是砖块** | 工具箱只有微观容器和变换原子，没有宏观算法（BFS 不是积木）     |

### Java 特性映射

| Java 特性             | 在游戏中的体现                                    |
| --------------------- | ------------------------------------------------- |
| 继承 / 多态           | 所有积木继承 `Block<O>`，`evaluate()` 多态分派    |
| 泛型                  | 积木签名 `Block<I,O>` 在 Tab 补全中对玩家可见     |
| 注解 `@BlockMeta`     | 携带积木名、签名、描述，反射读取后注册到注册表    |
| 反射 + URLClassLoader | 运行时扫描 `mods/` 目录，动态加载第三方积木       |
| 懒惰求值              | Parse 建树，Evaluate 延迟到提交，对齐算法竞赛体验 |

---

## 2 整体架构

### 进程模型

```
单一 JVM 进程
┌──────────────────────────────────────────────┐
│                                              │
│  game-core  ←──────────────  game-api        │
│  (引擎、积木、关卡、Mod 加载)    (Block 合约)  │
│       │                                      │
│  game-gl                                     │
│  (GLFW 窗口 + OpenGL 渲染 + 输入队列)         │
│                                              │
└──────────────────────────────────────────────┘
```

**无 IPC，无跨进程通信。** 渲染层和引擎层在同一 JVM 里通过直接方法调用交互，渲染线程与游戏逻辑线程通过 `BlockingQueue` 解耦。

### Maven 模块结构

```
algoblock/
├── game-api/          ← Mod 合约层（纯 JDK，无第三方依赖）
│   ├── Block.java
│   ├── EvalContext.java
│   ├── ValidationResult.java
│   └── BlockMeta.java
│
├── game-core/         ← 引擎层
│   ├── engine/        ← Parser, BlockRegistry, Judge
│   ├── blocks/        ← 所有内置积木（见 §7）
│   ├── levels/        ← JSON 加载, Level, LevelRegistry
│   └── mod/           ← URLClassLoader 热加载
│
└── game-gl/           ← 渲染层（替代原 game-tui）
    ├── renderer/      ← FontAtlas, TextRenderer, EffectRenderer, Compositor
    ├── input/         ← InputEventQueue, KeyMapper
    ├── ui/            ← TerminalWidget, Completer, SyntaxHighlighter, VisualizerWidget
    └── shader/        ← text.vert/frag, particle.vert/frag, wave.frag
```

### 模块依赖规则

```
game-api   ──►  （无依赖）
game-core  ──►  game-api, Gson, JUnit 5
game-gl    ──►  game-core, LWJGL3
Mod jar    ──►  game-api（编译期）；运行时由 URLClassLoader 注入
```

`game-core` 对 `game-gl` 无任何依赖，引擎可以独立测试。

---

## 3 技术栈

### 核心依赖

| 库          | 版本   | 用途                                              |
| ----------- | ------ | ------------------------------------------------- |
| **LWJGL3**  | 3.3.x  | GLFW 窗口管理、OpenGL 绑定、stb_truetype 字体烘焙 |
| **Gson**    | 2.10.x | 关卡 JSON 加载                                    |
| **JUnit 5** | 5.10.x | 单元测试                                          |

### LWJGL3 子模块选用

```xml
<!-- pom.xml（game-gl 模块） -->
<dependency>
    <groupId>org.lwjgl</groupId>
    <artifactId>lwjgl</artifactId>          <!-- core + GLFW -->
</dependency>
<dependency>
    <groupId>org.lwjgl</groupId>
    <artifactId>lwjgl-opengl</artifactId>   <!-- OpenGL 4.3+ -->
</dependency>
<dependency>
    <groupId>org.lwjgl</groupId>
    <artifactId>lwjgl-stb</artifactId>      <!-- stb_truetype 字体烘焙 -->
</dependency>
<!-- 平台 natives：按目标平台选择 linux/windows/macos -->
```

> **为什么选 LWJGL3 而不是 JavaFX / Electron？**
>
> - JavaFX 的 GPU 通道不够深，无法实现真正的 GLSL 粒子拖尾
> - Electron 引入 TypeScript，偏离「纯 Java」目标
> - LWJGL3 是纯 Java 绑定，native 库由 LWJGL 构建系统提供，不需要自己写 C/C++
> - LWJGL3 的 native 加载本身使用反射机制，与课设要求的 Java 特性高度一致

### 字体选择

推荐使用开源等宽字体（选其一即可）：

- **JetBrains Mono**：连字支持好，极客风格
- **Iosevka**：字形窄，同屏可放更多列
- **Hack**：烘焙效果稳定，无奇怪字形

---

## 4 模块详解

### 4.1 game-api · Mod 合约层

此模块是 Mod 作者编译时唯一可见的接口，保持极度精简。

```java
// Block.java
public abstract class Block<O> {
    /** 懒惰求值入口。ctx 持有输入数据与沙箱限制。*/
    public abstract O evaluate(EvalContext ctx);

    /** Tab 补全时展示的类型签名，如 "Collection<T> → List<T>" */
    public String signature() { return "?"; }

    /** 提交前的静态合法性检查（arity、子积木类型等）*/
    public ValidationResult validate() { return ValidationResult.OK; }
}

// 结构子类：按「接受几个子积木」划分
public abstract class NullaryBlock<O>     extends Block<O> { }
public abstract class UnaryBlock<I,O>     extends Block<O> { protected Block<I> child; }
public abstract class BinaryBlock<A,B,O>  extends Block<O> {
    protected Block<A> left;
    protected Block<B> right;
}
```

```java
// EvalContext.java
public final class EvalContext {
    private final List<?> input;
    private int stepBudget;                     // 耗尽时抛 TLEException
    private final List<String> trace = new ArrayList<>();

    public List<?> getInput()   { return Collections.unmodifiableList(input); }
    public void consumeStep()   { if (--stepBudget < 0) throw new TLEException(); }
    public void log(String msg) { trace.add(msg); }
    public List<String> trace() { return Collections.unmodifiableList(trace); }
}
```

```java
// BlockMeta.java
@Retention(RetentionPolicy.RUNTIME)   // 必须 RUNTIME，反射才可读
@Target(ElementType.TYPE)
public @interface BlockMeta {
    String name();
    String signature()   default "?";
    String description() default "";
    int    arity()       default 1;
}
```

---

### 4.2 game-core · 引擎层

#### BlockRegistry（反射核心）

```java
public class BlockRegistry {
    private static final Map<String, Class<? extends Block<?>>> MAP = new HashMap<>();

    public static void registerBuiltins() {
        Map.of(
            "Array",     ArrayBlock.class,
            "Stack",     StackBlock.class,
            "Queue",     QueueBlock.class,
            "PrioQueue", PrioQueueBlock.class,
            "Sort",      SortBlock.class,
            "Reverse",   ReverseBlock.class,
            "Map",       MapBlock.class,
            "Filter",    FilterBlock.class,
            "Zip",       ZipBlock.class,
            "Flat",      FlatBlock.class,
            "PopEach",   PopEachBlock.class
        ).forEach(MAP::put);
    }

    // Mod 加载：扫描 mods/*.jar，反射读取 @BlockMeta 并注册
    public static void loadMods(Path modDir) throws IOException {
        URL[] urls = Files.list(modDir)
            .filter(p -> p.toString().endsWith(".jar"))
            .map(p -> { try { return p.toUri().toURL(); }
                        catch (Exception e) { throw new RuntimeException(e); } })
            .toArray(URL[]::new);

        URLClassLoader loader = new URLClassLoader(urls,
                BlockRegistry.class.getClassLoader());

        for (URL url : urls) {
            try (JarFile jar = new JarFile(url.getFile())) {
                jar.entries().asIterator().forEachRemaining(entry -> {
                    if (!entry.getName().endsWith(".class")) return;
                    String cls = entry.getName().replace('/', '.').replace(".class", "");
                    try {
                        Class<?> c = loader.loadClass(cls);
                        if (!Block.class.isAssignableFrom(c)) return;
                        BlockMeta m = c.getAnnotation(BlockMeta.class);
                        if (m == null) return;
                        MAP.put(m.name(), (Class<? extends Block<?>>) c);
                        System.out.println("[MOD] Loaded: " + m.name());
                    } catch (Exception ignored) {}
                });
            }
        }
    }

    public static Block<?> instantiate(String name) throws Exception {
        Class<? extends Block<?>> cls = MAP.get(name);
        if (cls == null) throw new UnknownBlockException(name);
        return cls.getDeclaredConstructor().newInstance();
    }

    public static Collection<BlockMeta> allMeta() {
        return MAP.values().stream()
            .map(c -> c.getAnnotation(BlockMeta.class))
            .filter(Objects::nonNull)
            .toList();
    }
}
```

#### Judge

```java
public class Judge {
    public static boolean check(Object result, List<?> expected) {
        if (!(result instanceof List<?> actual)) return false;
        return actual.equals(expected);
    }
}
```

---

### 4.3 game-gl · 渲染层

渲染层是纯粹的输出设备，不包含任何游戏逻辑。它只做三件事：

1. 维护一个 `TerminalBuffer`（字符网格）
2. 将 Buffer 渲染到屏幕（Pass 1）
3. 在 Pass 1 上方叠加特效（Pass 2）

#### TerminalBuffer

```java
public class TerminalBuffer {
    public record Cell(char c, int fg, int bg) {}

    private final int cols, rows;
    private final Cell[] cells;

    public TerminalBuffer(int cols, int rows) {
        this.cols  = cols;
        this.rows  = rows;
        this.cells = new Cell[cols * rows];
        clear();
    }

    public void set(int col, int row, char c, int fg, int bg) {
        cells[row * cols + col] = new Cell(c, fg, bg);
    }

    public void print(int col, int row, String text, int fg, int bg) {
        for (int i = 0; i < text.length(); i++)
            set(col + i, row, text.charAt(i), fg, bg);
    }

    public void clear() {
        Arrays.fill(cells, new Cell(' ', 0xCDD9E5, 0x0D1117));
    }

    public Cell[] cells() { return cells; }
}
```

所有 UI 组件（`TerminalWidget`、`Completer`、`SyntaxHighlighter`、`VisualizerWidget`）都只操作 `TerminalBuffer`，不直接调用任何 OpenGL API。

---

## 5 渲染架构

```
┌─────────────────────────────────────────────┐
│              GLFW Window                    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  Pass 2: Effect Layer               │    │
│  │  GLSL Shader · additive blending    │    │
│  │  · 光标粒子拖尾（Transform Feedback）│    │
│  │  · AC 全屏光波（SDF 环 + 衰减）      │    │
│  │  · WA 红色闪屏                       │    │
│  └──────────────── ↑ 叠加 ─────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │  Pass 1: Text Layer                 │    │
│  │  Instanced Quad · FontAtlas 采样    │    │
│  │  · 字符网格（cols × rows）           │    │
│  │  · 语法高亮 = per-instance fg color │    │
│  │  · ASCII 可视化 = 特殊字符填充       │    │
│  │  · Tab 补全浮层 = 局部 Buffer 覆盖   │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

两个 Pass 共用同一个 OpenGL context，Compositor 按序调用，无需 FBO 中转（直接渲染到默认 framebuffer）。

---

### 5.1 字体管道 · FontAtlas

**职责**：在启动时把 `.ttf` 文件烘焙成一张 GPU 纹理，并记录每个字符的 UV 坐标。

```java
public class FontAtlas {
    private final int textureId;
    private final int glyphW, glyphH;               // 单个字形的像素尺寸
    private final Map<Character, float[]> uvMap;    // char → [u0,v0,u1,v1]

    public FontAtlas(String ttfPath, int fontSize, int atlasW, int atlasH) {
        // 1. 用 STBTruetype 烘焙 ASCII 0x20~0x7E 共 95 个字形到 bitmap
        // 2. 上传 bitmap 为 GL_RED 格式纹理（单通道，只存 alpha）
        // 3. 记录每个字形的 UV 区域到 uvMap
    }

    public float[] uvOf(char c) {
        return uvMap.getOrDefault(c, uvMap.get('?'));
    }

    public void bind(int unit) { /* glActiveTexture + glBindTexture */ }
}
```

**烘焙参数建议**：

| 参数      | 推荐值     | 说明                                             |
| --------- | ---------- | ------------------------------------------------ |
| fontSize  | 18~24 px   | 过小则字形模糊                                   |
| atlasSize | 512×512    | 95 个 ASCII 字符绰绰有余                         |
| 纹理格式  | GL_RED     | 单通道，省显存；frag 里用 `vec4(fgColor, tex.r)` |
| 滤波      | GL_NEAREST | 像素风格，禁止双线性模糊                         |

---

### 5.2 Pass 1 文字层

**渲染策略**：一次 `glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, cols*rows)` 画完整个终端。

#### Vertex Shader

```glsl
// text.vert
#version 430 core

layout(location = 0) in vec2 quadPos;   // 单位正方形顶点 [0,1]²，静态 VBO

// per-instance（从 SSBO 读取）
layout(std430, binding = 0) buffer CellBuffer {
    // 每个 Cell 打包为 2 个 uint：
    // [0]: char(16bit) | fg_r(8bit) | fg_g(8bit)
    // [1]: fg_b(8bit)  | bg_r(8bit) | bg_g(8bit) | bg_b(8bit)
    uvec2 cells[];
};

uniform vec2 u_cellSize;    // 单个字符格的屏幕像素尺寸
uniform vec2 u_screenSize;  // 窗口分辨率
uniform int  u_cols;        // 终端列数

out vec2  v_atlasUV;
out vec3  v_fg;
out vec3  v_bg;

void main() {
    int  idx  = gl_InstanceID;
    int  col  = idx % u_cols;
    int  row  = idx / u_cols;

    // 解包 Cell 数据
    uvec2 cell = cells[idx];
    char  c    = char(cell.x >> 16);
    v_fg = vec3(
        float((cell.x >>  8) & 0xFF) / 255.0,
        float((cell.x      ) & 0xFF) / 255.0,
        float((cell.y >> 24) & 0xFF) / 255.0
    );
    v_bg = vec3(/* 同理解包 */);

    // 计算 Atlas UV（FontAtlas 通过 UBO 传入每字符偏移）
    v_atlasUV = /* 查 Atlas UV 表 */ ;

    // 屏幕坐标（NDC）
    vec2 pixelPos = vec2(col, row) * u_cellSize + quadPos * u_cellSize;
    gl_Position   = vec4(pixelPos / u_screenSize * 2.0 - 1.0, 0.0, 1.0);
    gl_Position.y = -gl_Position.y;   // Y 轴翻转（屏幕坐标系）
}
```

#### Fragment Shader

```glsl
// text.frag
#version 430 core

in vec2 v_atlasUV;
in vec3 v_fg;
in vec3 v_bg;

uniform sampler2D u_atlas;
out vec4 fragColor;

void main() {
    float a   = texture(u_atlas, v_atlasUV).r;
    fragColor = vec4(mix(v_bg, v_fg, a), 1.0);
}
```

---

### 5.3 Pass 2 特效层

特效层使用 **additive blending**（`glBlendFunc(GL_ONE, GL_ONE)`），叠加到 Pass 1 上方不遮挡文字。

#### 光标拖尾粒子

使用 **Transform Feedback** 在 GPU 端更新粒子状态，零 CPU 读回开销。

```glsl
// particle.vert（Transform Feedback 更新 pass）
#version 430 core

in vec2  a_pos;        // 粒子当前位置
in vec2  a_vel;        // 速度
in float a_life;       // 剩余生命 [0,1]
in vec3  a_color;

uniform float u_dt;           // 帧间隔（秒）
uniform vec2  u_cursorPos;    // 当前光标屏幕坐标

out vec2  v_pos;
out vec2  v_vel;
out float v_life;
out vec3  v_color;

void main() {
    v_life  = a_life - u_dt * 1.5;
    v_pos   = a_pos + a_vel * u_dt;
    v_vel   = a_vel * 0.92;           // 阻尼
    v_color = a_color * v_life;       // 颜色随生命衰减

    if (v_life <= 0.0) {
        // 粒子死亡 → 在光标处重生
        v_pos   = u_cursorPos + (随机偏移);
        v_vel   = (随机方向) * 60.0;
        v_life  = 1.0;
        v_color = vec3(0.12, 0.43, 0.92);  // 蓝色拖尾
    }
}
```

#### AC 全屏光波

```glsl
// wave.frag
#version 430 core

uniform vec2  u_origin;     // 触发点（光标位置，归一化坐标）
uniform float u_time;       // 从触发开始经过的秒数
uniform vec2  u_resolution;

out vec4 fragColor;

void main() {
    vec2  uv   = gl_FragCoord.xy / u_resolution;
    float dist = distance(uv, u_origin);
    float wave = u_time * 0.8;                  // 扩散速度
    float ring = smoothstep(0.02, 0.0, abs(dist - wave));
    float fade = max(0.0, 1.0 - u_time * 1.2); // 整体衰减
    fragColor  = vec4(0.13, 0.86, 0.31, ring * fade * 0.6);
}
```

---

### 5.4 Compositor

```java
public class Compositor {
    private final TextRenderer   textRenderer;
    private final EffectRenderer effectRenderer;
    private long lastFrameNs = System.nanoTime();

    public void frame(TerminalBuffer buffer, List<Effect> activeEffects) {
        long now = System.nanoTime();
        float dt = (now - lastFrameNs) / 1_000_000_000f;
        lastFrameNs = now;

        glClear(GL_COLOR_BUFFER_BIT);

        // Pass 1：文字
        textRenderer.upload(buffer);    // 更新 SSBO
        textRenderer.draw();            // Instanced Draw Call

        // Pass 2：特效
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);    // additive
        for (Effect e : activeEffects) {
            effectRenderer.draw(e, dt);
        }
        glDisable(GL_BLEND);
    }
}
```

---

## 6 输入系统

### 架构

```
GLFW 键盘回调（渲染线程）
        │
        ▼
InputEventQueue（BlockingQueue<InputEvent>）
        │
        ▼
游戏逻辑线程（consume events）
```

GLFW 回调运行在渲染线程，游戏逻辑运行在独立线程，两者通过 `BlockingQueue` 解耦，避免在回调里做复杂逻辑。

### InputEvent 类型

```java
public sealed interface InputEvent permits
    CharEvent,        // 可打印字符输入
    KeyEvent,         // 功能键（Enter、Tab、Backspace、方向键、Esc）
    PasteEvent        // 粘贴板内容（GLFW 的 char callback 天然支持 Unicode）
{}
```

### Tab 补全逻辑

```
玩家按 Tab
    ↓
SyntaxHighlighter 对当前行做 Tokenize
    ↓
取光标左侧最近的 IDENT prefix
    ↓
BlockRegistry.allMeta() 过滤以 prefix 开头的积木
    ↓
Completer 把候选列表渲染到 TerminalBuffer 的浮层区域
    ↓
玩家用 ↑↓ 选择，Enter 确认，Esc 关闭
```

---

## 7 积木体系

### 设计原则：只放砖块

积木收录必须同时满足：

1. **原子性**：该积木的行为不能被现有积木的组合完全替代
2. **单步性**：一次 `evaluate()` 调用完成全部逻辑，不依赖外部循环或可变局部状态

BFS、DFS 因需要「循环 + 条件 + 可变访问标记」而不满足条件，以 Mod 形式存在。

### 积木目录

```
blocks/
├── basic/
│   ├── IdentityBlock       // 原样返回，占位 / 强制使用关卡
│   └── ArrayBlock          // 子积木输出包装为 ArrayList
│
├── collection/             // 容器积木：行为由容器语义决定
│   ├── StackBlock          // LIFO，输出是输入的完全反转
│   ├── QueueBlock          // FIFO，顺序保持
│   └── PrioQueueBlock      // 最小堆，等价自然升序
│
├── transform/
│   ├── SortBlock           // 按 Comparator 子积木排序
│   ├── ReverseBlock        // 反转序列（意图明确版的 Stack）
│   ├── MapBlock            // 对每个元素应用右子积木
│   ├── FilterBlock         // 按右子 Predicate 积木过滤
│   ├── ZipBlock            // 按 n 折叠为二维列表
│   └── FlatBlock           // 展平一层嵌套
│
└── io/
    ├── InputBlock          // 绑定 _INPUT_
    └── PopEachBlock        // 逐一 poll()/pop() 直到 Collection 为空
```

### 完整积木速查

| 积木             | 继承自       | 语义                |
| ---------------- | ------------ | ------------------- |
| `IdentityBlock`  | NullaryBlock | 原样返回子积木输出  |
| `ArrayBlock`     | UnaryBlock   | 包装为 ArrayList    |
| `StackBlock`     | UnaryBlock   | LIFO → 输出反转     |
| `QueueBlock`     | UnaryBlock   | FIFO → 顺序保持     |
| `PrioQueueBlock` | UnaryBlock   | 最小堆 → 自然升序   |
| `SortBlock`      | BinaryBlock  | 按 Comparator 排序  |
| `ReverseBlock`   | UnaryBlock   | 反转序列            |
| `MapBlock`       | BinaryBlock  | 逐元素变换          |
| `FilterBlock`    | BinaryBlock  | 元素过滤            |
| `ZipBlock`       | BinaryBlock  | 折叠为二维列表      |
| `FlatBlock`      | UnaryBlock   | 展平一层嵌套        |
| `InputBlock`     | NullaryBlock | 返回 `_INPUT_` 副本 |
| `PopEachBlock`   | UnaryBlock   | 逐一弹出 Collection |

---

## 8 表达式解析器

### 文法（非正式 EBNF）

```
expr   ::= '_INPUT_'
         | IDENT ( '<' expr '>' )*
```

### 两阶段流程

```
玩家输入字符串
      │
      ▼
┌─────────────┐
│   Lexer     │  →  Token 流：IDENT, LT(<), GT(>), COMMA
└─────────────┘
      │
      ▼
┌─────────────┐
│   Parser    │  →  Block 树（递归下降）
│             │      每遇 IDENT → BlockRegistry.instantiate()（反射）
│             │      解析 <arg> → 注入 child / left / right
└─────────────┘
      │
      ▼
  block.validate()   ←  静态检查（arity、子积木类型）
      │
      ▼
  TerminalWidget 展示类型签名树（不执行 evaluate）
      │
   玩家确认
      │
      ▼
  tree.evaluate(ctx)  ←  懒惰求值，此时才真正执行
```

### 一次完整的求值时序示例

```
输入: Array<PopEach<PrioQueue<_INPUT_>>>

[Lexer]
  IDENT(Array) LT IDENT(PopEach) LT IDENT(PrioQueue)
  LT IDENT(_INPUT_) GT GT GT

[Parser]
  ArrayBlock
    └── PopEachBlock
          └── PrioQueueBlock
                └── InputBlock

[validate()]  arity 检查通过 ✓

[Renderer]  展示签名树，不求值

玩家按 y 确认提交

[evaluate()]
  InputBlock      → [5, 2, 4, 3, 1]
  PrioQueueBlock  → MinHeap 压入 → poll×5 → [1, 2, 3, 4, 5]
  PopEachBlock    → 已内嵌于 PrioQueueBlock
  ArrayBlock      → ArrayList[1, 2, 3, 4, 5]

[Judge]  [1,2,3,4,5] == [1,2,3,4,5]  →  AC ✓
         积木数 3 ≤ optimal_size 3    →  ⭐ 最简解
         用时 18s ≤ time_par 30s      →  ⭐ 速度
```

---

## 9 关卡系统

### 关卡 JSON 格式

```json
{
  "schema_version": 1,
  "id": 3,
  "title": "等价的路",
  "story": "工厂新来了一批积木，但没有 Sort……",
  "input": [5, 2, 4, 3, 1],
  "output": [1, 2, 3, 4, 5],
  "available_blocks": ["Array", "PrioQueue", "PopEach"],
  "forced_blocks": [],
  "bonus_combos": [["PrioQueue", "PopEach"]],
  "optimal_size": 3,
  "time_par": 30,
  "step_budget": 50000,
  "discovery_hint": "PrioQueue 弹出顺序和 Sort 的关系是？"
}
```

| 字段               | 说明                                   |
| ------------------ | -------------------------------------- |
| `available_blocks` | 本关可用积木列表（Tab 补全只显示这些） |
| `forced_blocks`    | 必须使用的积木，缺少则判 WA            |
| `bonus_combos`     | 包含此组合时触发「发现新用法！」动效   |
| `optimal_size`     | 最优解积木数，达到则得⭐最简解         |
| `time_par`         | 速度评分阈值（秒）                     |
| `step_budget`      | EvalContext 步数上限（防死循环）       |

### 评分体系

三颗星独立判定：

| 星级      | 条件                                  |
| --------- | ------------------------------------- |
| ⭐ 正确性 | `evaluate()` 结果与 `output` 完全一致 |
| ⭐ 最简解 | 表达式积木总数 ≤ `optimal_size`       |
| ⭐ 速度   | 首次按键到提交总时间 ≤ `time_par` 秒  |

隐藏星（可选）：积木数 < `optimal_size × 0.8` 时触发，给神操作留空间。

### 关卡路线图

| 关卡         | 核心积木           | 示例最优解                              |
| ------------ | ------------------ | --------------------------------------- |
| 1 Hello Sort | Sort               | `Sort<Array<_INPUT_>><NaturalOrder>`    |
| 2 堆的秘密   | PrioQueue, PopEach | `Array<PopEach<PrioQueue<_INPUT_>>>`    |
| 3 等价的路   | Stack, Reverse     | `Array<PopEach<Stack<_INPUT_>>>`        |
| 4 先来先到   | Queue              | `Array<PopEach<Queue<_INPUT_>>>`        |
| 5 翻倍工厂   | Map                | `Array<Map<_INPUT_><DoubleOp>>`         |
| 6 筛选车间   | Filter             | `Array<Filter<_INPUT_><EvenPred>>`      |
| 7 折叠工厂   | Zip                | `Zip<_INPUT_><3>`                       |
| 8 展开车间   | Flat               | `Flat<Zip<_INPUT_><2>>`                 |
| 9 综合流水线 | 多积木组合         | `Array<Filter<Map<_INPUT_><Op>><Pred>>` |
| 10 无提示间  | 自由               | 无 `optimal_size` 约束，无提示          |

---

## 10 Mod 系统

### 工作流程

```
mods/my-block.jar
      │
      ▼
URLClassLoader 扫描 jar 内所有 .class
      │
      ▼
isAssignableFrom(Block.class)?
      │ Yes
      ▼
读取 @BlockMeta 注解（反射）
      │
      ▼
BlockRegistry.MAP.put(meta.name(), clazz)
      │
      ▼
Tab 补全、Parser 均可使用该积木
```

### Mod 最小实现示例

```java
// 编译依赖：game-api.jar（仅此一个 jar）
@BlockMeta(
    name        = "BFS",
    signature   = "List<List<T>> × T → List<T>",
    description = "图上广度优先搜索，返回到达目标节点的路径",
    arity       = 2
)
public class BFSBlock extends BinaryBlock<List<?>, Object, List<?>> {
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        List<?> graph  = (List<?>) left.evaluate(ctx);   // Zip 构造的邻接表
        Object  target = right.evaluate(ctx);
        // BFS 实现……
        return path;
    }
}
```

### Mod 编译管道

游戏提供一键编译脚本，Mod 作者无需手动 `javac`：

```
tools/
└── mod-compile.sh
    # javac -cp game-api.jar -d out/ src/*.java
    # jar cf mods/my-block.jar -C out/ .
    # 重启游戏自动热加载
```

---

## 11 IPC 与进程模型

**本项目是单 JVM 单进程架构，无 IPC。**

`game-core`（引擎）和 `game-gl`（渲染）在同一 JVM 内通过直接方法调用交互，线程模型如下：

```
主线程（渲染线程）                游戏逻辑线程
────────────────                ────────────────
glfwInit()                      while (true) {
GLFW 窗口创建                      InputEvent e = queue.take();  // 阻塞
启动游戏逻辑线程                    processEvent(e);
                                   updateBuffer(terminalBuffer);
while (!glfwWindowShouldClose) {  }
  pollEvents();
  compositor.frame(buffer, fx);
  glfwSwapBuffers();
}
```

`terminalBuffer` 是两个线程共享的数据，需要做简单的读写锁保护（或使用双缓冲）。

---

## 12 开发路径

### 里程碑

| 阶段   | 后端（game-core）                   | 前端（game-gl）                      | 完成标志                              |
| ------ | ----------------------------------- | ------------------------------------ | ------------------------------------- |
| **M1** | Block 类层次 + 所有内置积木 + JUnit | GLFW 窗口 + FontAtlas + 基础字符渲染 | 11 个积木单元测试全绿；终端可显示文字 |
| **M2** | Lexer + Parser + BlockRegistry      | 输入事件队列；基础 REPL 循环         | 能解析并求值第 1-4 关示例解           |
| **M3** | 关卡 JSON 加载 + Judge + 评分       | 语法高亮；Tab 补全浮层               | 前 4 关完整可游玩                     |
| **M4** | EvalContext trace 流式输出          | ASCII 可视化播放器；WA trace 树渲染  | WA 后能看到逐步 trace                 |
| **M5** | Mod 系统（URLClassLoader）          | PixiJS 特效层接入；光标拖尾 Shader   | 外部 BFSBlock Mod 成功热加载          |
| **M6** | 第 5-10 关内容 + Map/Filter 积木    | AC 光波 + WA 闪屏特效；主题系统      | 全 10 关可三星通关                    |
| **M7** | 存档序列化；关卡验证工具            | 高 DPI 压测；补间动画打磨            | 可提交评分版本                        |

### 各阶段关键风险

| 阶段 | 风险                              | 应对                                                            |
| ---- | --------------------------------- | --------------------------------------------------------------- |
| M1   | FontAtlas 字形对齐偏差            | 用 stb_truetype 的 `stbtt_GetBakedQuad` 直接获取 UV，勿手动计算 |
| M2   | GLFW 与游戏逻辑线程的 Buffer 竞争 | 使用 `AtomicReference` 交换 Buffer，渲染线程只读                |
| M3   | Map/Filter 的 Predicate 积木设计  | Predicate 本身也是 `Block<Boolean>`，语法与其他积木一致         |
| M4   | trace 帧数过少（3 帧太快）        | PrioQueue 每次 poll() 发一帧，玩家可用 `←/→` 逐帧步进           |
| M5   | Mod ClassLoader 访问引擎内部      | game-api 独立 jar；`EvalContext.consumeStep()` 做步数沙箱       |
| M6   | GLSL Shader 在不同显卡行为不一致  | 限定 OpenGL 4.3 core profile；测试时用 Mesa（软件渲染）验证     |

---

## 13 AI 辅助指导

### 最适合 AI 生成的模块

| 模块                    | AI 辅助效果           | 你需要提供                  |
| ----------------------- | --------------------- | --------------------------- |
| GLSL Shader             | ⭐⭐⭐⭐⭐ 完全可生成 | uniform 接口定义 + 效果描述 |
| FontAtlas               | ⭐⭐⭐⭐ 高度模板化   | 目标字体路径 + Atlas 尺寸   |
| TerminalBuffer + Widget | ⭐⭐⭐⭐ 逻辑清晰     | Cell 数据结构定义           |
| BlockRegistry 反射代码  | ⭐⭐⭐⭐ 模板化       | `@BlockMeta` 接口定义       |
| JUnit 测试用例          | ⭐⭐⭐⭐⭐ 可批量生成 | 积木的输入/输出样例         |
| 关卡 JSON               | ⭐⭐⭐⭐ 可批量设计   | 积木范围 + 难度约束         |
| Lexer / Parser          | ⭐⭐⭐ 需验证边界     | 文法 EBNF                   |

### 推荐的 Prompt 模式

**Shader 生成**：

```
实现一个 GLSL 4.30 fragment shader，效果是：
以 uniform vec2 u_origin（归一化坐标）为圆心，
向外扩散一个发光环，扩散速度由 uniform float u_time 控制，
环的宽度约为 0.02，颜色为 #22dd50，
在 u_time > 0.8 秒后 alpha 衰减至 0。
输出到 out vec4 fragColor。
```

**积木实现**：

```
实现 Java 类 FilterBlock，继承 BinaryBlock<List<?>, Block<Boolean>, List<?>>。
left 子积木返回 List<?>，right 子积木是 Predicate，
接受单个元素（Integer）返回 Boolean。
evaluate() 对 left 的每个元素调用 right.evaluate()，
保留返回 true 的元素，返回 ArrayList。
记得调用 ctx.consumeStep()，并 log 中间结果。
```

### 人工重点审查项

AI 生成后必须人工验证的地方：

1. **SSBO 内存布局**：Java 侧 `ByteBuffer` 的 pack 顺序必须与 GLSL `std430` 布局严格对齐
2. **Transform Feedback 的 VBO 双缓冲**：ping-pong buffer 容易写错导致渲染撕裂
3. **URLClassLoader 的 parent delegation**：确认 Mod 无法访问 `game-core` 的内部类
4. **EvalContext 的线程安全**：每次 `evaluate()` 调用必须使用独立的 `EvalContext` 实例

---

## 14 设计决策记录

### ADR-01 为什么移除 BFSBlock / DFSBlock？

BFS 本质上是 `Queue + While循环 + 访问标记数组`。本游戏的积木系统没有循环控制和可变局部状态，BFSBlock 只是把复杂性藏进黑箱。内置 BFSBlock 会让玩家在不理解 BFS 的情况下通过图论关卡。移除后以 Mod 形式存在，供高玩扩展。

### ADR-02 为什么选 LWJGL3 而不是 JavaFX？

JavaFX 无法做真正的 GLSL Shader——它的 GPU 通道止步于 CSS Effect，无法编写自定义顶点和片元着色器。光标拖尾粒子需要 Transform Feedback，全屏光波需要 SDF Fragment Shader，这些在 JavaFX 里不可实现。

### ADR-03 为什么用单进程而不是 Java 后端 + 前端进程？

单进程消除了 IPC 协议设计、序列化、网络延迟等所有跨进程问题，渲染层直接持有引擎对象的引用。游戏逻辑和渲染通过 `BlockingQueue` 解耦已经足够，不需要引入更重的通信机制。

### ADR-04 ReverseBlock 和 StackBlock 功能重复，为什么都保留？

功能等价，但教学意图不同。`StackBlock` 用于教容器语义（LIFO），关卡会用 `forced_blocks` 强制使用；`ReverseBlock` 作为「反转」变换的直接表达，用于后期综合关卡。两者并存让关卡设计者能精确控制玩家注意力。

### ADR-05 为什么不引入随机性？

速通机制要求低随机性：玩家通过反复练习对特定输入建立肌肉记忆，随机输入会破坏速通可重复性。所有关卡使用固定输入，随机数种子仅在「无提示间」（第 10 关）作为可选变体引入。

### ADR-06 Map/Filter 的「函数参数」如何解决？

`Predicate` 和 `Function` 本身也是 `Block`。`EvenPredBlock` 继承 `UnaryBlock<Integer, Boolean>`，作为 `FilterBlock` 的右子积木传入。这样函数参数和其他积木在语法上完全一致，Parser 不需要引入新的语法节点。

---

_AlgoBlock Technical Document v1.0 · 2026-04-12_
