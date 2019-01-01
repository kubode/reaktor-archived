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
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.12")
}
