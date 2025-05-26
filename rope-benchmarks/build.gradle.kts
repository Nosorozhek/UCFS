plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    // benchmark tool
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.13")
    // rope implementation
    implementation(project(":solver"))
}

task("runBenchmark", JavaExec::class) {
    benchmark {
        configurations {
            named("main") {
                reportFormat = "json"
            }
        }
        targets {
            register("main")
        }
    }
}

