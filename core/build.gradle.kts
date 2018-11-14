import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    java
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0")
    implementation("com.squareup:kotlinpoet:1.0.0-RC1")
    testImplementation("junit:junit:4.12")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.0-rc.1")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.0-rc.1")
    testRuntimeOnly(kotlin("reflect"))
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}
