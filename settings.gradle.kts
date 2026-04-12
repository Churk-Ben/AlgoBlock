plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "algoblock"

include("game-api")
include("game-core")
include("game-gl")
