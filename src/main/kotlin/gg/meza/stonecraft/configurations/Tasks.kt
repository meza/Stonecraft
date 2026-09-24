package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.MinecraftObfuscation
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.mod
import gg.meza.stonecraft.tasks.ConfigureMinecraftClient
import net.fabricmc.loom.task.DownloadAssetsTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

fun configureTasks(
    project: Project,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
    minecraftObfuscation: MinecraftObfuscation,
) {
    val currentModGroup = "mod"
    val buildAndCollect = project.tasks.register<Copy>("buildAndCollect") {
        val jarTask = resolveJarTask(project, minecraftObfuscation)
        group = "build"
        from(jarTask.flatMap { it.archiveFile })
        into(project.rootProject.layout.buildDirectory.file("libs"))
        dependsOn("build", jarTask)
    }

    project.tasks.named("jar", Jar::class.java) {
        mustRunAfter(project.tasks.named("runDatagen"))
    }
    val runtimeTasks = setOf("runClient", "runServer", "runGameTestClient", "runGameTestServer")
    project.tasks.matching { it.name in runtimeTasks }.configureEach {
        mustRunAfter(project.tasks.named("runDatagen"))
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
            dependsOn(project.tasks.named("runGameTestClient"))
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

    project.tasks.named("runGameTestClient") {
        dependsOn(project.tasks.named("configureMinecraftTestClient"))
    }

    // Version projects share the configured game run directory, but each download task must own distinct outputs.
    project.tasks.withType<DownloadAssetsTask>().configureEach {
        legacyResourcesDirectory.set(project.layout.projectDirectory.file("run/resources"))
    }

}
