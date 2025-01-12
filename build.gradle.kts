import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree.Companion.test
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "2.1.20-Beta1"
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.adarshr.test-logger") version "4.0.0"
}

group = "gg.meza"

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven("https://maven.kikugie.dev/releases")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    compileOnly(gradleApi())
    fun plugin(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"
    implementation(plugin("dev.kikugie.stonecutter", "0.5"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(plugin("dev.architectury.loom", "1.9.+"))
    implementation(plugin("me.modmuss50.mod-publish-plugin", "0.+"))

    testImplementation(gradleTestKit())
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
    testImplementation("net.bytebuddy:byte-buddy:LATEST")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))

}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    jvmArgs("-Xmx4G")
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
    plugins {
        create("gg.meza.stonecraft") {
            id = "gg.meza.stonecraft"
            displayName = "Stonecraft"
            implementationClass = "gg.meza.stonecraft.ModPlugin"
            tags = listOf("minecraft", "multi-loader", "configuration", "modding", "minecraft modding", "development environment configuration")
            description = "Stonecraft is a configuration Gradle plugin that removes the boilerplate of setting up a **multi-loader, multi-version** Minecraft modding workspace."
        }
    }
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.compilerOptions {
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    jvmTarget.set(JvmTarget.JVM_21)
    freeCompilerArgs.addAll(listOf("-opt-in=kotlin.ExperimentalStdlibApi", "-opt-in=kotlin.RequiresOptIn"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
