package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.Side
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.getProgramArgs
import gg.meza.stonecraft.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import net.fabricmc.loom.configuration.ide.RunConfigSettings
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType

fun configureLoom(project: Project, stonecutter: StonecutterBuildExtension, modSettings: ModSettingsExtension) {
    val loom = project.extensions.getByType(LoomGradleExtensionAPI::class)

    if (project.mod.isForge && stonecutter.eval(stonecutter.current.version, ">=1.21")) {
        project.logger.warn(
            "Forge 1.21 and above is not really supported by Architectury anymore and " +
                "issues may arise when using it with Architectury Loom.\n" +
                "Please consider using NeoForge instead if you can."
        )
    }

    loom.apply {
        accessWidenerPath.set(modSettings.effectiveAccessWidenerLocationProp)

        if (project.mod.isForge) {
            forge.convertAccessWideners.set(modSettings.effectiveAccessWidenerProcessingProp)
        }

        decompilers {
            getByName("vineflower").apply { options.put("mark-corresponding-synthetics", "1") }
        }
    }
    project.afterEvaluate {
        loom.apply {
            val awFile = modSettings.effectiveAccessWidenerLocationProp.orNull
            if (awFile != null) {
                val relativeLocation =
                    awFile.asFile.relativeTo(project.rootProject.layout.projectDirectory.dir("src/main/resources").asFile).invariantSeparatorsPath

                val task = when {
                    stonecutter.current.parsed >= "26.1" -> project.tasks.named("jar", Jar::class.java)
                    else -> project.tasks.named("remapJar", RemapJarTask::class.java)
                }

                if (project.mod.isFabricLike) {
                    injectAccessWidener(task)
                }

                if (project.mod.isNeoforge) {
                    neoForge.convertAccessWideners(task, relativeLocation)
                }
            }

            runConfigs.all {
                generateRunConfig.set(true)
                runDirectory.set(modSettings.runDirectoryProp)
                if (name == "client") {
                    programArguments.addAll("--username=developer")
                }
            }
        }
        configureDatagen(project, loom, stonecutter, modSettings)
        configureClientGameTests(project, loom, stonecutter, modSettings)
        configureServerGameTests(project, loom, stonecutter, modSettings)
    }
}

/**
 * Configures the client game test tasks for the project
 */
fun configureClientGameTests(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension
) {
    val mod = project.mod

    loom.runs {
        create("gameTestClient") {
            client()
            runDirectory.set(modSettings.testClientRunDirectoryProp)
            if (mod.isFabric) {
                fabricGameTestConfig(Side.CLIENT, modSettings.fabricClientJunitReportLocationProp)
            }
            if (mod.isForge) {
                forgeConfig(Side.CLIENT, mod.loader, stonecutter)
            }
            if (mod.isNeoforge) {
                neoforgeConfig(Side.CLIENT, mod.loader, stonecutter)
            }
        }

        if (mod.isForge) {
            project.tasks.named("runGameTestClient") {
                dependsOn("generatePackMCMetaJson")
            }
        }
    }
//
}

/**
 * Configures the client game test tasks for the project
 */
fun configureServerGameTests(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension
) {
    val mod = project.mod

    loom.runs {
        create("gameTestServer") {
            runDirectory.set(modSettings.testServerRunDirectoryProp)
            if (mod.isFabric) {
                server()
                fabricGameTestConfig(Side.SERVER, modSettings.fabricServerJunitReportLocationProp)
            }
            if (mod.isForge) {
                forgeConfig(Side.SERVER, mod.loader, stonecutter)
            }
            if (mod.isNeoforge) {
                neoforgeConfig(Side.SERVER, mod.loader, stonecutter)
            }
        }
    }

    if (mod.isForge) {
        project.tasks.named("runGameTestServer") {
            dependsOn("generatePackMCMetaJson")
        }
    }
}

/**
 * Configures the fabric game tests
 * @param side The side of the game test
 *
 */
private fun RunConfigSettings.fabricGameTestConfig(side: Side, junitFile: RegularFileProperty) {
    mapOf(
        "fabric-api.gametest" to "",
        "fabric-api.gametest.report-file" to junitFile.get().asFile.absolutePath
    ).forEach { (key, value) ->
        run {
            if (value.isNotEmpty()) {
                jvmArguments.add("-D$key=$value")
            } else {
                jvmArguments.add("-D$key")
            }
        }
    }
}

