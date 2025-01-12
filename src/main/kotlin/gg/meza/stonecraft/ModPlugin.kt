package gg.meza.stonecraft

import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.StonecutterController
import gg.meza.stonecraft.configurations.configureChiseledTasks
import gg.meza.stonecraft.configurations.configureDependencies
import gg.meza.stonecraft.configurations.configureJava
import gg.meza.stonecraft.configurations.configureLoom
import gg.meza.stonecraft.configurations.configurePlugins
import gg.meza.stonecraft.configurations.configureProcessResources
import gg.meza.stonecraft.configurations.configurePublishing
import gg.meza.stonecraft.configurations.configureStonecutterConstants
import gg.meza.stonecraft.configurations.configureTasks
import gg.meza.stonecraft.configurations.patchAroundArchitecturyQuirks
import gg.meza.stonecraft.extension.ModSettingsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType

class ModPlugin : Plugin<ExtensionAware> {

    override fun apply(project: ExtensionAware) {
        if (project !is Project) {
            throw IllegalStateException("Plugin must be applied to a build.gradle[.kts] or a stonecutter.gradle[.kts] file")
        }

        if (project.extensions.findByType(StonecutterController::class.java) != null) {
            val stonecutterController = project.extensions.getByType<StonecutterController>()
            configureChiseledTasks(project, stonecutterController)
            return
        }

        if (project.pluginManager.hasPlugin("dev.architectury.loom")) {
            project.logger.error(
                "This plugin needs to be applied before the Architectury Loom plugin.\n" +
                    "Please move gg.meza.stonecraft plugin to the top of your build.gradle.kts file"
            )
            throw IllegalStateException(
                "This plugin needs to be applied before the Architectury Loom plugin.\n" +
                    "Please move gg.meza.stonecraft plugin to the top of your build.gradle.kts file"
            )
        }

        project.group = project.mod.group
        configurePlugins(project)

        val stonecutter = project.extensions.getByType<StonecutterBuild>()
        val base = project.extensions.getByType(BasePluginExtension::class)
        val modSettings = project.extensions.create("modSettings", ModSettingsExtension::class.java, project, project.mod.loader)

        val minecraftVersion = stonecutter.current.version

        base.archivesName.set("${project.mod.id}-${project.mod.loader}")
        project.version = "${project.mod.version}+mc$minecraftVersion"

        configureDependencies(project, minecraftVersion)
        configureStonecutterConstants(project, stonecutter)
        configureProcessResources(project, minecraftVersion, modSettings)
        configureLoom(project, stonecutter, modSettings)
        patchAroundArchitecturyQuirks(project, stonecutter)
        configurePublishing(project, minecraftVersion)
        configureTasks(project, stonecutter, modSettings)
        configureJava(project, stonecutter, modSettings)

        project.afterEvaluate {
        }
    }
}
