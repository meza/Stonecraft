package gg.meza.stonecraft

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import gg.meza.stonecraft.configurations.*
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.skills.STONECRAFT_GUIDANCE_REMINDER_PROPERTY
import gg.meza.stonecraft.skills.STONECRAFT_SKILL_DESTINATION
import gg.meza.stonecraft.skills.StonecraftSkill
import gg.meza.stonecraft.skills.StonecraftSkillState
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.getByType

class ModPlugin : Plugin<Any> {

    companion object {
        private const val BANNER_PRINTED_PROPERTY = "gg.meza.stonecraft.bannerPrinted"
        private const val CLAUDE_CODE_ENVIRONMENT_VARIABLE = "CLAUDECODE"
        private const val CODEX_THREAD_ENVIRONMENT_VARIABLE = "CODEX_THREAD_ID"
        private const val CURSOR_AGENT_ENVIRONMENT_VARIABLE = "CURSOR_AGENT"
        private const val DEVELOPMENT_VERSION = "development"
    }

    override fun apply(target: Any) {
        when (target) {
            is Project -> applyToProject(target)
            is Settings -> applyToSettings(target)
            else -> throw IllegalStateException("Plugin must be applied to a build.gradle[.kts] or a stonecutter.gradle[.kts] file")
        }
    }

    private fun applyToSettings(settings: Settings) {
        settings.gradle.rootProject { printBanner(this) }
    }

    private fun applyToProject(project: Project) {
        printBanner(project)

        if (project.extensions.findByType(StonecutterControllerExtension::class.java) != null) {
            val stonecutterController = project.extensions.getByType<StonecutterControllerExtension>()
            configureAgentSkillTasks(project)
            configureStonecutterHandlers(stonecutterController)
            configureChiseledTasks(project, stonecutterController)
            return
        }
        val stonecutter = project.extensions.getByType<StonecutterBuildExtension>()
        val canonicalMinecraftVersion = stonecutter.current.version
        val realMinecraftVersion = loadSpecificMinecraftVersion(project, canonicalMinecraftVersion)

        if (
            project.pluginManager.hasPlugin("dev.architectury.loom") ||
            project.pluginManager.hasPlugin("dev.architectury.loom-no-remap")
        ) {
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
        val minecraftObfuscation = if (stonecutter.eval(realMinecraftVersion, ">1.21.11")) {
            MinecraftObfuscation.UNOBFUSCATED
        } else {
            MinecraftObfuscation.MAPPED
        }
        configurePlugins(project, minecraftObfuscation)

        val base = project.extensions.getByType(BasePluginExtension::class)
        val modSettings =
            project.extensions.create("modSettings", ModSettingsExtension::class.java, project, project.mod.loader)

        // Load version specific dependencies from versions/dependencies/[minecraftVersion].properties
        loadSpecificDependencyVersions(project, canonicalMinecraftVersion)

        base.archivesName.set("${project.mod.id}-${project.mod.loader}")
        project.version = "${project.mod.version}+mc$realMinecraftVersion"

        configureDependencies(project, stonecutter, realMinecraftVersion, minecraftObfuscation)
        configureJunit(project, stonecutter, modSettings)
        configureStonecutterConstants(project, stonecutter)
        configureProcessResources(project, realMinecraftVersion, modSettings, stonecutter)
        configureLoom(project, stonecutter, modSettings, minecraftObfuscation)
        patchAroundArchitecturyQuirks(project, stonecutter)
        configurePublishing(project, realMinecraftVersion, minecraftObfuscation)
        configureTasks(project, stonecutter, modSettings, minecraftObfuscation)
        configureIntellij(project, stonecutter)
        configureJava(project, stonecutter, modSettings)
    }

    private fun printBanner(project: Project) {
        val rootProperties = project.rootProject.extensions.extraProperties
        if (rootProperties.has(BANNER_PRINTED_PROPERTY)) {
            return
        }

        rootProperties.set(BANNER_PRINTED_PROPERTY, true)
        val version = ModPlugin::class.java.`package`.implementationVersion ?: DEVELOPMENT_VERSION
        project.logger.quiet("Using Stonecraft $version")

        val showGuidanceReminder = codingAgentDetected(project) &&
            project.providers.gradleProperty(STONECRAFT_GUIDANCE_REMINDER_PROPERTY)
                .map { !it.equals("false", ignoreCase = true) }
                .orElse(true)
                .get()
        if (!showGuidanceReminder) {
            return
        }

        val installedSkillFile = project.rootProject.file(STONECRAFT_SKILL_DESTINATION)
        val installedSkillContent = installedSkillFile
            .takeIf { it.isFile }
            ?.readText(Charsets.UTF_8)

        when (StonecraftSkill.classify(installedSkillContent, StonecraftSkill.embedded.version).state) {
            StonecraftSkillState.CURRENT -> return
            StonecraftSkillState.MISSING -> project.logger.quiet(
                "\n" +
                    "Instructions for using Stonecraft and Stonecutter: run ./gradlew stonecraftGuidance \n" +
                    "Install as a skill for the project ./gradlew installStonecraftSkill when prompted."
            )

            StonecraftSkillState.OUTDATED -> project.logger.quiet(
                "\n" +
                    "Updated Stonecraft and Stonecutter guidance is available: " +
                    "run ./gradlew stonecraftGuidanceVersion \n" +
                    "Read and merge the update with ./gradlew stonecraftGuidance."
            )
        }
    }

    private fun codingAgentDetected(project: Project): Boolean {
        val environment = project.providers
        val claudeCodeDetected = environment.environmentVariable(CLAUDE_CODE_ENVIRONMENT_VARIABLE)
            .map { it == "1" }
            .orElse(false)
            .get()
        val codexDetected = environment.environmentVariable(CODEX_THREAD_ENVIRONMENT_VARIABLE)
            .map(String::isNotBlank)
            .orElse(false)
            .get()
        val cursorDetected = environment.environmentVariable(CURSOR_AGENT_ENVIRONMENT_VARIABLE)
            .map(String::isNotBlank)
            .orElse(false)
            .get()

        return claudeCodeDetected || codexDetected || cursorDetected
    }
}
