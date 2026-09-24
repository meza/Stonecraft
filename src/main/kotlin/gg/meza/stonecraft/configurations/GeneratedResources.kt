package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.mod
import org.gradle.api.Project
import org.gradle.api.file.Directory

internal fun generatedResourceDirectories(
    project: Project,
    stonecutter: StonecutterBuildExtension,
    modSettings: ModSettingsExtension,
): List<Directory> {
    val base = modSettings.generatedResourcesProp.get()
    return if (project.mod.isNeoforge && stonecutter.eval(stonecutter.current.version, ">=1.21.4")) {
        listOf(base.dir("client"), base.dir("server"))
    } else {
        listOf(base)
    }
}
