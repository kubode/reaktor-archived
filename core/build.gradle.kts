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
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}
