import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "1.3.10"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.0.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.4.RELEASE"
}

group = "com.tractive"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("javax.validation:validation-api")
    compileOnly("com.github.mongobee:mongobee:0.13")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    enabled = true
}
tasks.withType<BootJar> {
    enabled = false
}
