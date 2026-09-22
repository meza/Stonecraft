package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.MinecraftObfuscation
import gg.meza.stonecraft.applyIfAbsent
import gg.meza.stonecraft.mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

fun configurePlugins(project: Project, minecraftObfuscation: MinecraftObfuscation) {
    loadBasics(project)
    loadArchitectury(project, minecraftObfuscation)
    loadPublishing(project)
}

private fun loadBasics(project: Project) {
    applyIfAbsent("idea", project)
    applyIfAbsent("java", project)
}

private fun loadArchitectury(project: Project, minecraftObfuscation: MinecraftObfuscation) {
    // The loom platform needs to be set before architectury is loaded
    project.extra["loom.platform"] = project.mod.loader
    project.extra["fabric.loom.disableObfuscation"] =
        (minecraftObfuscation == MinecraftObfuscation.UNOBFUSCATED).toString()
    applyIfAbsent("dev.architectury.loom", project)
}

private fun loadPublishing(project: Project) {
    applyIfAbsent("me.modmuss50.mod-publish-plugin", project)
}
