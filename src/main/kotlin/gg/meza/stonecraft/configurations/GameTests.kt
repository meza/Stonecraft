package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.MinecraftObfuscation
import gg.meza.stonecraft.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.RunConfiguration
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByType

internal const val GAME_TEST_SOURCE_SET_NAME = "gametest"
internal const val GAME_TEST_MODULE_SOURCE_SET_NAME = "gametestModule"

/**
 * Creates the loader-neutral source set used by Stonecraft's Minecraft game-test runs.
 *
 * Game tests can use production classes and dependencies, while their own classes and resources remain outside the
 * production jar. Loom remap configurations are only required while Minecraft is distributed obfuscated.
 */
internal fun configureGameTestSourceSet(
    project: Project,
    minecraftObfuscation: MinecraftObfuscation,
    gameTestModuleName: Provider<String>,
): SourceSet {
    val sourceSets = project.extensions.getByType<JavaPluginExtension>().sourceSets
    val main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    val gameTest = sourceSets.maybeCreate(GAME_TEST_SOURCE_SET_NAME)
    val gameTestModule = sourceSets.maybeCreate(GAME_TEST_MODULE_SOURCE_SET_NAME)

    gameTest.compileClasspath += main.output
    gameTest.runtimeClasspath += main.output
    gameTest.runtimeClasspath += gameTestModule.output

    project.configurations.named(gameTest.compileClasspathConfigurationName) {
        extendsFrom(project.configurations.getByName(main.compileClasspathConfigurationName))
    }
    project.configurations.named(gameTest.runtimeClasspathConfigurationName) {
        extendsFrom(project.configurations.getByName(main.runtimeClasspathConfigurationName))
    }

    if (minecraftObfuscation == MinecraftObfuscation.MAPPED) {
        project.extensions.getByType<LoomGradleExtensionAPI>().createRemapConfigurations(gameTest)
    }

    if (project.mod.isFabric) {
        project.afterEvaluate {
            project.extensions.getByType<LoomGradleExtensionAPI>().mods {
                maybeCreate(gameTestModuleName.get()).apply {
                    modFiles.from(gameTest.output)
                    modFiles.from(gameTestModule.output)
                    gameTestModule.output.resourcesDir?.let(mainResourceDirectory::set)
                }
            }
        }
    }

    return gameTest
}

/**
 * Makes a game-test run use the isolated source set. Forge-like loaders require all source-set outputs that implement
 * one mod to be grouped explicitly. Legacy annotated tests belong to the dedicated game-test module. On 1.21.5 and
 * later, test resources remain in that module while registrar classes join the production mod whose event bus they
 * subscribe to. Every globally configured production and support module is preserved.
 */
internal fun RunConfiguration.useGameTestSourceSet(
    project: Project,
    gameTest: SourceSet,
    gameTestModuleName: Provider<String>,
    registerGameTestCodeInProductionMod: Boolean,
) {
    sourceSet.set(gameTest.name)

    if (!project.mod.isForgeLike || gameTest.name != GAME_TEST_SOURCE_SET_NAME) {
        return
    }

    project.afterEvaluate {
        val globalMods = project.extensions.getByType<LoomGradleExtensionAPI>().mods
        val gameTestModule = project.extensions.getByType<JavaPluginExtension>().sourceSets
            .getByName(GAME_TEST_MODULE_SOURCE_SET_NAME)

        globalMods.forEach { globalMod ->
            val runMod = mods.maybeCreate(globalMod.name)
            runMod.modFiles.from(globalMod.modFiles)
            runMod.externalGroups.addAll(globalMod.externalGroups)

            if (globalMod.mainResourceDirectory.isPresent && !runMod.mainResourceDirectory.isPresent) {
                runMod.mainResourceDirectory.set(globalMod.mainResourceDirectory)
            }
        }

        mods.maybeCreate(gameTestModuleName.get()).apply {
            gameTestModule.output.resourcesDir?.let { resourcesDirectory ->
                modFiles.from(gameTestModule.output)
                mainResourceDirectory.set(resourcesDirectory)
            }
            if (registerGameTestCodeInProductionMod) {
                gameTest.output.resourcesDir?.let { resourcesDirectory -> modFiles.from(resourcesDirectory) }
            } else {
                modFiles.from(gameTest.output)
            }
        }

        mods.maybeCreate(project.mod.id).apply {
            sourceSet(SourceSet.MAIN_SOURCE_SET_NAME)
            modFiles.from(globalMods.named("main").get().modFiles)
            if (registerGameTestCodeInProductionMod) {
                modFiles.from(gameTest.output.classesDirs)
            }
        }
    }
}
