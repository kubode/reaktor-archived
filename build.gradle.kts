plugins {
    base
    kotlin("jvm") version "1.2.71" apply false
}

allprojects {
    group = "com.github.kubode.reaktor"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
    }
}
