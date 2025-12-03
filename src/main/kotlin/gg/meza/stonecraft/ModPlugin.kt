package gg.meza.stonecraft

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import gg.meza.stonecraft.configurations.*
import gg.meza.stonecraft.extension.ModSettingsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.getByType

class ModPlugin : Plugin<Any> {

    override fun apply(target: Any) {
        when (target) {
            is Project -> applyToProject(target)
            is Settings -> applyToSettings(target)
            else -> throw IllegalStateException("Plugin must be applied to a build.gradle[.kts] or a stonecutter.gradle[.kts] file")
        }
    }
    private fun applyToSettings(settings: Settings) {
        return
    }
    private fun applyToProject(project: Project) {
        if (project.extensions.findByType(StonecutterControllerExtension::class.java) != null) {
            val stonecutterController = project.extensions.getByType<StonecutterControllerExtension>()
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

        val stonecutter = project.extensions.getByType<StonecutterBuildExtension>()
        val base = project.extensions.getByType(BasePluginExtension::class)
        val modSettings = project.extensions.create("modSettings", ModSettingsExtension::class.java, project, project.mod.loader)

        val canonicalMinecraftVersion = stonecutter.current.version

        // Load version specific dependencies from versions/dependencies/[minecraftVersion].properties
        loadSpecificDependencyVersions(project, canonicalMinecraftVersion)

        val realMinecraftVersion = project.mod.prop("minecraft_version", canonicalMinecraftVersion)

        base.archivesName.set("${project.mod.id}-${project.mod.loader}")
        project.version = "${project.mod.version}+mc$realMinecraftVersion"

        configureDependencies(project, canonicalMinecraftVersion, realMinecraftVersion)
        configureStonecutterConstants(project, stonecutter)
        configureProcessResources(project, canonicalMinecraftVersion, modSettings)
        configureLoom(project, stonecutter, modSettings)
        patchAroundArchitecturyQuirks(project, stonecutter)
        configurePublishing(project, realMinecraftVersion)
        configureTasks(project, stonecutter, modSettings)
        configureJava(project, stonecutter, modSettings)

        project.afterEvaluate {
        }
    }
}
