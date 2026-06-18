package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.mod
import gg.meza.stonecraft.tasks.ConfigureMinecraftClient
import net.fabricmc.loom.task.DownloadAssetsTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

fun configureTasks(project: Project, realMinecraftVersion: String, stonecutter: StonecutterBuildExtension, modSettings: ModSettingsExtension) {
    val currentModGroup = "mod"
    val buildAndCollect = project.tasks.register<Copy>("buildAndCollect") {
        val jarTask = resolveJarTask(project, stonecutter, realMinecraftVersion)
        group = "build"
        from(jarTask.flatMap { it.archiveFile })
        into(project.rootProject.layout.buildDirectory.file("libs"))
        dependsOn("build", jarTask)
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

    project.tasks.withType<DownloadAssetsTask>().configureEach {
        legacyResourcesDirectory.set(project.layout.projectDirectory.file("run/resources"))
    }

    if (project.mod.isNeoforge) {
        // Necessary to enable minecraft facing unit test facilities
        project.tasks.withType<Test>().configureEach {
            jvmArgs("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
            systemProperty(
                "fml.modFolders",
                listOf(
                    "main%%${project.layout.buildDirectory.dir("resources/main").get().asFile.absolutePath}",
                    "main%%${project.layout.buildDirectory.dir("classes/java/main").get().asFile.absolutePath}",
                    "main%%${project.layout.buildDirectory.dir("classes/java/test").get().asFile.absolutePath}",
                ).joinToString(File.pathSeparator)
            )
        }
    }
}
