package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.tasks.ConfigureMinecraftClient
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

fun configureTasks(project: Project, stonecutter: StonecutterBuildExtension, modSettings: ModSettingsExtension) {
    val currentModGroup = "mod"

    val buildAndCollect = project.tasks.register<Copy>("buildAndCollect") {
        val remapJar = project.tasks.named<RemapJarTask>("remapJar")
        group = "build"
        from(remapJar.get().archiveFile)
        into(project.rootProject.layout.buildDirectory.file("libs"))
        dependsOn("build")
    }

    if (stonecutter.current.isActive) {
        project.rootProject.tasks.register("buildActive") {
            group = currentModGroup
            dependsOn(buildAndCollect)
        }
        project.rootProject.tasks.register("runActive") {
            group = currentModGroup
            dependsOn(project.tasks.named("runClient"))
        }

        project.rootProject.tasks.register("runActiveServer") {
            group = currentModGroup
            dependsOn(project.tasks.named("runServer"))
        }

        project.rootProject.tasks.register("dataGenActive") {
            group = currentModGroup
            dependsOn(project.tasks.named("runDatagen"))
        }

        project.rootProject.tasks.register("testActiveClient") {
            group = currentModGroup
            dependsOn(project.tasks.named("runGameTestClient"), project.tasks.named("configureMinecraftTestClient"))
        }
        project.rootProject.tasks.register("testActiveServer") {
            group = currentModGroup
            dependsOn(project.tasks.named("runGameTestServer"))
        }
    }

    project.tasks.register<ConfigureMinecraftClient>("configureMinecraftClient") {
        val runDirAsFile = modSettings.runDirectoryProp.get().asFile

        if (!runDirAsFile.exists()) {
            runDirAsFile.mkdirs()
        }

        clientOptions.set(modSettings.clientOptions.getOptions())
        runDirectory.set(modSettings.runDirectoryProp)

        dependsOn(project.tasks.named("downloadAssets"))
    }

    project.tasks.register<ConfigureMinecraftClient>("configureMinecraftTestClient") {
        val runDirAsFile = modSettings.testClientRunDirectoryProp.get().asFile

        logger.debug("Run directory: ${runDirAsFile.absolutePath}")

        if (!runDirAsFile.exists()) {
            runDirAsFile.mkdirs()
        }

        clientOptions.set(modSettings.clientOptions.getOptions())
        runDirectory.set(modSettings.testClientRunDirectoryProp)

        dependsOn(project.tasks.named("downloadAssets"))
    }

    project.tasks.named("runClient") {
        dependsOn(project.tasks.named("configureMinecraftClient"))
    }
}
