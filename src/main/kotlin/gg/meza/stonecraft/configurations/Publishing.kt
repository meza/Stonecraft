package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.mod
import gg.meza.stonecraft.upperCaseFirst
import me.modmuss50.mpp.ModPublishExtension
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

fun configurePublishing(project: Project, minecraftVersion: String) {
    val publishing = project.extensions.getByType(ModPublishExtension::class)
    val modrinthVariables = listOf("MODRINTH_TOKEN", "MODRINTH_ID")
    val curseforgeVariables = listOf("CURSEFORGE_SLUG", "CURSEFORGE_ID", "CURSEFORGE_TOKEN")

    val mod = project.mod

    publishing.apply {
        val isBeta = "next" in project.version.toString()
        changelog.set(
            project.layout.projectDirectory.file("changelog.md").asFile.takeIf { it.exists() }?.readText() ?: "No changelog provided."
        )

        val remapJar = project.tasks.named<RemapJarTask>("remapJar")
        file.set(remapJar.get().archiveFile)
        version.set("${mod.version}+${mod.loader}-${minecraftVersion}")
        type.set(if (isBeta) BETA else STABLE)
        modLoaders.add(mod.loader)
        displayName.set("${mod.version} for ${mod.loader.upperCaseFirst()} $minecraftVersion")
        dryRun.set(project.providers.environmentVariable("DO_PUBLISH").getOrElse("true").toBoolean())

        publishing.platforms.forEach { platform ->
            println("LOOKIE HERE  -> Platform: $platform")
        }

        if (modrinthVariables.all { project.providers.environmentVariable(it).isPresent }) {
            modrinth {
                accessToken.set(project.providers.environmentVariable("MODRINTH_TOKEN"))
                projectId.set(project.providers.environmentVariable("MODRINTH_ID"))
                minecraftVersions.add(minecraftVersion)
                announcementTitle.set("Download ${mod.version}+${mod.loader}-${minecraftVersion} from Modrinth")
            }
        } else {
            if (!dryRun.get()) {
                project.rootProject.logger.lifecycle("Essential Modrinth variables not found, skipping Modrinth publishing")
                project.rootProject.logger.lifecycle("If you want to use Modrinth, please set the MODRINTH_TOKEN and MODRINTH_ID environment variables")
            }
        }

        if (curseforgeVariables.all { project.providers.environmentVariable(it).isPresent }) {
            curseforge {
                projectSlug.set(project.providers.environmentVariable("CURSEFORGE_SLUG"))
                projectId.set(project.providers.environmentVariable("CURSEFORGE_ID"))
                accessToken.set(project.providers.environmentVariable("CURSEFORGE_TOKEN").orElse(""))
                minecraftVersions.add(minecraftVersion)
                announcementTitle.set("Download ${mod.version}+${mod.loader}-${minecraftVersion} from CurseForge")
            }
        } else {
            if (!dryRun.get()) {
                project.rootProject.logger.lifecycle("Essential CurseForge variables not found, skipping CurseForge publishing")
                project.rootProject.logger.lifecycle("If you want to use CurseForge, please set the CURSEFORGE_SLUG, CURSEFORGE_ID, and CURSEFORGE_TOKEN environment variables")
            }
        }
    }
}
