package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.MinecraftObfuscation
import gg.meza.stonecraft.Side
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.getProgramArgs
import gg.meza.stonecraft.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.RunConfiguration
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType

/**
 * Retains the pre-2.0 configuration API for convention plugins compiled against Stonecraft.
 */
@Deprecated("Use the overload that accepts the configured gametest source set")
fun configureLoom(
    project: Project,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
    minecraftObfuscation: MinecraftObfuscation,
) {
    val gameTestSourceSet = configureGameTestSourceSet(
        project,
        minecraftObfuscation,
        modSettings.gametestModuleNameProp,
    )
    configureLoom(project, stonecutter, modSettings, minecraftObfuscation, gameTestSourceSet)
}

fun configureLoom(
    project: Project,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
    minecraftObfuscation: MinecraftObfuscation,
    gameTestSourceSet: SourceSet,
) {
    val loom = project.extensions.getByType(LoomGradleExtensionAPI::class)

    loom.apply {
        accessWidenerPath.set(modSettings.effectiveAccessWidenerLocationProp)

        if (project.mod.isForge) {
            forge.convertAccessWideners.set(modSettings.effectiveAccessWidenerProcessingProp)
        }

        decompilers {
            getByName("vineflower").apply { options.put("mark-corresponding-synthetics", "1") }
        }

        runConfigs.configureEach {
            generateRunConfig.set(true)
            preferGradleTask.set(true)
            runDirectory.set(modSettings.runDirectoryProp)
            if (name == "client") {
                programArguments.addAll("--username=developer")
            }
        }
    }

    configureDatagen(project, loom, stonecutter, modSettings)
    configureClientGameTests(project, loom, stonecutter, modSettings, gameTestSourceSet)
    configureServerGameTests(project, loom, stonecutter, modSettings, gameTestSourceSet)

    project.afterEvaluate {
        val generatedDirectories = generatedResourceDirectories(project, stonecutter, modSettings)
        val mainMod = if (project.mod.isForgeLike) {
            loom.mods.named("main").get()
        } else {
            val mainSourceSet = project.extensions.getByType(JavaPluginExtension::class)
                .sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            loom.mods.maybeCreate(project.mod.id).apply { sourceSet(mainSourceSet) }
        }
        mainMod.modFiles.from(generatedDirectories)

        val awFile = modSettings.effectiveAccessWidenerLocationProp.orNull
        if (awFile != null) {
            val relativeLocation =
                awFile.asFile.relativeTo(project.rootProject.layout.projectDirectory.dir("src/main/resources").asFile).invariantSeparatorsPath

            val task = resolveJarTask(project, minecraftObfuscation)

            if (project.mod.isFabricLike) {
                loom.injectAccessWidener(task)
            }

            if (project.mod.isNeoforge) {
                loom.neoForge.convertAccessWideners(task, relativeLocation)
            }
        }
    }
}

/**
 * Configures the client game test tasks for the project
 */
@Deprecated("Use the overload that accepts the configured gametest source set")
fun configureClientGameTests(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
) {
    val mainSourceSet = project.extensions.getByType<SourceSetContainer>().getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    configureClientGameTests(project, loom, stonecutter, modSettings, mainSourceSet)
}

fun configureClientGameTests(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
    gameTestSourceSet: SourceSet,
) {
    val mod = project.mod

    loom.runs {
        create("gameTestClient") {
            client()
            useGameTestSourceSet(
                project,
                gameTestSourceSet,
                modSettings.gametestModuleNameProp,
                stonecutter.eval(stonecutter.current.version, ">=1.21.5"),
            )
            runDirectory.set(modSettings.testClientRunDirectoryProp)
            if (mod.isFabric) {
                fabricGameTestConfig(Side.CLIENT, modSettings.fabricClientJunitReportLocationProp)
            }
            if (mod.isForge) {
                forgeConfig(Side.CLIENT, mod.loader, mod.id, modSettings.gametestModuleNameProp)
            }
            if (mod.isNeoforge) {
                neoforgeConfig(
                    Side.CLIENT,
                    mod.loader,
                    mod.id,
                    modSettings.gametestModuleNameProp,
                    stonecutter,
                )
            }
        }

    }
//
}

/**
 * Configures the client game test tasks for the project
 */
@Deprecated("Use the overload that accepts the configured gametest source set")
fun configureServerGameTests(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
) {
    val mainSourceSet = project.extensions.getByType<SourceSetContainer>().getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    configureServerGameTests(project, loom, stonecutter, modSettings, mainSourceSet)
}

fun configureServerGameTests(
    project: Project,
    loom: LoomGradleExtensionAPI,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
    gameTestSourceSet: SourceSet,
) {
    val mod = project.mod

    loom.runs {
        create("gameTestServer") {
            useGameTestSourceSet(
                project,
                gameTestSourceSet,
                modSettings.gametestModuleNameProp,
                stonecutter.eval(stonecutter.current.version, ">=1.21.5"),
            )
            runDirectory.set(modSettings.testServerRunDirectoryProp)
            if (mod.isFabric) {
                server()
                fabricGameTestConfig(Side.SERVER, modSettings.fabricServerJunitReportLocationProp)
            }
            if (mod.isForge) {
                forgeConfig(Side.SERVER, mod.loader, mod.id, modSettings.gametestModuleNameProp)
            }
            if (mod.isNeoforge) {
                neoforgeConfig(
                    Side.SERVER,
                    mod.loader,
                    mod.id,
                    modSettings.gametestModuleNameProp,
                    stonecutter,
                )
            }
        }
    }

}

