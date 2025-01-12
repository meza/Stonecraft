package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.controller.StonecutterController
import org.gradle.api.Project

/**
 * Configures chiseled (stonecutter) tasks for the given Gradle project.
 *
 * @param project The Gradle project to configure tasks for.
 */
fun configureChiseledTasks(project: Project, stonecutterController: StonecutterController) {
    // @TODO: Extract this setting to an extension that works on the stonecutter controller file
    val allModsGroup = "modsAll"
    stonecutterController.automaticPlatformConstants = true

    // List of task names and their corresponding `ofTask` values
    val tasks = listOf(
        "chiseledBuild" to "build",
        "chiseledClean" to "clean",
        "chiseledDatagen" to "runDatagen",
        "chiseledTest" to "test",
        "chiseledGameTest" to "runGameTestServer",
        "chiseledBuildAndCollect" to "buildAndCollect",
        "chiseledPublishMods" to "publishMods"
    )

    // Register each task with the project and the StonecutterController
    tasks.forEach { (taskName, ofTask) ->
        stonecutterController.registerChiseled(
            project.tasks.register(taskName, stonecutterController.chiseled) {
                group = allModsGroup
                description = "Executes $ofTask for all versions and loaders"
                ofTask(ofTask)
            }
        )
    }
}
