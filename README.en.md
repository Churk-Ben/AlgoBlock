# AlgoBlock

[中文](README.md) | English

![AlgoBlock Game Screenshot](./assets/banner.gif)

## Introduction

AlgoBlock is a declarative sequence transformation puzzle game. Players solve level challenges by nesting and combining "blocks" to write expressions that transform a given integer sequence into a target sequence.

### Game Mechanics

The core gameplay revolves around encapsulating complex algorithmic logic into individual "atomic blocks". In specific levels, players can invoke built-in blocks or extended blocks created via MODs. These blocks feature distinct names, parameter lists, data processing logic, and various forms of output.

To ensure a unified interface and gaming experience, we focus primarily on a class of fundamental linear transformation algorithms. Through nested commands in a geek-style terminal full of light and shadow effects, players can experience the joy of "solving complex problems with a single line of command".

### Technical Features

- **Decoupling of Data and Rendering**: The core algorithm logic ("how to calculate") is completely separated from visual presentation ("how to draw"). Writing new logic blocks requires no knowledge of underlying OpenGL rendering, greatly lowering the barrier to extension.
- **MOD System**: The game is highly extensible. The MOD engine provides standardized interfaces, allowing developers to freely write custom libraries to implement complete level designs, from puzzles to solutions.
- **Geek-style Rendering Pipeline**: Inspired by works like _The Matrix_ and _Hacknet_. It uses a customized rendering toolchain that supports smooth cursor trailing effects and other cool visual feedback, providing an immersive terminal operation experience.
- **Modular Design**: The project adopts a modular and highly decoupled architecture. It utilizes dependency injection and reflection mechanisms to ensure code standardization and excellent engineering quality.

## Quick Start

### Basic Development Environment Requirements

- **JDK 21** or higher
- **Gradle** matching the JDK version
- A graphics card that supports OpenGL (most modern graphics cards do)

> [!NOTE]
>
> Reference for the build configuration and important dependency versions used on the development machine:
>
> Gradle will automatically download the required dependencies during the first run. (A good network connection is required)
>
> - **Java Version**: JDK 21
> - **Gradle Version**: 8.9
> - **Shadow Plugin Version**: 9.3.1
> - **Graphics Library Version**: LWJGL 3.3.4

### Build and Run

The project uses Gradle for building. Dependencies will be automatically downloaded during the first run. (A good network connection is required)

```bash
# 1. Clone the project and enter the directory
git clone https://github.com/Churk-Ben/AlgoBlock.git
cd AlgoBlock

# 2. Full build (automatically downloads Gradle and dependencies)
./gradlew clean build

# 3. Run the game GUI
./gradlew :game-gl:run
```

> [!NOTE]
>
> If your network environment is unstable, you can also choose to manually download dependencies and build:
>
> 1. Properly configure the JDK and Gradle suitable for your current system.
> 2. Use Gradle to build the project:
>
> ```bash
> gradle clean build
> ```
>
> 3. Run the game GUI:
>
> ```bash
> gradle :game-gl:run
> ```

### Common Build Commands

| Operation       | Command                        | Description                                                    |
| --------------- | ------------------------------ | -------------------------------------------------------------- |
| **Full Build**  | `./gradlew clean build`        | Cleans and compiles the entire project                         |
| **Run Tests**   | `./gradlew test`               | Runs all unit tests                                            |
| **Start Game**  | `./gradlew :game-gl:run`       | Directly starts the graphical interface for debugging          |
| **Package App** | `./gradlew :game-gl:shadowJar` | Generates an executable `jar` file containing all dependencies |

> After successfully packaging with `shadowJar`, you can run the game independently using the following command:
>
> `java -jar game-gl/build/libs/game-gl-0.1.0-all.jar`

## License

This project is licensed under the [Apache 2.0 License](LICENSE). See the LICENSE file for details.
