package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuild
import gg.meza.stonecraft.Side
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.getProgramArgs
import gg.meza.stonecraft.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import net.fabricmc.loom.configuration.ide.RunConfigSettings
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.getByType

fun configureLoom(project: Project, stonecutter: StonecutterBuild, modSettings: ModSettingsExtension) {
    val loom = project.extensions.getByType(LoomGradleExtensionAPI::class)

    if (project.mod.isForge && stonecutter.eval(stonecutter.current.version, ">=1.21")) {
        project.logger.warn(
            "Forge 1.21 and above is not really supported by Architectury anymore and " +
                "issues may arise when using it with Architectury Loom.\n" +
                "Please consider using NeoForge instead if you can."
        )
    }

    loom.apply {
        val awFile =
            project.rootProject.layout.projectDirectory.file("src/main/resources/${project.mod.id}.accesswidener")

        if (awFile.asFile.exists()) {
            accessWidenerPath.set(awFile)

            if (project.mod.isForge) {
                forge.convertAccessWideners.set(true)
            }
        }

        decompilers {
            getByName("vineflower").apply { options.put("mark-corresponding-synthetics", "1") }
        }
    }
    project.afterEvaluate {
        val runString = project.layout.projectDirectory.asFile.toPath()
            .relativize(modSettings.runDirectoryProp.get().asFile.toPath()).toString()

        loom.apply {
            runConfigs.all {
                isIdeConfigGenerated = true
                runDir = runString
                if (environment == "client") programArgs("--username=developer")
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
    stonecutter: StonecutterBuild,
    modSettings: ModSettingsExtension
) {
    val mod = project.mod

    val testClientDir =
        project.layout.projectDirectory.asFile.toPath().relativize(
            modSettings.testClientRunDirectoryProp.get().asFile.toPath()
        ).toString()

    loom.runs {
        create("gameTestClient") {
            client()
            runDir = testClientDir
            if (mod.isFabric) {
                fabricGameTestConfig(Side.CLIENT, modSettings.fabricClientJunitReportLocationProp)
            }
            if (mod.isForgeLike) {
                forgeLikeConfig(Side.CLIENT, mod.loader, stonecutter)
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
    stonecutter: StonecutterBuild,
    modSettings: ModSettingsExtension
) {
    val mod = project.mod

    val testServerDir =
        project.layout.projectDirectory.asFile.toPath().relativize(
            modSettings.testServerRunDirectoryProp.get().asFile.toPath()
        ).toString()

    loom.runs {
        create("gameTestServer") {
            server()
            runDir = testServerDir
            if (mod.isFabric) {
                fabricGameTestConfig(Side.SERVER, modSettings.fabricServerJunitReportLocationProp)
            }
            if (mod.isForgeLike) {
                forgeLikeConfig(Side.SERVER, mod.loader, stonecutter)
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
                vmArg("-D$key=$value")
            } else {
                vmArg("-D$key")
            }
        }
    }
}

/**
 * Configures the forge like game tests
 *
 * On the server side it also sets the `forge.gameTestServer` property to `true`
 * which is mostly undocumented and took a lot of debugging to figure out
 *
 * @param side The side of the game test
 */
private fun RunConfigSettings.forgeLikeConfig(side: Side, loader: String, stonecutter: StonecutterBuild) {
    if (side == Side.SERVER) {
        environment("gametestserver")
        forgeTemplate("gametestserver")
        property("$loader.gameTestServer", "true")
    }

    mapOf(
        "$loader.enabledGameTestNamespaces" to project.mod.id,
        "$loader.enableGameTest" to "true"
    ).forEach { (key, value) -> property(key, value) }
}

/**
 * Configures the datagen tasks for the project in regard to the loaders and the quirks of Architectury
 */
fun configureDatagen(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuild,
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
        ).forEach { (key, value) -> property(key, value) }
    }

    loom.runs {
        if (mod.isForge) {
            create("datagen") {
                data()
                programArgs(getProgramArgs(generateAll, modDefinition, outputFolder, existingResources))
                forgeLikeLogging()
            }
        }

        // Neoforge datagen is simply broken in architectury loom
        // @see https://github.com/architectury/architectury-loom/pull/258
        if (mod.isNeoforge) {
            if (stonecutter.eval(minecraftVersion, ">=1.21.4")) {
                // @see https://neoforged.net/news/21.4release/#data-generation-splitting
                create("ServerDatagen") {
                    serverData()
                    programArgs(getProgramArgs(modDefinition, outputFolder))
                    forgeLikeLogging()
                    environment("dataserver")
                    forgeTemplate("dataserver")
                }
                create("ClientDatagen") {
                    clientData()
                    programArgs(getProgramArgs(modDefinition, outputFolder))
                    forgeLikeLogging()
                    environment("dataclient")
                    forgeTemplate("dataclient")
                }
            } else {
                if (stonecutter.eval(minecraftVersion, ">1.21")) {
                    create("Datagen") {
                        data()
                        programArgs(getProgramArgs(generateAll, modDefinition, outputFolder, existingResources))
                        forgeLikeLogging()
                    }
                }
            }
        }
    }

    if (mod.isForge) {
        project.tasks.named("runDatagen") {
            dependsOn("generatePackMCMetaJson")
        }
    }

    if (mod.isNeoforge && stonecutter.eval(stonecutter.current.version, "<=1.21")) {
        project.tasks.register("runDatagen") {
            logger.error("Datagen is disabled for Neoforge 1.21.4 and below due to existing issues")
        }
    }

    // NeoForge changed their datagen approach in 1.21.4
    // This is disabled for now because Architectury Loom does not support it yet
    if (mod.isNeoforge && stonecutter.eval(stonecutter.current.version, ">=1.21.4")) {
        project.tasks.register("runDatagen") {
            dependsOn(project.tasks.named("runServerDatagen"), project.tasks.named("runClientDatagen"))
        }
    }
}