/**
 * Configures the fabric game tests
 * @param side The side of the game test
 *
 */
private fun RunConfiguration.fabricGameTestConfig(side: Side, junitFile: RegularFileProperty) {
    jvmArguments.add("-Dfabric-api.gametest")
    jvmArguments.add(
        junitFile.map { reportFile ->
            "-Dfabric-api.gametest.report-file=${reportFile.asFile.absolutePath}"
        }
    )
}

/**
 * Configures the forge game tests
 *
 * On the server side it also sets the `forge.gameTestServer` property to `true`
 * which is mostly undocumented and took a lot of debugging to figure out
 *
 * @param side The side of the game test
 */
private fun RunConfiguration.forgeConfig(
    side: Side,
    loader: String,
    modId: String,
    gameTestModuleName: Provider<String>,
) {
    if (side == Side.SERVER) {
        runtimeEnvironment.set("gameTestServer")
        forgeTemplate.set("gameTestServer")
        systemProperties.put("$loader.gameTestServer", "true")
    }

    val enabledNamespaces = gameTestModuleName.map { moduleName -> "$modId,$moduleName" }
    systemProperties.put("$loader.enableGameTest", "true")
    systemProperties.put("$loader.enabledGameTestNamespaces", enabledNamespaces)
}

private fun RunConfiguration.neoforgeConfig(
    side: Side,
    loader: String,
    modId: String,
    gameTestModuleName: Provider<String>,
    stonecutter: StonecutterBuildExtension
) {
    if (side == Side.SERVER) {
        runtimeEnvironment.set("gameTestServer")
        forgeTemplate.set("gameTestServer")

        if (stonecutter.current.parsed >= "1.21.5") {
            mainClass.set("net.neoforged.fml.startup.GameTestServer")
        }

        systemProperties.put("$loader.gameTestServer", "true")
    }

    val enabledNamespaces = gameTestModuleName.map { moduleName -> "$modId,$moduleName" }
    systemProperties.put("$loader.enableGameTest", "true")
    systemProperties.put("$loader.enabledGameTestNamespaces", enabledNamespaces)
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
    val generatedResources = modSettings.generatedResourcesProp
    val clientGeneratedResources = generatedResources.map { directory -> directory.dir("client") }
    val serverGeneratedResources = generatedResources.map { directory -> directory.dir("server") }

    val modDefinition = listOf("--mod", mod.id)
    val generateAll = listOf("--all")
    val existingResources = listOf("--existing", project.rootProject.file("src/main/resources").absolutePath)

    if (project.mod.isFabric) {
        val fabricApi = project.extensions.getByType(FabricApiExtension::class)
        fabricApi.apply {
            configureDataGeneration {
                if (stonecutter.eval(stonecutter.current.version, ">=1.21.4")) {
                    client.set(true)
                }
                addToResources.set(false)
                outputDirectory.set(project.layout.file(generatedResources.map { directory -> directory.asFile }))
            }
        }
    }

    val forgeLikeLogging: RunConfiguration.() -> Unit = {
        mapOf(
            "${mod.loader}.logging.console.level" to "debug",
            "${mod.loader}.logging.markers" to "REGISTRIES"
        ).forEach { (key, value) -> systemProperties.put(key, value) }
    }

    loom.runs {
        if (mod.isForge) {
            create("datagen") {
                data()
                if (stonecutter.eval(minecraftVersion, ">=26.1")) {
                    programArguments.addAll("--launchTarget", "forge_userdev_data", "--gameDir", ".")
                }
                programArguments.addAll(
                    generatedResources.map { directory ->
                        getProgramArgs(
                            generateAll,
                            modDefinition,
                            listOf("--output", directory.asFile.absolutePath),
                            existingResources
                        )
                    }
                )
                forgeLikeLogging()
            }
        }

        if (mod.isNeoforge) {
            if (stonecutter.eval(minecraftVersion, ">=1.21.4")) {
                // @see https://neoforged.net/news/21.4release/#data-generation-splitting
                create("ServerDatagen") {
                    serverData()
                    programArguments.addAll(
                        serverGeneratedResources.map { directory ->
                            getProgramArgs(
                                modDefinition,
                                listOf("--output", directory.asFile.absolutePath)
                            )
                        }
                    )
                    forgeLikeLogging()
                }
                create("ClientDatagen") {
                    clientData()
                    programArguments.addAll(
                        clientGeneratedResources.map { directory ->
                            getProgramArgs(
                                modDefinition,
                                listOf("--output", directory.asFile.absolutePath)
                            )
                        }
                    )
                    forgeLikeLogging()
                }
            } else {
//                if (stonecutter.eval(minecraftVersion, ">1.21")) {
                create("Datagen") {
                    data()
                    programArguments.addAll(
                        generatedResources.map { directory ->
                            getProgramArgs(
                                generateAll,
                                modDefinition,
                                listOf("--output", directory.asFile.absolutePath),
                                existingResources
                            )
                        }
                    )
                    forgeLikeLogging()
                }
//                }
            }
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
