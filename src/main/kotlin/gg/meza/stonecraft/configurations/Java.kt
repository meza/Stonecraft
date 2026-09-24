package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.extension.ModSettingsExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

fun configureJava(project: Project, stonecutter: StonecutterBuildExtension, modSettingsExtension: ModSettingsExtension) {
    // Configure the compile time
    project.project.configure<JavaPluginExtension> {
        // Configure the Java plugin to use the correct Java version for the given Minecraft version
        val javaVersion = javaVersion(stonecutter)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    }

    project.afterEvaluate {
        val generatedDirectories = generatedResourceDirectories(project, stonecutter, modSettingsExtension)
        val generatedFiles = project.files(generatedDirectories)

        project.tasks.named("jar", Jar::class.java) {
            generatedDirectories.forEach { from(it) }
        }
        val runtimeTasks = setOf("runClient", "runServer", "runGameTestClient", "runGameTestServer")
        project.tasks.withType<JavaExec>().configureEach {
            if (name in runtimeTasks) {
                classpath(generatedFiles)
            }
        }
        project.tasks.withType<Test>().configureEach {
            classpath = classpath.plus(generatedFiles)
            mustRunAfter(project.tasks.named("runDatagen"))
        }
    }
}

private fun javaVersion(stonecutter: StonecutterBuildExtension): JavaVersion {
    val j21 = stonecutter.eval(stonecutter.current.version, ">=1.20.6")
    val j25 = stonecutter.eval(stonecutter.current.version, ">=21.6")
    return if (j25) {
        JavaVersion.VERSION_25
    } else if (j21) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_17
    }
}
