import net.thauvin.erik.gradle.semver.*
import org.jetbrains.dokka.gradle.*

plugins {
    kotlin("jvm") version "1.3.72"
    //id("com.gorylenko.gradle-git-properties") version "2.2.2"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("org.jetbrains.dokka") version "0.10.1"
    `java-library`
    `maven-publish`
}

group = "net.masterzach32"
version = getVersionFromSemver()

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/ktor")
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.ktor:ktor-client-cio:1.3.2")
    implementation("io.ktor:ktor-client-okhttp:1.3.2")
    implementation("org.jsoup:jsoup:1.11.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
sourceSets["main"].resources.srcDirs("resources")

val incrementBuildMeta by tasks.existing(SemverIncrementBuildMetaTask::class) {
    doFirst {
        buildMeta = (buildMeta.toInt() + 1).toString()
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from((project.the<SourceSetContainer>()["main"] as SourceSet).allSource)
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/dokka"

    subProjects = project.subprojects.toList().map { it.name }
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            artifactId = project.name
            version = getVersionFromSemver()
            from(components["kotlin"])
            artifact(sourcesJar.get())
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}

fun getVersionFromSemver() = file("version.properties")
    .readLines()
    .first { it.contains("version.semver") }
    .split("=")
    .last()
    .trim()