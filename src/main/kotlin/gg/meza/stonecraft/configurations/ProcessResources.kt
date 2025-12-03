package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.getResourcePackFormat
import gg.meza.stonecraft.mod
import gg.meza.stonecraft.tasks.McMetaCreation
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

fun configureProcessResources(project: Project, minecraftVersion: String, modSettings: ModSettingsExtension) {
    /**
     * If the mod is a forge mod, we need to generate the pack.mcmeta file
     * If one already exists, it will be used instead of generating a new one
     */
    if (project.mod.isForge) {
        project.tasks.register<McMetaCreation>("generatePackMCMetaJson") {
            resourcePackVersion.set(getResourcePackFormat(minecraftVersion).toBigDecimal())
        }
    }

    if (project.mod.isForge) {
        project.tasks.named("runClient") {
            dependsOn(project.tasks.named("generatePackMCMetaJson"))
        }

        project.tasks.named("runServer") {
            dependsOn(project.tasks.named("generatePackMCMetaJson"))
        }
    }

    if (project.mod.isForge) {
        project.tasks.named("jar") {
            dependsOn(project.tasks.named("generatePackMCMetaJson"))
        }
    }

    project.tasks.named("processResources") {
        dependsOn(project.tasks.named("stonecutterGenerate"))
    }

    project.afterEvaluate {
        // Deal with the general resources
        project.project.tasks.withType<ProcessResources> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            val currentResourceVersion = getResourcePackFormat(minecraftVersion)

            // Version 43 changed how the resource directories are named
            val needsOldResources = currentResourceVersion.compareTo(34) < 0

            doFirst {
                logger.debug(String.format("Current resource version is %s", currentResourceVersion.toString()))
            }

            val basicModDetails = mapOf(
                "id" to project.mod.id,
                "name" to project.mod.name,
                "group" to project.mod.group,
                "description" to project.mod.description,
                "version" to project.mod.version,
                "minecraftVersion" to minecraftVersion,
                "packVersion" to currentResourceVersion.toBigDecimal(),
                "fabricVersion" to project.mod.prop("fabric_version", "not set"),
                "forgeVersion" to project.mod.prop("forge_version", "not set"),
                "neoforgeVersion" to project.mod.prop("neoforge_version", "not set"),
            ) + modSettings.variableReplacements.get()

            val filesToNotExpandIn = listOf("assets/**/lang/*.json")

            filesMatching(listOf("**/*.json", "**/*.toml", "**/*.mcmeta")) {
                val shouldSkip = filesToNotExpandIn.any { skipPattern ->
                    // If anyone has a better way to do this, please submit a PR
                    // Convert Ant-style pattern to regex pattern for matching
                    val regexPattern = skipPattern
                        .replace(".", "\\.")
                        .replace("**", ".*")
                        .replace("*", "[^/]*")
                    path.matches(Regex(regexPattern))
                }

                if (!shouldSkip) {
                    expand(basicModDetails)
                }
            }

            // Exclude the correct mod metadata file based on the loader
            when {
                project.mod.isFabric -> {
                    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")
                }

                project.mod.isNeoforge -> {
                    exclude("fabric.mod.json", "META-INF/mods.toml", "pack.mcmeta")
                }

                project.mod.isForge -> {
                    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
                }
            }

            // If the mod is using the old resource system, we need to change the item identifier
            // Ideally you're using data generators and this is not an issue for you
            filesMatching("data/**/*.json") {
                expand(
                    mapOf(
                        "itemIdentifier" to if (needsOldResources) "item" else "id"
                    )
                )
            }

            // Similarly, if the mod is using the old resource system, we need to rename some directories
            // This is because the old system used a different naming convention
            // Ideally you're using generators and this is not an issue.
            // The Structures directory might be a problem as that's not something you usually generate
            if (needsOldResources) {
                logger.debug("Using old resource system")

                val renameMappings = mapOf(
                    "data/minecraft/tags/block" to "data/minecraft/tags/blocks",
                    "data/${project.mod.id}/advancement/recipe" to "data/${project.mod.id}/advancement/recipes",
                    "data/${project.mod.id}/advancement" to "data/${project.mod.id}/advancements",
                    "data/${project.mod.id}/loot_table" to "data/${project.mod.id}/loot_tables",
                    "data/${project.mod.id}/recipe" to "data/${project.mod.id}/recipes",
                    "data/${project.mod.id}/structure" to "data/${project.mod.id}/structures"
                )

                renameMappings.forEach { (source, destination) ->
                    filesMatching("$source/**") {
                        path = path.replace(source, destination)
                    }
                }
            }

            if (project.mod.isForge) {
                finalizedBy("generatePackMCMetaJson")
            }

            doLast {
                // Process the pack.mcmeta file for Forge
                cleanUpEmptyResourceDirectories(project)
            }
        }
    }
}

private fun cleanUpEmptyResourceDirectories(project: Project) {
    val buildResourcesDir = project.layout.buildDirectory.dir("resources/main").get().asFile

    if (buildResourcesDir.exists()) {
        project.logger.debug("Cleaning up empty directories in ${buildResourcesDir.path}")

        // Recursively delete empty directories
        buildResourcesDir.walkBottomUp().filter { it.isDirectory && it.listFiles().isNullOrEmpty() }
            .forEach { dir ->
                dir.delete()
            }
    }
}
