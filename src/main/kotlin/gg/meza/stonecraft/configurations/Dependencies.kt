package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.util.*

/**
 * Configure the dependencies for a project depending on the mod type and Minecraft version
 * This is all based on yarn mappings for now
 *
 * @TODO: Add support for Quilt
 * @TODO: Add support for Mojang mappings
 *
 * @param project The project to configure the dependencies for
 * @param minecraftVersion The version of Minecraft to configure the dependencies for
 */
fun configureDependencies(project: Project, minecraftVersion: String) {
    // Set the basic repositories for a multiloader project
    project.repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }

    // Load version specific dependencies from versions/dependencies/[minecraftVersion].properties
    loadSpecificDependencyVersions(project, minecraftVersion)

    project.dependencies.add("minecraft", "com.mojang:minecraft:$minecraftVersion")

    if (!project.mod.isNeoforge) {
        // normal projects
        project.dependencies.add("mappings", "net.fabricmc:yarn:${project.mod.prop("yarn_mappings")}:v2")
    }

    if (project.mod.isNeoforge) {
        // Neoforge is designed with mojmap in mind. When yarn is used, it needs to be patched.
        val loom = project.extensions.getByType(LoomGradleExtensionAPI::class)
        val neoforgeMappings = loom.layered {
            mappings("net.fabricmc:yarn:${project.mod.prop("yarn_mappings")}:v2")
            mappings("dev.architectury:yarn-mappings-patch-neoforge:${project.mod.prop("yarn_mappings_neoforge_patch")}")
        }
        project.dependencies.add("mappings", neoforgeMappings)
        project.dependencies.add("neoForge", "net.neoforged:neoforge:${project.mod.prop("neoforge_version")}")
    }

    if (project.mod.isForge) {
        project.dependencies.add("forge", "net.minecraftforge:forge:${project.mod.prop("forge_version")}")
    }

    if (project.mod.isFabric) {
        project.dependencies.add(
            "modImplementation",
            "net.fabricmc:fabric-loader:${project.mod.prop("loader_version")}"
        )
        project.dependencies.add("modApi", "net.fabricmc.fabric-api:fabric-api:${project.mod.prop("fabric_version")}")
        project.dependencies.add(
            "modApi",
            "net.fabricmc.fabric-api:fabric-gametest-api-v1:${project.mod.prop("fabric_version")}"
        )
    }
}

/**
 * Load version specific dependencies from a properties file
 * It will look for a file in the versions/dependencies directory with the name of the minecraft version
 *
 * @param project The project to load the dependencies into
 * @param minecraftVersion The version of Minecraft to load the dependencies for
 *
 */
private fun loadSpecificDependencyVersions(project: Project, minecraftVersion: String) {
    val customPropsFile = project.rootProject.file("versions/dependencies/$minecraftVersion.properties")

    if (customPropsFile.exists()) {
        val customProps = Properties().apply {
            customPropsFile.inputStream().use { load(it) }
        }
        customProps.forEach { key, value ->
            project.extra[key.toString()] = value
        }
    }
}
