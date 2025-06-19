package gg.meza.stonecraft

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.gradle.api.Project
import java.io.InputStreamReader

fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }
private data class PackFormats(val datapack: Int, val resourcepack: Int)

private object ResourceHolder

private val resourceVersionMap: Map<String, Int> by lazy {
    val stream = requireNotNull(
        ResourceHolder::class.java.classLoader.getResourceAsStream("pack_versions.json")
    ) { "pack_versions.json not found" }
    val type = object : TypeToken<Map<String, PackFormats>>() {}.type
    val map: Map<String, PackFormats> =
        GsonBuilder().create().fromJson(InputStreamReader(stream), type)
    map.mapValues { it.value.resourcepack }
}

fun getResourceVersionFor(version: String): Int {
    return resourceVersionMap[version]
        ?: throw IllegalArgumentException("Unknown Minecraft version: $version")
}

fun getProgramArgs(vararg lists: List<String>): List<String> = lists.flatMap { it }

fun applyIfAbsent(pluginId: String, project: Project) {
    if (!project.pluginManager.hasPlugin(pluginId)) {
        project.pluginManager.apply(pluginId)
    }
}

enum class Side {
    CLIENT,
    SERVER
}
