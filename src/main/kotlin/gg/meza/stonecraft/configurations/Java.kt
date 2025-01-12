package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuild
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.mod
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure

fun configureJava(project: Project, stonecutter: StonecutterBuild, modSettingsExtension: ModSettingsExtension) {
    val generatedResources = modSettingsExtension.generatedResourcesProp

    // Configure the compile time
    project.project.configure<JavaPluginExtension> {
        // Configure the Java plugin to use the correct Java version for the given Minecraft version
        val javaVersion = javaVersion(stonecutter)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    }

    project.afterEvaluate {
        project.project.configure<JavaPluginExtension> {
            // Add the generated resources directory to the resources source set for ForgeLike mods
            // This is to allow them to read the generated resources
            if (project.mod.isForgeLike) {
                sourceSets.named("main").get().resources.srcDir(generatedResources.get())
            }
        }
    }
}

private fun javaVersion(stonecutter: StonecutterBuild): JavaVersion {
    val j21 = stonecutter.eval(stonecutter.current.version, ">=1.20.6")
    return if (j21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
}
