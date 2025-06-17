package gg.meza.stonecraft

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

val Project.mod: ModData get() = ModData(this)

/**
 * Represents the mod data for the project
 *
 * Use it from the build.gradle.kts file like this:
 *
 * ```
 * import gg.meza.stonecraft.*;
 *
 * if(mod.isFabric) {}
 * ```
 *
 * @param project The project to get the mod data from
 *
 * @property id The mod id coming from the root gradle.properties
 * @property name The mod name coming from the root gradle.properties
 * @property description The mod description coming from the root gradle.properties
 * @property version The mod version coming from the root gradle.properties
 * @property group The mod group coming from the root gradle.properties
 * @property loader The mod loader derived by the project name set by stonecutter in the settings.gradle.kts
 * @property isFabric Whether the mod is a fabric mod
 * @property isQuilt Whether the mod is a quilt mod
 * @property isForge Whether the mod is a forge mod
 * @property isNeoforge Whether the mod is a neoforge mod
 * @property isForgeLike Whether the mod is a forge or neoforge mod
 * @property isFabricLike Whether the mod is a fabric or quilt mod
 * @property prop A helper function to get a property from the project that is not in the mod data
 * @property hasProp A helper function to check if a property exists in the project
 */
class ModData(private val project: Project) {
    val id: String get() = project.property("mod.id").toString()
    val name: String get() = project.property("mod.name").toString()
    val description: String get() = project.property("mod.description").toString()
    val version: String get() = project.property("mod.version").toString()
    val group: String get() = project.property("mod.group").toString()
    val loader: String get() = project.name.substringAfterLast("-").lowercase()

    val isFabric: Boolean get() = loader == "fabric"
    val isQuilt: Boolean get() = loader == "quilt"
    val isForge: Boolean get() = loader == "forge"
    val isNeoforge: Boolean get() = loader == "neoforge"
    val isForgeLike: Boolean get() = isNeoforge || isForge
    val isFabricLike: Boolean get() = isFabric || isQuilt

    fun prop(key: String) = requireNotNull(project.extra[key]?.toString()) {
        "Property $key not found"
    }
    fun prop(key: String, default: String): String {
        if (project.extra.has(key)) {
            return project.extra[key].toString()
        }

        return default
    }

    fun hasProp(key: String): Boolean = project.extra.has(key)

    fun isSnapshot(): Boolean = prop("is_snapshot", "false") == "true"
}
