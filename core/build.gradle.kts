plugins {
    java
    kotlin("jvm")
}

dependencies {
    implementation(project("core"))
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.0.0-RC1")
    testImplementation("junit:junit:4.12")
}
