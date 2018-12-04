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
    `maven-publish`
}

group = "com.github.tractive"
version = "0.2.0-SNAPSHOT"

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

val sourcesJarTask = tasks.create<Jar>("sourceJar") {
    from(sourceSets["main"].allSource)
    classifier = "sources"
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            pom {
                url.set("https://github.com/Tractive/mongobee-spring")
                organization {
                    name.set("Tractive GmbH")
                    url.set("https://tractive.com")
                }
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
            }
            from(components["java"])
            artifact(sourcesJarTask)
        }
    }
}