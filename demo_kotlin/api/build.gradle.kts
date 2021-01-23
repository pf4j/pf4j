val pf4jVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.pf4j:pf4j:${pf4jVersion}")
}
