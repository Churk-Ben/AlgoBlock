plugins {
    base
}

allprojects {
    group = "com.algoblock"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    tasks.withType<JavaCompile>().configureEach {
        // Allow local JDK 21/26 builds while emitting Java 21-compatible bytecode.
        options.release.set(21)
    }
    tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
        useJUnitPlatform()
    }
}
