import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.test.logger)
//    alias(libs.plugins.spotless)
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
}

testlogger {
    theme = ThemeType.PLAIN
}

java {
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

val compileKotlin: KotlinCompile by tasks

compileKotlin.compilerOptions {
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0) // Changed from KOTLIN_2_1
    jvmTarget.set(JvmTarget.JVM_21)
    freeCompilerArgs.addAll(listOf("-opt-in=kotlin.ExperimentalStdlibApi", "-opt-in=kotlin.RequiresOptIn"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

//spotless {
//    kotlin {
//        ktlint()
//            .editorConfigOverride(
//                mapOf(
//                    "ktlint_code_style" to "intellij_idea",
//                    "ktlint_standard_no-line-break-before-assignment" to "disabled",
//                    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
//                    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled"
//                )
//            )
//    }
//}

publishing {
    repositories {
        mavenLocal()
    }
}
