package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuild
import gg.meza.stonecraft.canBeLaunchedByArchitectury
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.mod
import gg.meza.stonecraft.tasks.ConfigureMinecraftClient
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import kotlin.math.min

fun configureTasks(project: Project, stonecutter: StonecutterBuild, modSettings: ModSettingsExtension) {
    val currentModGroup = "mod"
    val minecraftVersion = stonecutter.current.version

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

    project.tasks.named("runClient") {
        dependsOn(project.tasks.named("configureMinecraftClient"))
    }
}


