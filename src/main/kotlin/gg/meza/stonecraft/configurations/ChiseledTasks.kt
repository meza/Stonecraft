package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import org.gradle.api.Project

/**
 * Configures the public `modsAll` wrappers so downstream builds can continue to run the legacy
 * `chiseled*` tasks while we delegate to Stonecutter's task aggregation API.
 */
fun configureChiseledTasks(project: Project, controller: StonecutterControllerExtension) {
    val allModsGroup = "modsAll"

    data class Wrapper(val name: String, val target: String)

    val wrappers = listOf(
        Wrapper("chiseledBuild", "build"),
        Wrapper("chiseledClean", "clean"),
        Wrapper("chiseledDatagen", "runDatagen"),
        Wrapper("chiseledTest", "test"),
        Wrapper("chiseledGameTest", "runGameTestServer"),
        Wrapper("chiseledBuildAndCollect", "buildAndCollect"),
        Wrapper("chiseledPublishMods", "publishMods")
    )

    wrappers.forEach { (wrapperName, targetTask) ->
        project.tasks.register(wrapperName) {
            group = allModsGroup
            description = "Executes $targetTask for all versions and loaders"
            dependsOn(controller.tasks.named(targetTask))
        }
    }

    controller.tasks {
        order("buildAndCollect")
        order("publishMods")
    }
}
