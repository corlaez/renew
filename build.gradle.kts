import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.10"
    `maven-publish`
}

group = "com.corlaez"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    fun kotlinx(suffix: String) = "org.jetbrains.kotlinx:kotlinx-$suffix"
    val coroutinesVersion = "1.6.0"

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlinx("coroutines-core:$coroutinesVersion"))

    testImplementation(kotlinx("coroutines-core-jvm:$coroutinesVersion"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
//            groupId = "com.corlaez"
//            artifactId = "renew"
//            version = "0.1.0"
            from(components["java"])
        }
    }
}
