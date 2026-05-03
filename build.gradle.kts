import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.plugin.publish) apply false
    alias(libs.plugins.test.logger)
}

group = "gg.meza"
description =
    "Stonecraft is a configuration Gradle plugin that removes the boilerplate of setting up a multi-loader, multi-version Minecraft modding workspace."

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven("https://maven.kikugie.dev/releases")
    maven("https://maven.kikugie.dev/snapshots")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    compileOnly(gradleApi())

    implementation(libs.stonecutter)
    implementation(libs.gson)
    implementation(libs.architectury.loom)
    implementation(libs.mod.publish)
    implementation(libs.okio)

    constraints {
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
    }

    testImplementation(gradleTestKit())
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.junit)
    testImplementation(libs.junit.pioneer)

    testImplementation(kotlin("test"))

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    maxHeapSize = "4G"
}

testlogger {
    theme = ThemeType.PLAIN
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

gradlePlugin {
    website = "https://stonecraft.meza.gg"
    vcsUrl = "https://github.com/meza/Stonecraft.git"

    testSourceSet(sourceSets["test"])

    plugins {
        create("gg.meza.stonecraft") {
            id = "gg.meza.stonecraft"
            displayName = "Stonecraft"
            implementationClass = "gg.meza.stonecraft.ModPlugin"
            tags = listOf(
                "minecraft",
                "multi-loader",
                "configuration",
                "modding",
                "minecraft modding",
                "development environment configuration"
            )
            description = project.description
        }
    }
}

kotlin {
    jvmToolchain(21)
}

val isPrereleaseChannel = providers.environmentVariable("STONECRAFT_PRERELEASE")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)

if (!isPrereleaseChannel.get()) {
    apply(plugin = "com.gradle.plugin-publish")
}

publishing {

    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.meza"
            artifactId = "stonecraft"
            version = project.version.toString()
            from(components["java"])
        }
    }

    repositories {
        mavenLocal()
        if (isPrereleaseChannel.get()) {
            maven {
                name = "reposilite"

                val versionTag = project.version.toString().lowercase()
                val target = if (listOf("alpha", "beta", "rc", "snapshot", "dev", "next").any { it in versionTag }) {
                    "snapshots"
                } else {
                    "releases"
                }

                url = uri("https://maven.meza.gg/$target")

                credentials(PasswordCredentials::class) {
                    username = providers.environmentVariable("MEZA_MAVEN_USER").get()
                    password = providers.environmentVariable("MEZA_MAVEN_PASSWORD").get()
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
