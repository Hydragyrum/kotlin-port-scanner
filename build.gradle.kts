import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
    id("com.palantir.graal") version "0.10.0"
    id ("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

group = "net.hydrashead"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.6.2")
    implementation("commons-net:commons-net:3.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("me.tongfei:progressbar:0.9.2")
    kapt("info.picocli:picocli-codegen:4.6.2")
    testImplementation(kotlin("test"))
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

tasks.shadowJar {
    minimize()
}