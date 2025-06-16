package gg.meza.stonecraft

import org.gradle.api.Project

fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

/**
 * Compares two version strings numerically.
 * Returns:
 *   - negative if a < b
 *   - zero if a == b
 *   - positive if a > b
 */
private fun compareVersionStrings(a: String, b: String): Int {
    val aParts = a.split('.').map { it.toIntOrNull() ?: 0 }
    val bParts = b.split('.').map { it.toIntOrNull() ?: 0 }
    val maxLength = maxOf(aParts.size, bParts.size)
    for (i in 0 until maxLength) {
        val aVal = aParts.getOrElse(i) { 0 }
        val bVal = bParts.getOrElse(i) { 0 }
        if (aVal != bVal) return aVal - bVal
    }
    return 0
}

fun getResourceVersionFor(version: String): Int {
    val versionMap = listOf(
        "1.20.2" to 18,
        "1.21" to 34,
        "1.21.4" to 46,
        "1.21.5" to 55,
        "1.21.6" to 63
    )

    if (compareVersionStrings(version, versionMap.first().first) < 0) {
        return versionMap.first().second
    }
    if (compareVersionStrings(version, versionMap.last().first) > 0) {
        return versionMap.last().second
    }
    for (i in versionMap.indices.reversed()) {
        if (compareVersionStrings(version, versionMap[i].first) >= 0) {
            return versionMap[i].second
        }
    }
    return versionMap.first().second // fallback, should not be reached
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
