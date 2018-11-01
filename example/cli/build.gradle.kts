import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

application {
    mainClassName = "com.github.kubode.reaktor.example.cli.Main"
}

dependencies {
    implementation(project(":core"))
    kapt(project(":compiler"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.30.2")
    implementation("com.squareup:kotlinpoet:1.0.0-RC1")
    testImplementation("junit:junit:4.12")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}