/**
 * Configures the forge game tests
 *
 * On the server side it also sets the `forge.gameTestServer` property to `true`
 * which is mostly undocumented and took a lot of debugging to figure out
 *
 * @param side The side of the game test
 */
private fun RunConfigSettings.forgeConfig(side: Side, loader: String, stonecutter: StonecutterBuildExtension) {
    if (side == Side.SERVER) {
        runtimeEnvironment.set("gameTestServer")
        forgeTemplate.set("gameTestServer")
        systemProperties.put("$loader.gameTestServer", "true")
    }

    mapOf(
        "$loader.enabledGameTestNamespaces" to project.mod.id,
        "$loader.enableGameTest" to "true"
    ).forEach { (key, value) -> systemProperties.put(key, value) }
}

private fun RunConfigSettings.neoforgeConfig(side: Side, loader: String, stonecutter: StonecutterBuildExtension) {
    if (side == Side.SERVER) {
        runtimeEnvironment.set("gameTestServer")
        forgeTemplate.set("gameTestServer")

        if (stonecutter.current.parsed >= "1.21.5") {
            mainClass.set("net.neoforged.fml.startup.GameTestServer")
        }

        systemProperties.put("$loader.gameTestServer", "true")
    }

    mapOf(
        "$loader.enabledGameTestNamespaces" to project.mod.id,
        "$loader.enableGameTest" to "true"
    ).forEach { (key, value) -> systemProperties.put(key, value) }
}

/**
 * Configures the datagen tasks for the project in regard to the loaders and the quirks of Architectury
 */
fun configureDatagen(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension
) {
    val minecraftVersion = stonecutter.current.version

    val mod = project.mod
    val generatedResources = modSettings.generatedResourcesProp.get()

    val modDefinition = listOf("--mod", mod.id)
    val generateAll = listOf("--all")
    val outputFolder = listOf("--output", generatedResources.asFile.absolutePath)
    val existingResources = listOf("--existing", project.rootProject.file("src/main/resources").absolutePath)

    if (project.mod.isFabric) {
        val fabricApi = project.extensions.getByType(FabricApiExtension::class)
        fabricApi.apply {
            configureDataGeneration {
                if (stonecutter.eval(stonecutter.current.version, ">=1.21.4")) {
                    client.set(true)
                }
                outputDirectory.set(generatedResources.asFile)
            }
        }
    }

    val forgeLikeLogging: RunConfigSettings.() -> Unit = {
        mapOf(
            "${mod.loader}.logging.console.level" to "debug",
            "${mod.loader}.logging.markers" to "REGISTRIES"
        ).forEach { (key, value) -> systemProperties.put(key, value) }
    }

    loom.runs {
        if (mod.isForge) {
            create("datagen") {
                data()
                programArguments.addAll(getProgramArgs(generateAll, modDefinition, outputFolder, existingResources))
                forgeLikeLogging()
            }
        }

        if (mod.isNeoforge) {
            if (stonecutter.eval(minecraftVersion, ">=1.21.4")) {
                // @see https://neoforged.net/news/21.4release/#data-generation-splitting
                create("ServerDatagen") {
                    serverData()
                    programArguments.addAll(getProgramArgs(modDefinition, outputFolder))
                    forgeLikeLogging()
                }
                create("ClientDatagen") {
                    clientData()
                    programArguments.addAll(getProgramArgs(modDefinition, outputFolder))
                    forgeLikeLogging()
                }
            } else {
//                if (stonecutter.eval(minecraftVersion, ">1.21")) {
                create("Datagen") {
                    data()
                    programArguments.addAll(getProgramArgs(generateAll, modDefinition, outputFolder, existingResources))
                    forgeLikeLogging()
                }
//                }
            }
        }
    }

    if (mod.isForge) {
        project.tasks.named("runDatagen") {
            dependsOn("generatePackMCMetaJson")
        }
    }

//    if (mod.isNeoforge && stonecutter.eval(stonecutter.current.version, "<=1.21")) {
//        project.tasks.register("runDatagen") {
//            logger.error("Datagen is disabled for Neoforge 1.21 and below due to existing issues")
//        }
//    }

    if (mod.isNeoforge && stonecutter.eval(stonecutter.current.version, ">=1.21.4")) {
        project.tasks.register("runDatagen") {
            dependsOn(project.tasks.named("runServerDatagen"), project.tasks.named("runClientDatagen"))
        }
    }
}
