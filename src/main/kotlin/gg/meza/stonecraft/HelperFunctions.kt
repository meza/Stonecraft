package gg.meza.stonecraft

import dev.kikugie.stonecutter.build.StonecutterBuild
import org.gradle.api.Project

fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

fun getResourceVersionFor(version: String): Int = when (version) {
    "1.20.2" -> 18
    "1.21" -> 34
    "1.21.4" -> 46
    else -> 18
}

fun getProgramArgs(vararg lists: List<String>): List<String> = lists.flatMap { it }

fun applyIfAbsent(pluginId: String, project: Project) {
    if (!project.pluginManager.hasPlugin(pluginId)) {
        project.pluginManager.apply(pluginId)
    }
}

fun canBeLaunchedByArchitectury(mod: ModData, stonecutter: StonecutterBuild): Boolean {
    // @see https://github.com/architectury/architectury-loom/issues/262
    if (!mod.isForge) return true
    return (mod.isForge && stonecutter.eval(stonecutter.current.version, "<1.21.4"))
}

enum class Side {
    CLIENT,
    SERVER
}
