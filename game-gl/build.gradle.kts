plugins {
    application
    id("com.gradleup.shadow") version "9.3.1"
}

val lwjglVersion = "3.3.4"
val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()
val lwjglNatives = when {
    osName.contains("win") -> "natives-windows"
    osName.contains("linux") -> when {
        osArch.contains("aarch64") || osArch.contains("arm64") -> "natives-linux-arm64"
        osArch.contains("arm") -> "natives-linux-arm32"
        osArch.contains("ppc") -> "natives-linux-ppc64le"
        osArch.contains("riscv") -> "natives-linux-riscv64"
        else -> "natives-linux"
    }
    osName.contains("mac") || osName.contains("darwin") -> when {
        osArch.contains("aarch64") || osArch.contains("arm64") -> "natives-macos-arm64"
        else -> "natives-macos"
    }
    else -> throw GradleException("Unsupported OS for LWJGL natives: os.name=$osName, os.arch=$osArch")
}

dependencies {
    implementation(project(":game-core"))
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    implementation("javazoom:jlayer:1.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
}

application {
    mainClass.set("com.algoblock.gl.Main")
}
