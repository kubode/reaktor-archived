plugins {
    java
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    testImplementation(kotlin("test"))
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.0-rc.1")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.0-rc.1")
    testRuntimeOnly(kotlin("reflect"))
}
