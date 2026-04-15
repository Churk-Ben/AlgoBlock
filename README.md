# AlgoBlock

AlgoBlock 是一个基于 Java 的算法解密游戏原型，当前实现覆盖 M1\~M3 的核心能力：

- Gradle 多模块：`game-api`、`game-core`、`game-gl`
- 积木合约、内置积木、解析器、关卡加载、判题与评分
- 基础 GLFW 窗口、输入事件队列、终端缓冲、语法高亮与补全骨架

## 🚀 快速开始

### 环境要求

- **JDK 21** 或更高版本
- 支持 OpenGL 的显卡

### 首次运行

```bash
# 1. 克隆项目
# 2. 构建项目（自动下载Gradle）
./gradlew build

# 3. 运行游戏
java -jar game-gl/build/libs/game-gl-0.1.0-all.jar
```

## 📋 构建配置（已最终锁定）

### Gradle 环境

- **Gradle Wrapper**: 8.9 (稳定版本)
- **Shadow 插件**: 9.3.1 (fat jar打包)
- **LWJGL 版本**: 3.3.4 (图形库)
- **Java 兼容**: JDK 21

### 构建命令

#### 完整构建

```bash
./gradlew clean build
```

#### 生成可执行jar

```bash
./gradlew :game-gl:shadowJar
```

#### 运行测试

```bash
./gradlew test
```

#### 启动图形界面

```bash
./gradlew :game-gl:run
```

## 📁 项目结构

```
AlgoBlock/
├── game-api/     # 游戏API模块 - 积木合约定义
├── game-core/    # 游戏核心逻辑 - 解析器、判题、关卡管理
├── game-gl/      # 图形界面模块 - GLFW窗口、终端渲染
├── assets/       # 资源文件 - 字体、音效、着色器
└── docs/         # 项目文档
```

## 🎮 功能特性

### 核心能力 (M1-M3)

- ✅ **积木合约系统** - 标准化的积木接口定义
- ✅ **内置积木库** - 丰富的算法积木集合
- ✅ **解析器引擎** - 代码解析与执行
- ✅ **关卡管理系统** - 多级别难度设计
- ✅ **实时判题** - 算法正确性验证
- ✅ **评分系统** - 性能与正确性综合评分

### 图形界面

- ✅ **GLFW 窗口管理** - 跨平台图形窗口
- ✅ **输入事件队列** - 键盘、鼠标输入处理
- ✅ **终端缓冲区** - 文本渲染与显示
- ✅ **语法高亮** - 代码语法可视化
- ✅ **自动补全** - 智能代码补全骨架

## 📦 输出文件

### 可执行jar位置

```
game-gl/build/libs/game-gl-0.1.0-all.jar
```

**文件信息**:

- 大小: \~163MB (包含所有依赖)
- 类型: Fat Jar (可独立运行)
- 运行: `java -jar game-gl-0.1.0-all.jar`

## 🔧 开发指南

### 新开发者入门

1. 确保安装 JDK 21
2. 克隆项目到本地
3. 运行 `./gradlew build` 自动配置环境
4. 开始开发或运行游戏

### 日常开发命令

```bash
# 快速构建并生成jar
./gradlew :game-gl:shadowJar

# 运行特定测试
./gradlew :game-core:test

# 清理重建
./gradlew clean build

# 运行项目
./gradlew run
```

## 🐛 故障排除

### 常见问题

1. **构建失败**
   ```bash
   # 删除缓存后重新构建
   rm -rf .gradle
   ./gradlew clean build
   ```
2. **依赖下载慢**
   - 检查网络连接
   - 或配置国内镜像仓库
3. **GLFW 初始化失败**
   - 确保显卡驱动支持 OpenGL
   - 更新显卡驱动程序

### 技术支持

- 查看详细文档: `docs/` 目录

## 📄 许可证

本项目采用 [Apache 2.0 许可证](LICENSE)。详情见 LICENSE 文件。

---

_最后更新: 2026-04-15_
