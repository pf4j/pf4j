val pluginsDir by extra { file("$buildDir/plugins") }

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.21"))
    }
}

plugins {
    kotlin("jvm") version "1.4.21"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}

tasks.named("build") {
    dependsOn(":app:uberJar")
}
