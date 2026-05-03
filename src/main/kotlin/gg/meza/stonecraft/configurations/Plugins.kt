package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.applyIfAbsent
import gg.meza.stonecraft.mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

fun configurePlugins(project: Project) {
    loadBasics(project)
    loadArchitectury(project)
    loadPublishing(project)
}

private fun loadBasics(project: Project) {
    applyIfAbsent("idea", project)
    applyIfAbsent("java", project)
}

private fun loadArchitectury(project: Project) {
    // The loom platform needs to be set before architectury is loaded
    project.extra["loom.platform"] = project.mod.loader
    project.extra["fabric.loom.disableObfuscation"] = project.mod.minecraftVersion.startsWith("2").toString()
    applyIfAbsent("dev.architectury.loom-no-remap", project)
}

private fun loadPublishing(project: Project) {
    applyIfAbsent("me.modmuss50.mod-publish-plugin", project)
}
