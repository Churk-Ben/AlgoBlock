# AlgoBlock

AlgoBlock 是一个基于 Java 的算法解密游戏原型，当前实现覆盖 M1~M3 的核心能力：

- Gradle 多模块：`game-api`、`game-core`、`game-gl`
- 积木合约、内置积木、解析器、关卡加载、判题与评分
- 基础 GLFW 窗口、输入事件队列、终端缓冲、语法高亮与补全骨架

## 运行与测试

- 运行核心测试：
  - `gradle :game-core:test`
- 构建全部模块：
  - `gradle build`
- 启动图形模块：
  - `gradle :game-gl:run`
