buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.0-alpha09")
        classpath(kotlin("gradle-plugin", version = "1.3.10"))
    }
}

plugins {
    base
    kotlin("jvm") version "1.3.0" apply false
}

allprojects {
    group = "com.github.kubode.reaktor"
    version = "1.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}
